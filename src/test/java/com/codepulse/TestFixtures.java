package com.codepulse;

import com.codepulse.model.entity.*;
import com.codepulse.model.enums.AnalysisStatus;

import java.time.LocalDateTime;

public class TestFixtures {

    public static User createTestUser() {
        return User.builder()
                .id(1L)
                .githubUsername("testuser")
                .accessToken("test-token")
                .createdAt(LocalDateTime.now())
                .build();
    }

    public static Repository createTestRepository(User user) {
        return Repository.builder()
                .id(1L)
                .user(user)
                .githubRepoUrl("https://github.com/testuser/test-repo")
                .repoName("test-repo")
                .defaultBranch("main")
                .webhookSecret("test-secret")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public static AnalysisRun createTestAnalysisRun(Repository repository) {
        return AnalysisRun.builder()
                .id(1L)
                .repository(repository)
                .commitSha("abc1234567890abcdef1234567890abcdef1234")
                .commitMessage("test commit")
                .author("testuser")
                .branch("main")
                .status(AnalysisStatus.QUEUED)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public static Metrics createTestMetrics(AnalysisRun run) {
        return Metrics.builder()
                .id(1L)
                .analysisRun(run)
                .totalFiles(10)
                .totalLines(1000)
                .avgCyclomaticComplexity(5.0)
                .maxCyclomaticComplexity(12.0)
                .avgMethodLength(15.0)
                .maxMethodLength(45)
                .duplicateBlockCount(2)
                .codeSmellCount(3)
                .testFileCount(4)
                .testCoverageEstimate(66.7)
                .overallHealthScore(82.0)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public static QualityThreshold createDefaultThreshold(Repository repository) {
        return QualityThreshold.builder()
                .id(1L)
                .repository(repository)
                .maxCyclomaticComplexity(10)
                .maxMethodLength(30)
                .maxFileLength(300)
                .maxNestingDepth(4)
                .minHealthScore(70.0)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public static FileMetrics createTestFileMetrics(AnalysisRun run) {
        return FileMetrics.builder()
                .id(1L)
                .analysisRun(run)
                .filePath("src/main/java/com/example/Service.java")
                .language("java")
                .linesOfCode(100)
                .cyclomaticComplexity(8)
                .methodCount(5)
                .avgMethodLength(18.0)
                .maxMethodLength(35)
                .hasLongMethods(true)
                .hasDeepNesting(false)
                .nestingDepth(3)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public static String createSampleJavaCode() {
        return """
                package com.example;

                public class Calculator {

                    // Add two numbers
                    public int add(int a, int b) {
                        return a + b;
                    }

                    public int divide(int a, int b) {
                        if (b == 0) {
                            throw new ArithmeticException("Cannot divide by zero");
                        }
                        return a / b;
                    }

                    public String classify(int score) {
                        if (score >= 90) {
                            return "A";
                        } else if (score >= 80) {
                            return "B";
                        } else if (score >= 70) {
                            return "C";
                        } else {
                            return "F";
                        }
                    }
                }
                """;
    }

    public static String createSamplePythonCode() {
        return """
                # Calculator module

                def add(a, b):
                    return a + b

                def classify(score):
                    if score >= 90:
                        return "A"
                    elif score >= 80:
                        return "B"
                    elif score >= 70:
                        return "C"
                    else:
                        return "F"

                def process(items):
                    for item in items:
                        if item > 0 and item < 100:
                            print(item)
                """;
    }
}
