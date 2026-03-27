package com.codepulse.analyzer;

import com.codepulse.TestFixtures;
import com.codepulse.model.entity.FileMetrics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JavaAnalyzerTest {

    private JavaAnalyzer analyzer;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        analyzer = new JavaAnalyzer();
    }

    @Test
    void getSupportedLanguage_returnsJava() {
        assertEquals("java", analyzer.getSupportedLanguage());
    }

    @Test
    void analyzeFile_simpleClass_correctMetrics() throws IOException {
        Path file = tempDir.resolve("Simple.java");
        Files.writeString(file, """
                public class Simple {
                    public int getValue() {
                        return 42;
                    }
                }
                """);

        FileMetrics result = analyzer.analyzeFile(file);

        assertEquals("java", result.getLanguage());
        assertTrue(result.getLinesOfCode() > 0);
        assertEquals(1, result.getCyclomaticComplexity()); // base complexity
        assertEquals(1, result.getMethodCount());
        assertFalse(result.getHasLongMethods());
        assertFalse(result.getHasDeepNesting());
    }

    @Test
    void analyzeFile_withBranching_correctComplexity() throws IOException {
        Path file = tempDir.resolve("Branching.java");
        Files.writeString(file, TestFixtures.createSampleJavaCode());

        FileMetrics result = analyzer.analyzeFile(file);

        assertTrue(result.getCyclomaticComplexity() > 1, "Should have complexity > 1 due to if/else");
        assertTrue(result.getMethodCount() >= 3, "Should detect at least 3 methods");
    }

    @Test
    void analyzeFile_emptyFile_nocrash() throws IOException {
        Path file = tempDir.resolve("Empty.java");
        Files.writeString(file, "");

        FileMetrics result = analyzer.analyzeFile(file);

        assertEquals(0, result.getLinesOfCode());
        assertEquals(1, result.getCyclomaticComplexity()); // base
        assertEquals(0, result.getMethodCount());
    }

    @Test
    void countLinesOfCode_excludesComments() {
        List<String> lines = Arrays.asList(
                "package com.example;",
                "",
                "// This is a comment",
                "public class Foo {",
                "    /* block comment */",
                "    int x = 1;",
                "}"
        );

        int loc = analyzer.countLinesOfCode(lines);
        assertEquals(4, loc); // package, class, int x, closing brace
    }

    @Test
    void calculateNestingDepth_deeplyNested() {
        List<String> lines = Arrays.asList(
                "public class Foo {",
                "    public void bar() {",
                "        if (true) {",
                "            for (int i = 0; i < 10; i++) {",
                "                if (i > 5) {",
                "                    // deep",
                "                }",
                "            }",
                "        }",
                "    }",
                "}"
        );

        int depth = analyzer.calculateNestingDepth(lines);
        assertTrue(depth >= 4, "Nesting depth should be at least 4");
    }

    @Test
    void analyzeFile_nonexistent_returnsZeroMetrics() {
        Path file = tempDir.resolve("NonExistent.java");

        FileMetrics result = analyzer.analyzeFile(file);

        assertEquals(0, result.getLinesOfCode());
        assertEquals(0, result.getCyclomaticComplexity());
    }

    @Test
    void calculateCyclomaticComplexity_logicalOperators() {
        List<String> lines = Arrays.asList(
                "if (a > 0 && b > 0 || c > 0) {",
                "    return true;",
                "}"
        );

        int complexity = analyzer.calculateCyclomaticComplexity(lines);
        // base(1) + if(1) + &&(1) + ||(1) = 4
        assertTrue(complexity >= 4, "Should count logical operators");
    }
}
