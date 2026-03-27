package com.codepulse.controller;

import com.codepulse.model.dto.ApiResponse;
import com.codepulse.model.dto.MetricsResponseDTO;
import com.codepulse.model.dto.TrendDataDTO;
import com.codepulse.model.entity.FileMetrics;
import com.codepulse.model.entity.Metrics;
import com.codepulse.repository.FileMetricsRepository;
import com.codepulse.repository.MetricsRepository;
import com.codepulse.service.HealthScoreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/repositories/{repoId}/metrics")
@RequiredArgsConstructor
@Tag(name = "Metrics", description = "Code quality metrics endpoints")
public class MetricsController {

    private final MetricsRepository metricsRepository;
    private final FileMetricsRepository fileMetricsRepository;
    private final HealthScoreService healthScoreService;

    @GetMapping("/latest")
    @Operation(summary = "Get latest metrics snapshot")
    public ResponseEntity<ApiResponse<MetricsResponseDTO>> getLatestMetrics(@PathVariable Long repoId) {
        Metrics metrics = metricsRepository.findLatestByRepositoryId(repoId)
                .orElseThrow(() -> new RuntimeException("No metrics found for repository: " + repoId));
        return ResponseEntity.ok(ApiResponse.ok(toDTO(metrics)));
    }

    @GetMapping("/trend")
    @Operation(summary = "Get historical trend data for charting")
    public ResponseEntity<ApiResponse<TrendDataDTO>> getTrend(
            @PathVariable Long repoId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {

        List<Metrics> metricsList = metricsRepository.findTrendByRepositoryId(repoId, from, to);

        List<TrendDataDTO.TrendPoint> points = metricsList.stream()
                .map(m -> TrendDataDTO.TrendPoint.builder()
                        .timestamp(m.getCreatedAt())
                        .commitSha(m.getAnalysisRun().getCommitSha())
                        .healthScore(m.getOverallHealthScore())
                        .avgComplexity(m.getAvgCyclomaticComplexity())
                        .totalLines(m.getTotalLines())
                        .codeSmellCount(m.getCodeSmellCount())
                        .totalFiles(m.getTotalFiles())
                        .build())
                .collect(Collectors.toList());

        TrendDataDTO trend = TrendDataDTO.builder()
                .repositoryId(repoId)
                .dataPoints(points)
                .build();

        return ResponseEntity.ok(ApiResponse.ok(trend));
    }

    @GetMapping("/hotspots")
    @Operation(summary = "Get top 10 worst files by complexity")
    public ResponseEntity<ApiResponse<List<FileMetrics>>> getHotspots(@PathVariable Long repoId) {
        List<FileMetrics> hotspots = fileMetricsRepository.findHotspotsByRepositoryId(repoId);
        List<FileMetrics> top10 = hotspots.stream().limit(10).toList();
        return ResponseEntity.ok(ApiResponse.ok(top10));
    }

    private MetricsResponseDTO toDTO(Metrics m) {
        return MetricsResponseDTO.builder()
                .id(m.getId())
                .analysisRunId(m.getAnalysisRun().getId())
                .totalFiles(m.getTotalFiles())
                .totalLines(m.getTotalLines())
                .avgCyclomaticComplexity(m.getAvgCyclomaticComplexity())
                .maxCyclomaticComplexity(m.getMaxCyclomaticComplexity())
                .avgMethodLength(m.getAvgMethodLength())
                .maxMethodLength(m.getMaxMethodLength())
                .duplicateBlockCount(m.getDuplicateBlockCount())
                .codeSmellCount(m.getCodeSmellCount())
                .testFileCount(m.getTestFileCount())
                .testCoverageEstimate(m.getTestCoverageEstimate())
                .overallHealthScore(m.getOverallHealthScore())
                .healthGrade(healthScoreService.getGrade(m.getOverallHealthScore() != null ? m.getOverallHealthScore() : 0))
                .createdAt(m.getCreatedAt())
                .build();
    }
}
