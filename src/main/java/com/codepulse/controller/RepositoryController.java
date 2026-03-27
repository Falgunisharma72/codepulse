package com.codepulse.controller;

import com.codepulse.model.dto.ApiResponse;
import com.codepulse.model.dto.RepositoryDTO;
import com.codepulse.queue.AnalysisJobPublisher;
import com.codepulse.model.entity.AnalysisRun;
import com.codepulse.model.enums.AnalysisStatus;
import com.codepulse.repository.AnalysisRunRepository;
import com.codepulse.repository.RepoRepository;
import com.codepulse.service.RepositoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/repositories")
@RequiredArgsConstructor
@Tag(name = "Repositories", description = "Repository management endpoints")
public class RepositoryController {

    private final RepositoryService repositoryService;
    private final RepoRepository repoRepository;
    private final AnalysisRunRepository analysisRunRepository;
    private final AnalysisJobPublisher jobPublisher;

    @PostMapping
    @Operation(summary = "Register a new repository for tracking")
    public ResponseEntity<ApiResponse<RepositoryDTO>> registerRepository(@Valid @RequestBody RepositoryDTO dto) {
        RepositoryDTO created = repositoryService.registerRepository(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(created));
    }

    @GetMapping
    @Operation(summary = "List all tracked repositories")
    public ResponseEntity<ApiResponse<List<RepositoryDTO>>> getAllRepositories() {
        return ResponseEntity.ok(ApiResponse.ok(repositoryService.getAllRepositories()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get repository details with latest metrics")
    public ResponseEntity<ApiResponse<RepositoryDTO>> getRepository(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(repositoryService.getRepository(id)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update repository settings")
    public ResponseEntity<ApiResponse<RepositoryDTO>> updateRepository(
            @PathVariable Long id, @RequestBody RepositoryDTO dto) {
        return ResponseEntity.ok(ApiResponse.ok(repositoryService.updateRepository(id, dto)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Stop tracking a repository")
    public ResponseEntity<Void> deleteRepository(@PathVariable Long id) {
        repositoryService.deleteRepository(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/analyze")
    @Operation(summary = "Trigger manual analysis for a repository")
    public ResponseEntity<ApiResponse<Map<String, String>>> triggerAnalysis(@PathVariable Long id) {
        com.codepulse.model.entity.Repository repo = repoRepository.findById(id)
                .orElseThrow(() -> new com.codepulse.exception.RepositoryNotFoundException(id));

        AnalysisRun run = AnalysisRun.builder()
                .repository(repo)
                .commitSha("HEAD")
                .commitMessage("Manual analysis trigger")
                .author("manual")
                .branch(repo.getDefaultBranch())
                .status(AnalysisStatus.QUEUED)
                .build();
        run = analysisRunRepository.save(run);
        jobPublisher.publishJob(run.getId());

        return ResponseEntity.accepted()
                .body(ApiResponse.ok(Map.of("status", "queued", "analysisRunId", run.getId().toString())));
    }
}
