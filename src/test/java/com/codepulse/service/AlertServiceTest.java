package com.codepulse.service;

import com.codepulse.TestFixtures;
import com.codepulse.model.entity.*;
import com.codepulse.model.enums.Severity;
import com.codepulse.repository.AlertRepository;
import com.codepulse.repository.MetricsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlertServiceTest {

    @Mock
    private AlertRepository alertRepository;

    @Mock
    private MetricsRepository metricsRepository;

    @InjectMocks
    private AlertService alertService;

    private User testUser;
    private Repository testRepo;
    private AnalysisRun testRun;
    private QualityThreshold threshold;

    @BeforeEach
    void setUp() {
        testUser = TestFixtures.createTestUser();
        testRepo = TestFixtures.createTestRepository(testUser);
        testRun = TestFixtures.createTestAnalysisRun(testRepo);
        threshold = TestFixtures.createDefaultThreshold(testRepo);

        lenient().when(alertRepository.findByRepositoryIdAndIsResolvedFalseOrderByCreatedAtDesc(anyLong()))
                .thenReturn(Collections.emptyList());
        lenient().when(alertRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void generateAlerts_metricsWithinThresholds_noAlerts() {
        Metrics metrics = Metrics.builder()
                .overallHealthScore(85.0)
                .avgCyclomaticComplexity(5.0)
                .maxMethodLength(20)
                .build();

        when(metricsRepository.findLatestByRepositoryId(anyLong())).thenReturn(Optional.empty());

        List<Alert> alerts = alertService.generateAlerts(testRun, metrics, threshold);
        assertTrue(alerts.isEmpty(), "No alerts should be generated when within thresholds");
    }

    @Test
    void generateAlerts_lowHealthScore_criticalAlert() {
        Metrics metrics = Metrics.builder()
                .overallHealthScore(50.0)
                .avgCyclomaticComplexity(5.0)
                .maxMethodLength(20)
                .build();

        when(metricsRepository.findLatestByRepositoryId(anyLong())).thenReturn(Optional.empty());

        List<Alert> alerts = alertService.generateAlerts(testRun, metrics, threshold);
        assertTrue(alerts.stream().anyMatch(a -> a.getSeverity() == Severity.CRITICAL),
                "Should generate CRITICAL alert for low health score");
    }

    @Test
    void generateAlerts_highComplexity_warningAlert() {
        Metrics metrics = Metrics.builder()
                .overallHealthScore(85.0)
                .avgCyclomaticComplexity(25.0) // 2.5x threshold
                .maxMethodLength(20)
                .build();

        when(metricsRepository.findLatestByRepositoryId(anyLong())).thenReturn(Optional.empty());

        List<Alert> alerts = alertService.generateAlerts(testRun, metrics, threshold);
        assertTrue(alerts.stream().anyMatch(a ->
                        a.getSeverity() == Severity.WARNING && a.getAlertType().name().equals("COMPLEXITY")),
                "Should generate WARNING alert for high complexity");
    }

    @Test
    void generateAlerts_scoreRegression_criticalAlert() {
        Metrics previousMetrics = Metrics.builder()
                .overallHealthScore(90.0)
                .build();
        Metrics currentMetrics = Metrics.builder()
                .overallHealthScore(75.0)
                .avgCyclomaticComplexity(5.0)
                .maxMethodLength(20)
                .build();

        when(metricsRepository.findLatestByRepositoryId(anyLong()))
                .thenReturn(Optional.of(previousMetrics));

        List<Alert> alerts = alertService.generateAlerts(testRun, currentMetrics, threshold);
        assertTrue(alerts.stream().anyMatch(a ->
                        a.getSeverity() == Severity.CRITICAL && a.getAlertType().name().equals("REGRESSION")),
                "Should generate CRITICAL alert for score regression > 10 points");
    }
}
