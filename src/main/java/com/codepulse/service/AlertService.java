package com.codepulse.service;

import com.codepulse.model.dto.AlertDTO;
import com.codepulse.model.entity.*;
import com.codepulse.model.enums.AlertType;
import com.codepulse.model.enums.Severity;
import com.codepulse.repository.AlertRepository;
import com.codepulse.repository.MetricsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlertService {

    private final AlertRepository alertRepository;
    private final MetricsRepository metricsRepository;

    @Transactional
    public List<Alert> generateAlerts(AnalysisRun analysisRun, Metrics metrics, QualityThreshold threshold) {
        List<Alert> alerts = new ArrayList<>();
        Repository repository = analysisRun.getRepository();

        // Health score below minimum
        if (metrics.getOverallHealthScore() < threshold.getMinHealthScore()) {
            alerts.add(Alert.builder()
                    .analysisRun(analysisRun)
                    .repository(repository)
                    .alertType(AlertType.HEALTH_SCORE)
                    .severity(Severity.CRITICAL)
                    .message(String.format("Health score %.1f is below minimum %.1f",
                            metrics.getOverallHealthScore(), threshold.getMinHealthScore()))
                    .build());
        }

        // High complexity
        if (metrics.getAvgCyclomaticComplexity() > threshold.getMaxCyclomaticComplexity() * 2) {
            alerts.add(Alert.builder()
                    .analysisRun(analysisRun)
                    .repository(repository)
                    .alertType(AlertType.COMPLEXITY)
                    .severity(Severity.WARNING)
                    .message(String.format("Average complexity %.1f exceeds 2x threshold of %d",
                            metrics.getAvgCyclomaticComplexity(), threshold.getMaxCyclomaticComplexity()))
                    .build());
        }

        // Long methods
        if (metrics.getMaxMethodLength() > threshold.getMaxMethodLength()) {
            alerts.add(Alert.builder()
                    .analysisRun(analysisRun)
                    .repository(repository)
                    .alertType(AlertType.METHOD_LENGTH)
                    .severity(Severity.WARNING)
                    .message(String.format("Max method length %d exceeds threshold of %d",
                            metrics.getMaxMethodLength(), threshold.getMaxMethodLength()))
                    .build());
        }

        // Check for regression (score dropped > 10 points)
        metricsRepository.findLatestByRepositoryId(repository.getId()).ifPresent(previousMetrics -> {
            if (previousMetrics.getOverallHealthScore() != null && metrics.getOverallHealthScore() != null) {
                double drop = previousMetrics.getOverallHealthScore() - metrics.getOverallHealthScore();
                if (drop > 10) {
                    alerts.add(Alert.builder()
                            .analysisRun(analysisRun)
                            .repository(repository)
                            .alertType(AlertType.REGRESSION)
                            .severity(Severity.CRITICAL)
                            .message(String.format("Health score dropped %.1f points (from %.1f to %.1f)",
                                    drop, previousMetrics.getOverallHealthScore(), metrics.getOverallHealthScore()))
                            .build());
                }
            }
        });

        // Save alerts and mark old ones as resolved
        resolveOldAlerts(repository.getId());
        return alertRepository.saveAll(alerts);
    }

    public List<AlertDTO> getActiveAlerts(Long repositoryId) {
        return alertRepository.findByRepositoryIdAndIsResolvedFalseOrderByCreatedAtDesc(repositoryId)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<AlertDTO> getAllActiveAlerts() {
        return alertRepository.findAllActiveAlerts()
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional
    public void resolveAlert(Long alertId) {
        alertRepository.findById(alertId).ifPresent(alert -> {
            alert.setIsResolved(true);
            alertRepository.save(alert);
        });
    }

    public long getActiveAlertCount() {
        return alertRepository.countByIsResolvedFalse();
    }

    private void resolveOldAlerts(Long repositoryId) {
        List<Alert> oldAlerts = alertRepository.findByRepositoryIdAndIsResolvedFalseOrderByCreatedAtDesc(repositoryId);
        oldAlerts.forEach(alert -> alert.setIsResolved(true));
        alertRepository.saveAll(oldAlerts);
    }

    private AlertDTO toDTO(Alert alert) {
        return AlertDTO.builder()
                .id(alert.getId())
                .repositoryId(alert.getRepository().getId())
                .repoName(alert.getRepository().getRepoName())
                .analysisRunId(alert.getAnalysisRun().getId())
                .alertType(alert.getAlertType())
                .severity(alert.getSeverity())
                .message(alert.getMessage())
                .filePath(alert.getFilePath())
                .isResolved(alert.getIsResolved())
                .createdAt(alert.getCreatedAt())
                .build();
    }
}
