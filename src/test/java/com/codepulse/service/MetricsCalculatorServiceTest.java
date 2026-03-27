package com.codepulse.service;

import com.codepulse.model.entity.FileMetrics;
import com.codepulse.model.entity.Metrics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MetricsCalculatorServiceTest {

    private MetricsCalculatorService calculator;

    @BeforeEach
    void setUp() {
        calculator = new MetricsCalculatorService();
    }

    @Test
    void aggregate_emptyList_zeroMetrics() {
        Metrics result = calculator.aggregate(Collections.emptyList(), Collections.emptyList());

        assertEquals(0, result.getTotalFiles());
        assertEquals(0, result.getTotalLines());
        assertEquals(0.0, result.getAvgCyclomaticComplexity());
    }

    @Test
    void aggregate_singleFile_matchesFileMetrics() {
        FileMetrics fm = FileMetrics.builder()
                .filePath("src/Main.java")
                .language("java")
                .linesOfCode(100)
                .cyclomaticComplexity(5)
                .methodCount(3)
                .avgMethodLength(20.0)
                .maxMethodLength(30)
                .hasLongMethods(false)
                .hasDeepNesting(false)
                .nestingDepth(2)
                .build();

        Metrics result = calculator.aggregate(List.of(fm), List.of("some content"));

        assertEquals(1, result.getTotalFiles());
        assertEquals(100, result.getTotalLines());
        assertEquals(5.0, result.getAvgCyclomaticComplexity());
    }

    @Test
    void aggregate_multipleFiles_correctAverages() {
        FileMetrics fm1 = FileMetrics.builder()
                .filePath("src/A.java")
                .language("java")
                .linesOfCode(50)
                .cyclomaticComplexity(4)
                .methodCount(2)
                .avgMethodLength(10.0)
                .maxMethodLength(15)
                .hasLongMethods(false)
                .hasDeepNesting(false)
                .nestingDepth(1)
                .build();

        FileMetrics fm2 = FileMetrics.builder()
                .filePath("src/B.java")
                .language("java")
                .linesOfCode(150)
                .cyclomaticComplexity(10)
                .methodCount(5)
                .avgMethodLength(25.0)
                .maxMethodLength(50)
                .hasLongMethods(true)
                .hasDeepNesting(true)
                .nestingDepth(6)
                .build();

        Metrics result = calculator.aggregate(List.of(fm1, fm2), List.of("content1", "content2"));

        assertEquals(2, result.getTotalFiles());
        assertEquals(200, result.getTotalLines());
        assertEquals(7.0, result.getAvgCyclomaticComplexity());
        assertEquals(50, result.getMaxMethodLength());
        assertEquals(1, result.getCodeSmellCount()); // fm2 has long methods + deep nesting + complexity > 10
    }

    @Test
    void aggregate_testFilesIdentified() {
        FileMetrics source = FileMetrics.builder()
                .filePath("src/Service.java")
                .language("java")
                .linesOfCode(100)
                .cyclomaticComplexity(5)
                .methodCount(3)
                .avgMethodLength(20.0)
                .maxMethodLength(30)
                .hasLongMethods(false)
                .hasDeepNesting(false)
                .nestingDepth(2)
                .build();

        FileMetrics test = FileMetrics.builder()
                .filePath("src/ServiceTest.java")
                .language("java")
                .linesOfCode(80)
                .cyclomaticComplexity(2)
                .methodCount(5)
                .avgMethodLength(10.0)
                .maxMethodLength(15)
                .hasLongMethods(false)
                .hasDeepNesting(false)
                .nestingDepth(1)
                .build();

        Metrics result = calculator.aggregate(List.of(source, test), List.of("a", "b"));

        assertEquals(1, result.getTestFileCount());
        assertTrue(result.getTestCoverageEstimate() > 0);
    }

    @Test
    void detectDuplicates_noDuplicates_returnsZero() {
        int count = calculator.detectDuplicates(List.of(
                "line1\nline2\nline3\nline4\nline5\nline6\nline7",
                "lineA\nlineB\nlineC\nlineD\nlineE\nlineF\nlineG"
        ));
        assertEquals(0, count);
    }

    @Test
    void detectDuplicates_withDuplicates_countsCorrectly() {
        String sharedBlock = "int x = 1;\nint y = 2;\nint z = 3;\nint w = 4;\nint v = 5;\nint u = 6;\n";
        int count = calculator.detectDuplicates(List.of(
                "// file 1\n" + sharedBlock,
                "// file 2\n" + sharedBlock
        ));
        assertTrue(count > 0, "Should detect duplicate blocks");
    }
}
