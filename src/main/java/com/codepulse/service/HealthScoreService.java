package com.codepulse.service;

import com.codepulse.model.entity.Metrics;
import com.codepulse.model.entity.QualityThreshold;
import org.springframework.stereotype.Service;

@Service
public class HealthScoreService {

    public double calculateScore(Metrics metrics, QualityThreshold threshold) {
        double score = 100.0;

        // Penalize high average complexity
        if (metrics.getAvgCyclomaticComplexity() > threshold.getMaxCyclomaticComplexity()) {
            score -= (metrics.getAvgCyclomaticComplexity() - threshold.getMaxCyclomaticComplexity()) * 3;
        }

        // Penalize long methods
        if (metrics.getMaxMethodLength() > threshold.getMaxMethodLength()) {
            score -= 5;
        }

        // Penalize duplicates
        if (metrics.getDuplicateBlockCount() != null) {
            score -= metrics.getDuplicateBlockCount() * 2;
        }

        // Penalize code smells relative to total files
        if (metrics.getTotalFiles() > 0 && metrics.getCodeSmellCount() != null) {
            double smellRatio = (double) metrics.getCodeSmellCount() / metrics.getTotalFiles();
            score -= smellRatio * 20;
        }

        // Bonus for test coverage
        if (metrics.getTestCoverageEstimate() != null && metrics.getTestCoverageEstimate() > 50) {
            score += 10;
        }

        return Math.max(0, Math.min(100, Math.round(score * 100.0) / 100.0));
    }

    public String getGrade(double score) {
        if (score >= 90) return "A";
        if (score >= 80) return "B";
        if (score >= 70) return "C";
        if (score >= 60) return "D";
        return "F";
    }
}
