package com.codepulse.analyzer;

import com.codepulse.TestFixtures;
import com.codepulse.model.entity.FileMetrics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class PythonAnalyzerTest {

    private PythonAnalyzer analyzer;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        analyzer = new PythonAnalyzer();
    }

    @Test
    void getSupportedLanguage_returnsPython() {
        assertEquals("python", analyzer.getSupportedLanguage());
    }

    @Test
    void analyzeFile_samplePythonCode_correctMetrics() throws IOException {
        Path file = tempDir.resolve("calculator.py");
        Files.writeString(file, TestFixtures.createSamplePythonCode());

        FileMetrics result = analyzer.analyzeFile(file);

        assertEquals("python", result.getLanguage());
        assertTrue(result.getLinesOfCode() > 0);
        assertTrue(result.getCyclomaticComplexity() > 1);
        assertTrue(result.getMethodCount() >= 3);
    }

    @Test
    void analyzeFile_emptyFile_nocrash() throws IOException {
        Path file = tempDir.resolve("empty.py");
        Files.writeString(file, "");

        FileMetrics result = analyzer.analyzeFile(file);

        assertEquals(0, result.getLinesOfCode());
        assertEquals(1, result.getCyclomaticComplexity());
        assertEquals(0, result.getMethodCount());
    }

    @Test
    void analyzeFile_nonexistent_returnsZeroMetrics() {
        Path file = tempDir.resolve("nonexistent.py");

        FileMetrics result = analyzer.analyzeFile(file);

        assertEquals(0, result.getLinesOfCode());
    }

    @Test
    void analyzeFile_deepNesting_flagged() throws IOException {
        Path file = tempDir.resolve("nested.py");
        Files.writeString(file, """
                def process():
                    for i in range(10):
                        for j in range(10):
                            for k in range(10):
                                if i > 0:
                                    if j > 0:
                                        print(i, j, k)
                """);

        FileMetrics result = analyzer.analyzeFile(file);
        assertTrue(result.getHasDeepNesting(), "Should flag deep nesting");
    }
}
