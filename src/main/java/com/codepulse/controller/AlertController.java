package com.codepulse.controller;

import com.codepulse.model.dto.AlertDTO;
import com.codepulse.model.dto.ApiResponse;
import com.codepulse.service.AlertService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Alerts", description = "Alert management endpoints")
public class AlertController {

    private final AlertService alertService;

    @GetMapping("/repositories/{repoId}/alerts")
    @Operation(summary = "List active alerts for a repository")
    public ResponseEntity<ApiResponse<List<AlertDTO>>> getRepoAlerts(@PathVariable Long repoId) {
        return ResponseEntity.ok(ApiResponse.ok(alertService.getActiveAlerts(repoId)));
    }

    @GetMapping("/alerts/summary")
    @Operation(summary = "Get alert counts across all repos")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAlertSummary() {
        List<AlertDTO> allAlerts = alertService.getAllActiveAlerts();
        long criticalCount = allAlerts.stream()
                .filter(a -> "CRITICAL".equals(a.getSeverity().name())).count();
        long warningCount = allAlerts.stream()
                .filter(a -> "WARNING".equals(a.getSeverity().name())).count();

        Map<String, Object> summary = Map.of(
                "total", allAlerts.size(),
                "critical", criticalCount,
                "warning", warningCount,
                "alerts", allAlerts.stream().limit(10).toList()
        );
        return ResponseEntity.ok(ApiResponse.ok(summary));
    }

    @PutMapping("/alerts/{id}/resolve")
    @Operation(summary = "Manually resolve an alert")
    public ResponseEntity<ApiResponse<Map<String, String>>> resolveAlert(@PathVariable Long id) {
        alertService.resolveAlert(id);
        return ResponseEntity.ok(ApiResponse.ok(Map.of("status", "resolved")));
    }
}
