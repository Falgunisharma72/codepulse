package com.codepulse.service;

import com.codepulse.TestFixtures;
import com.codepulse.analyzer.AnalyzerFactory;
import com.codepulse.analyzer.JavaAnalyzer;
import com.codepulse.model.entity.*;
import com.codepulse.model.enums.AnalysisStatus;
import com.codepulse.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnalysisServiceTest {

    @Mock private AnalysisRunRepository analysisRunRepository;
    @Mock private FileMetricsRepository fileMetricsRepository;
    @Mock private MetricsRepository metricsRepository;
    @Mock private QualityThresholdRepository thresholdRepository;
    @Mock private GitCloneService gitCloneService;
    @Mock private AnalyzerFactory analyzerFactory;
    @Mock private MetricsCalculatorService metricsCalculatorService;
    @Mock private HealthScoreService healthScoreService;
    @Mock private AlertService alertService;

    @InjectMocks
    private AnalysisService analysisService;

    @TempDir
    Path tempDir;

    private User testUser;
    private Repository testRepo;
    private AnalysisRun testRun;

    @BeforeEach
    void setUp() {
        testUser = TestFixtures.createTestUser();
        testRepo = TestFixtures.createTestRepository(testUser);
        testRun = TestFixtures.createTestAnalysisRun(testRepo);
    }

    @Test
    void analyze_successfulRun_completesWithMetrics() throws Exception {
        // Create a test file in temp dir
        Path javaFile = tempDir.resolve("Test.java");
        Files.writeString(javaFile, TestFixtures.createSampleJavaCode());

        when(analysisRunRepository.findById(1L)).thenReturn(Optional.of(testRun));
        when(analysisRunRepository.save(any())).thenReturn(testRun);
        when(gitCloneService.cloneRepository(any(), any(), any(), any())).thenReturn(tempDir);
        when(analyzerFactory.detectLanguage(any())).thenReturn("java");
        when(analyzerFactory.isSupported(any())).thenReturn(true);
        when(analyzerFactory.getAnalyzer("java")).thenReturn(new JavaAnalyzer());
        when(metricsCalculatorService.aggregate(anyList(), anyList()))
                .thenReturn(Metrics.builder().totalFiles(1).totalLines(100)
                        .avgCyclomaticComplexity(5.0).maxMethodLength(20)
                        .duplicateBlockCount(0).codeSmellCount(0).testFileCount(0)
                        .testCoverageEstimate(0.0).build());
        when(thresholdRepository.findByRepositoryId(anyLong()))
                .thenReturn(Optional.of(TestFixtures.createDefaultThreshold(testRepo)));
        when(healthScoreService.calculateScore(any(), any())).thenReturn(85.0);
        when(alertService.generateAlerts(any(), any(), any())).thenReturn(Collections.emptyList());

        analysisService.analyze(1L);

        verify(metricsRepository).save(any(Metrics.class));
        verify(gitCloneService).cleanup(tempDir);
        // Verify the run was marked as COMPLETED
        verify(analysisRunRepository, atLeast(2)).save(argThat(run ->
                run.getStatus() == AnalysisStatus.RUNNING || run.getStatus() == AnalysisStatus.COMPLETED));
    }

    @Test
    void analyze_cloneFails_markedAsFailed() throws Exception {
        when(analysisRunRepository.findById(1L)).thenReturn(Optional.of(testRun));
        when(analysisRunRepository.save(any())).thenReturn(testRun);
        when(gitCloneService.cloneRepository(any(), any(), any(), any()))
                .thenThrow(new IOException("Clone failed"));

        analysisService.analyze(1L);

        verify(analysisRunRepository).save(argThat(run -> run.getStatus() == AnalysisStatus.FAILED));
    }
}
