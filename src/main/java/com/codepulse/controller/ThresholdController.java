package com.codepulse.controller;

import com.codepulse.model.dto.ApiResponse;
import com.codepulse.model.entity.QualityThreshold;
import com.codepulse.repository.QualityThresholdRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/repositories/{repoId}/thresholds")
@RequiredArgsConstructor
@Tag(name = "Thresholds", description = "Quality threshold configuration")
public class ThresholdController {

    private final QualityThresholdRepository thresholdRepository;

    @GetMapping
    @Operation(summary = "Get current quality thresholds")
    public ResponseEntity<ApiResponse<QualityThreshold>> getThresholds(@PathVariable Long repoId) {
        QualityThreshold threshold = thresholdRepository.findByRepositoryId(repoId)
                .orElse(QualityThreshold.builder().build());
        return ResponseEntity.ok(ApiResponse.ok(threshold));
    }

    @PutMapping
    @Operation(summary = "Update quality thresholds")
    public ResponseEntity<ApiResponse<QualityThreshold>> updateThresholds(
            @PathVariable Long repoId, @RequestBody QualityThreshold updated) {
        QualityThreshold threshold = thresholdRepository.findByRepositoryId(repoId)
                .orElseThrow(() -> new RuntimeException("Thresholds not found for repository: " + repoId));

        if (updated.getMaxCyclomaticComplexity() != null)
            threshold.setMaxCyclomaticComplexity(updated.getMaxCyclomaticComplexity());
        if (updated.getMaxMethodLength() != null)
            threshold.setMaxMethodLength(updated.getMaxMethodLength());
        if (updated.getMaxFileLength() != null)
            threshold.setMaxFileLength(updated.getMaxFileLength());
        if (updated.getMaxNestingDepth() != null)
            threshold.setMaxNestingDepth(updated.getMaxNestingDepth());
        if (updated.getMinHealthScore() != null)
            threshold.setMinHealthScore(updated.getMinHealthScore());

        threshold = thresholdRepository.save(threshold);
        return ResponseEntity.ok(ApiResponse.ok(threshold));
    }

    @PostMapping("/reset")
    @Operation(summary = "Reset thresholds to defaults")
    public ResponseEntity<ApiResponse<Map<String, String>>> resetThresholds(@PathVariable Long repoId) {
        QualityThreshold threshold = thresholdRepository.findByRepositoryId(repoId)
                .orElseThrow(() -> new RuntimeException("Thresholds not found for repository: " + repoId));

        threshold.setMaxCyclomaticComplexity(10);
        threshold.setMaxMethodLength(30);
        threshold.setMaxFileLength(300);
        threshold.setMaxNestingDepth(4);
        threshold.setMinHealthScore(70.0);
        thresholdRepository.save(threshold);

        return ResponseEntity.ok(ApiResponse.ok(Map.of("status", "reset to defaults")));
    }
}
