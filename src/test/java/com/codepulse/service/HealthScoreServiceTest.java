package com.codepulse.service;

import com.codepulse.model.entity.Metrics;
import com.codepulse.model.entity.QualityThreshold;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HealthScoreServiceTest {

    private HealthScoreService healthScoreService;

    @BeforeEach
    void setUp() {
        healthScoreService = new HealthScoreService();
    }

    @Test
    void calculateScore_perfectCode_nearHundred() {
        Metrics metrics = Metrics.builder()
                .avgCyclomaticComplexity(3.0)
                .maxMethodLength(15)
                .duplicateBlockCount(0)
                .codeSmellCount(0)
                .totalFiles(10)
                .testCoverageEstimate(80.0)
                .build();
        QualityThreshold threshold = QualityThreshold.builder()
                .maxCyclomaticComplexity(10)
                .maxMethodLength(30)
                .minHealthScore(70.0)
                .build();

        double score = healthScoreService.calculateScore(metrics, threshold);
        assertTrue(score >= 90, "Perfect code should score >= 90, got: " + score);
    }

    @Test
    void calculateScore_terribleCode_nearZero() {
        Metrics metrics = Metrics.builder()
                .avgCyclomaticComplexity(30.0)
                .maxMethodLength(100)
                .duplicateBlockCount(20)
                .codeSmellCount(15)
                .totalFiles(10)
                .testCoverageEstimate(10.0)
                .build();
        QualityThreshold threshold = QualityThreshold.builder()
                .maxCyclomaticComplexity(10)
                .maxMethodLength(30)
                .minHealthScore(70.0)
                .build();

        double score = healthScoreService.calculateScore(metrics, threshold);
        assertTrue(score < 30, "Terrible code should score < 30, got: " + score);
    }

    @Test
    void calculateScore_clampedBetweenZeroAndHundred() {
        Metrics metrics = Metrics.builder()
                .avgCyclomaticComplexity(100.0)
                .maxMethodLength(500)
                .duplicateBlockCount(100)
                .codeSmellCount(100)
                .totalFiles(10)
                .testCoverageEstimate(0.0)
                .build();
        QualityThreshold threshold = QualityThreshold.builder()
                .maxCyclomaticComplexity(10)
                .maxMethodLength(30)
                .minHealthScore(70.0)
                .build();

        double score = healthScoreService.calculateScore(metrics, threshold);
        assertTrue(score >= 0 && score <= 100, "Score should be between 0-100");
    }

    @Test
    void getGrade_correctGrading() {
        assertEquals("A", healthScoreService.getGrade(95));
        assertEquals("A", healthScoreService.getGrade(90));
        assertEquals("B", healthScoreService.getGrade(85));
        assertEquals("C", healthScoreService.getGrade(75));
        assertEquals("D", healthScoreService.getGrade(65));
        assertEquals("F", healthScoreService.getGrade(50));
        assertEquals("F", healthScoreService.getGrade(0));
    }

    @Test
    void calculateScore_testCoverageBonus() {
        Metrics withTests = Metrics.builder()
                .avgCyclomaticComplexity(5.0)
                .maxMethodLength(20)
                .duplicateBlockCount(0)
                .codeSmellCount(0)
                .totalFiles(10)
                .testCoverageEstimate(80.0)
                .build();

        Metrics withoutTests = Metrics.builder()
                .avgCyclomaticComplexity(5.0)
                .maxMethodLength(20)
                .duplicateBlockCount(0)
                .codeSmellCount(0)
                .totalFiles(10)
                .testCoverageEstimate(20.0)
                .build();

        QualityThreshold threshold = QualityThreshold.builder()
                .maxCyclomaticComplexity(10)
                .maxMethodLength(30)
                .minHealthScore(70.0)
                .build();

        double scoreWithTests = healthScoreService.calculateScore(withTests, threshold);
        double scoreWithoutTests = healthScoreService.calculateScore(withoutTests, threshold);

        assertTrue(scoreWithTests > scoreWithoutTests, "Higher test coverage should give bonus");
    }
}
