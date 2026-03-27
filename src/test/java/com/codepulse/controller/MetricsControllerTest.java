package com.codepulse.controller;

import com.codepulse.TestFixtures;
import com.codepulse.exception.GlobalExceptionHandler;
import com.codepulse.model.entity.AnalysisRun;
import com.codepulse.model.entity.Metrics;
import com.codepulse.model.entity.Repository;
import com.codepulse.model.entity.User;
import com.codepulse.repository.FileMetricsRepository;
import com.codepulse.repository.MetricsRepository;
import com.codepulse.service.HealthScoreService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class MetricsControllerTest {

    @Mock
    private MetricsRepository metricsRepository;
    @Mock
    private FileMetricsRepository fileMetricsRepository;
    @Mock
    private HealthScoreService healthScoreService;

    @InjectMocks
    private MetricsController controller;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void getLatestMetrics_found_returnsData() throws Exception {
        User user = TestFixtures.createTestUser();
        Repository repo = TestFixtures.createTestRepository(user);
        AnalysisRun run = TestFixtures.createTestAnalysisRun(repo);
        Metrics metrics = TestFixtures.createTestMetrics(run);

        when(metricsRepository.findLatestByRepositoryId(1L)).thenReturn(Optional.of(metrics));
        when(healthScoreService.getGrade(anyDouble())).thenReturn("B");

        mockMvc.perform(get("/api/repositories/1/metrics/latest"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.overallHealthScore").value(82.0))
                .andExpect(jsonPath("$.data.healthGrade").value("B"));
    }

    @Test
    void getLatestMetrics_notFound_returns500() throws Exception {
        when(metricsRepository.findLatestByRepositoryId(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/repositories/99/metrics/latest"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getTrend_validDateRange_returnsTrendData() throws Exception {
        User user = TestFixtures.createTestUser();
        Repository repo = TestFixtures.createTestRepository(user);
        AnalysisRun run = TestFixtures.createTestAnalysisRun(repo);
        Metrics metrics = TestFixtures.createTestMetrics(run);

        when(metricsRepository.findTrendByRepositoryId(eq(1L), any(), any()))
                .thenReturn(List.of(metrics));

        mockMvc.perform(get("/api/repositories/1/metrics/trend")
                        .param("from", "2026-01-01T00:00:00")
                        .param("to", "2026-03-27T23:59:59"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.dataPoints.length()").value(1));
    }

    @Test
    void getHotspots_returnsTop10() throws Exception {
        when(fileMetricsRepository.findHotspotsByRepositoryId(1L)).thenReturn(List.of());

        mockMvc.perform(get("/api/repositories/1/metrics/hotspots"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }
}
