package com.codepulse.controller;

import com.codepulse.model.dto.AnalysisRunDTO;
import com.codepulse.model.dto.ApiResponse;
import com.codepulse.model.entity.AnalysisRun;
import com.codepulse.model.entity.FileMetrics;
import com.codepulse.model.enums.AnalysisStatus;
import com.codepulse.repository.AnalysisRunRepository;
import com.codepulse.repository.FileMetricsRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/repositories/{repoId}/analyses")
@RequiredArgsConstructor
@Tag(name = "Analysis", description = "Analysis run endpoints")
public class AnalysisController {

    private final AnalysisRunRepository analysisRunRepository;
    private final FileMetricsRepository fileMetricsRepository;

    @GetMapping
    @Operation(summary = "List all analysis runs for a repository")
    public ResponseEntity<ApiResponse<Page<AnalysisRunDTO>>> getAnalyses(
            @PathVariable Long repoId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<AnalysisRunDTO> runs = analysisRunRepository
                .findByRepositoryIdOrderByCreatedAtDesc(repoId, PageRequest.of(page, size))
                .map(this::toDTO);
        return ResponseEntity.ok(ApiResponse.ok(runs));
    }

    @GetMapping("/{runId}")
    @Operation(summary = "Get single analysis run details")
    public ResponseEntity<ApiResponse<AnalysisRunDTO>> getAnalysis(
            @PathVariable Long repoId, @PathVariable Long runId) {
        AnalysisRun run = analysisRunRepository.findById(runId)
                .orElseThrow(() -> new RuntimeException("Analysis run not found: " + runId));
        return ResponseEntity.ok(ApiResponse.ok(toDTO(run)));
    }

    @GetMapping("/{runId}/files")
    @Operation(summary = "Get per-file breakdown for an analysis run")
    public ResponseEntity<ApiResponse<List<FileMetrics>>> getFileBreakdown(
            @PathVariable Long repoId, @PathVariable Long runId) {
        List<FileMetrics> files = fileMetricsRepository.findByAnalysisRunId(runId);
        return ResponseEntity.ok(ApiResponse.ok(files));
    }

    @GetMapping("/latest")
    @Operation(summary = "Get latest completed analysis run")
    public ResponseEntity<ApiResponse<AnalysisRunDTO>> getLatest(@PathVariable Long repoId) {
        AnalysisRun run = analysisRunRepository
                .findFirstByRepositoryIdAndStatusOrderByCreatedAtDesc(repoId, AnalysisStatus.COMPLETED)
                .orElseThrow(() -> new RuntimeException("No completed analysis found for repository: " + repoId));
        return ResponseEntity.ok(ApiResponse.ok(toDTO(run)));
    }

    private AnalysisRunDTO toDTO(AnalysisRun run) {
        Double healthScore = null;
        if (run.getMetrics() != null) {
            healthScore = run.getMetrics().getOverallHealthScore();
        }
        return AnalysisRunDTO.builder()
                .id(run.getId())
                .repositoryId(run.getRepository().getId())
                .commitSha(run.getCommitSha())
                .commitMessage(run.getCommitMessage())
                .author(run.getAuthor())
                .branch(run.getBranch())
                .status(run.getStatus())
                .startedAt(run.getStartedAt())
                .completedAt(run.getCompletedAt())
                .createdAt(run.getCreatedAt())
                .healthScore(healthScore)
                .build();
    }
}
