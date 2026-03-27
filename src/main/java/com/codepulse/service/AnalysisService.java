package com.codepulse.service;

import com.codepulse.analyzer.AnalyzerFactory;
import com.codepulse.analyzer.CodeAnalyzer;
import com.codepulse.model.entity.*;
import com.codepulse.model.enums.AnalysisStatus;
import com.codepulse.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalysisService {

    private final AnalysisRunRepository analysisRunRepository;
    private final FileMetricsRepository fileMetricsRepository;
    private final MetricsRepository metricsRepository;
    private final QualityThresholdRepository thresholdRepository;
    private final GitCloneService gitCloneService;
    private final AnalyzerFactory analyzerFactory;
    private final MetricsCalculatorService metricsCalculatorService;
    private final HealthScoreService healthScoreService;
    private final AlertService alertService;

    @Value("${codepulse.analysis.max-file-size-kb:500}")
    private int maxFileSizeKb;

    private static final Set<String> SKIP_DIRS = Set.of(
            "node_modules", ".git", "build", "target", "vendor",
            "__pycache__", ".idea", ".vscode", "dist", "out", ".gradle"
    );

    @Transactional
    public void analyze(Long analysisRunId) {
        AnalysisRun run = analysisRunRepository.findById(analysisRunId)
                .orElseThrow(() -> new RuntimeException("Analysis run not found: " + analysisRunId));

        run.setStatus(AnalysisStatus.RUNNING);
        run.setStartedAt(LocalDateTime.now());
        analysisRunRepository.save(run);

        Path repoDir = null;
        try {
            Repository repository = run.getRepository();

            // Clone the repository
            String accessToken = repository.getUser() != null ? repository.getUser().getAccessToken() : null;
            repoDir = gitCloneService.cloneRepository(
                    repository.getGithubRepoUrl(),
                    run.getCommitSha(),
                    run.getId(),
                    accessToken
            );

            // Walk file tree and analyze
            List<FileMetrics> allFileMetrics = new ArrayList<>();
            List<String> allFileContents = new ArrayList<>();

            try (Stream<Path> fileStream = Files.walk(repoDir)) {
                List<Path> files = fileStream
                        .filter(Files::isRegularFile)
                        .filter(this::shouldAnalyze)
                        .toList();

                for (Path file : files) {
                    String language = analyzerFactory.detectLanguage(file);
                    if (language == null) continue;

                    CodeAnalyzer analyzer = analyzerFactory.getAnalyzer(language);
                    if (analyzer == null) continue;

                    FileMetrics fm = analyzer.analyzeFile(file);
                    fm.setAnalysisRun(run);
                    fm.setFilePath(repoDir.relativize(file).toString());
                    allFileMetrics.add(fm);

                    try {
                        allFileContents.add(Files.readString(file));
                    } catch (IOException e) {
                        log.warn("Could not read file for duplicate detection: {}", file);
                    }
                }
            }

            // Save file metrics
            fileMetricsRepository.saveAll(allFileMetrics);

            // Aggregate metrics
            Metrics metrics = metricsCalculatorService.aggregate(allFileMetrics, allFileContents);
            metrics.setAnalysisRun(run);

            // Calculate health score
            QualityThreshold threshold = thresholdRepository.findByRepositoryId(repository.getId())
                    .orElse(QualityThreshold.builder().build());
            double healthScore = healthScoreService.calculateScore(metrics, threshold);
            metrics.setOverallHealthScore(healthScore);

            metricsRepository.save(metrics);

            // Generate alerts
            alertService.generateAlerts(run, metrics, threshold);

            // Mark as completed
            run.setStatus(AnalysisStatus.COMPLETED);
            run.setCompletedAt(LocalDateTime.now());
            analysisRunRepository.save(run);

            log.info("Analysis completed for run {}. Health score: {}", analysisRunId, healthScore);

        } catch (Exception e) {
            log.error("Analysis failed for run {}: {}", analysisRunId, e.getMessage(), e);
            run.setStatus(AnalysisStatus.FAILED);
            run.setCompletedAt(LocalDateTime.now());
            analysisRunRepository.save(run);
        } finally {
            if (repoDir != null) {
                gitCloneService.cleanup(repoDir);
            }
        }
    }

    private boolean shouldAnalyze(Path path) {
        // Skip directories
        for (Path component : path) {
            if (SKIP_DIRS.contains(component.toString())) return false;
        }

        // Skip large files
        try {
            long sizeKb = Files.size(path) / 1024;
            if (sizeKb > maxFileSizeKb) return false;
        } catch (IOException e) {
            return false;
        }

        return analyzerFactory.isSupported(path);
    }
}
