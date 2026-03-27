package com.codepulse.controller;

import com.codepulse.model.dto.*;
import com.codepulse.model.entity.AnalysisRun;
import com.codepulse.model.entity.Metrics;
import com.codepulse.model.enums.AnalysisStatus;
import com.codepulse.repository.AnalysisRunRepository;
import com.codepulse.repository.MetricsRepository;
import com.codepulse.repository.RepoRepository;
import com.codepulse.service.AlertService;
import com.codepulse.service.HealthScoreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Dashboard summary endpoints")
public class DashboardController {

    private final RepoRepository repoRepository;
    private final AnalysisRunRepository analysisRunRepository;
    private final MetricsRepository metricsRepository;
    private final AlertService alertService;
    private final HealthScoreService healthScoreService;

    @GetMapping("/summary")
    @Operation(summary = "Get aggregated stats across all repos")
    public ResponseEntity<ApiResponse<DashboardSummaryDTO>> getDashboardSummary() {
        long totalRepos = repoRepository.count();
        long totalAnalyses = analysisRunRepository.count();
        long activeAlerts = alertService.getActiveAlertCount();

        // Calculate average health score across all repos
        List<com.codepulse.model.entity.Repository> repos = repoRepository.findByIsActiveTrue();
        double avgHealthScore = repos.stream()
                .map(repo -> metricsRepository.findLatestByRepositoryId(repo.getId()))
                .filter(java.util.Optional::isPresent)
                .map(java.util.Optional::get)
                .filter(m -> m.getOverallHealthScore() != null)
                .mapToDouble(Metrics::getOverallHealthScore)
                .average()
                .orElse(0.0);

        // Health distribution
        Map<String, Long> healthDistribution = new HashMap<>();
        for (String grade : List.of("A", "B", "C", "D", "F")) {
            healthDistribution.put(grade, 0L);
        }
        repos.stream()
                .map(repo -> metricsRepository.findLatestByRepositoryId(repo.getId()))
                .filter(java.util.Optional::isPresent)
                .map(java.util.Optional::get)
                .filter(m -> m.getOverallHealthScore() != null)
                .forEach(m -> {
                    String grade = healthScoreService.getGrade(m.getOverallHealthScore());
                    healthDistribution.merge(grade, 1L, Long::sum);
                });

        // Recent analyses
        List<AnalysisRunDTO> recentAnalyses = analysisRunRepository
                .findAll(PageRequest.of(0, 5))
                .stream()
                .map(run -> AnalysisRunDTO.builder()
                        .id(run.getId())
                        .repositoryId(run.getRepository().getId())
                        .commitSha(run.getCommitSha())
                        .commitMessage(run.getCommitMessage())
                        .status(run.getStatus())
                        .createdAt(run.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        DashboardSummaryDTO summary = DashboardSummaryDTO.builder()
                .totalRepos(totalRepos)
                .totalAnalyses(totalAnalyses)
                .avgHealthScore(Math.round(avgHealthScore * 100.0) / 100.0)
                .activeAlerts(activeAlerts)
                .recentAnalyses(recentAnalyses)
                .healthDistribution(healthDistribution)
                .build();

        return ResponseEntity.ok(ApiResponse.ok(summary));
    }
}
