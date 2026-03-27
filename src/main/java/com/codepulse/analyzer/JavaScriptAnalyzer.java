package com.codepulse.analyzer;

import com.codepulse.model.entity.FileMetrics;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Component
@Slf4j
public class JavaScriptAnalyzer implements CodeAnalyzer {

    private static final Pattern FUNCTION_PATTERN = Pattern.compile(
            "(function\\s+\\w+\\s*\\(|\\w+\\s*[:=]\\s*(async\\s+)?function|\\w+\\s*[:=]\\s*(async\\s+)?\\([^)]*\\)\\s*=>|const\\s+\\w+\\s*=\\s*(async\\s+)?\\([^)]*\\)\\s*=>)");
    private static final Pattern COMMENT_PATTERN = Pattern.compile("^\\s*//.*$");
    private static final Pattern BLANK_PATTERN = Pattern.compile("^\\s*$");

    @Override
    public String getSupportedLanguage() {
        return "javascript";
    }

    @Override
    public FileMetrics analyzeFile(Path filePath) {
        try {
            List<String> lines = Files.readAllLines(filePath);
            int linesOfCode = countLinesOfCode(lines);
            int complexity = calculateCyclomaticComplexity(lines);
            int methodCount = countFunctions(lines);
            int nestingDepth = calculateNestingDepth(lines);
            List<MethodInfo> methods = extractMethods(lines);

            double avgMethodLength = methods.isEmpty() ? 0 :
                    methods.stream().mapToInt(m -> m.length).average().orElse(0);
            int maxMethodLength = methods.stream().mapToInt(m -> m.length).max().orElse(0);

            return FileMetrics.builder()
                    .filePath(filePath.toString())
                    .language("javascript")
                    .linesOfCode(linesOfCode)
                    .cyclomaticComplexity(complexity)
                    .methodCount(methodCount)
                    .avgMethodLength(avgMethodLength)
                    .maxMethodLength(maxMethodLength)
                    .hasLongMethods(maxMethodLength > 30)
                    .hasDeepNesting(nestingDepth > 4)
                    .nestingDepth(nestingDepth)
                    .build();
        } catch (IOException e) {
            log.error("Failed to analyze JavaScript file: {}", filePath, e);
            return FileMetrics.builder()
                    .filePath(filePath.toString())
                    .language("javascript")
                    .linesOfCode(0)
                    .cyclomaticComplexity(0)
                    .methodCount(0)
                    .avgMethodLength(0.0)
                    .maxMethodLength(0)
                    .nestingDepth(0)
                    .build();
        }
    }

    int countLinesOfCode(List<String> lines) {
        boolean inBlockComment = false;
        int count = 0;

        for (String line : lines) {
            String trimmed = line.trim();

            if (inBlockComment) {
                if (trimmed.contains("*/")) inBlockComment = false;
                continue;
            }
            if (trimmed.startsWith("/*")) {
                inBlockComment = true;
                if (trimmed.contains("*/")) inBlockComment = false;
                continue;
            }
            if (!BLANK_PATTERN.matcher(line).matches() && !COMMENT_PATTERN.matcher(line).matches()) {
                count++;
            }
        }
        return count;
    }

    int calculateCyclomaticComplexity(List<String> lines) {
        int complexity = 1;

        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.startsWith("//") || trimmed.startsWith("*")) continue;

            if (trimmed.matches(".*\\bif\\s*\\(.*")) complexity++;
            if (trimmed.matches(".*\\belse\\s+if\\s*\\(.*")) { /* already counted */ }
            if (trimmed.matches(".*\\bfor\\s*\\(.*")) complexity++;
            if (trimmed.matches(".*\\bwhile\\s*\\(.*")) complexity++;
            if (trimmed.matches(".*\\bcase\\s+.*:.*")) complexity++;
            if (trimmed.matches(".*\\bcatch\\s*\\(.*")) complexity++;

            long andCount = countOccurrences(trimmed, "&&");
            long orCount = countOccurrences(trimmed, "||");
            complexity += (int) (andCount + orCount);

            if (trimmed.contains("?") && trimmed.contains(":") && !trimmed.matches(".*\\bcase\\s+.*:.*")) {
                complexity++;
            }

            // Promise chains and callbacks add complexity
            if (trimmed.contains(".then(")) complexity++;
            if (trimmed.contains(".catch(")) complexity++;
        }

        return complexity;
    }

    int calculateNestingDepth(List<String> lines) {
        int currentDepth = 0;
        int maxDepth = 0;

        for (String line : lines) {
            for (char c : line.toCharArray()) {
                if (c == '{') {
                    currentDepth++;
                    maxDepth = Math.max(maxDepth, currentDepth);
                } else if (c == '}') {
                    currentDepth--;
                }
            }
        }
        return maxDepth;
    }

    int countFunctions(List<String> lines) {
        int count = 0;
        for (String line : lines) {
            if (FUNCTION_PATTERN.matcher(line).find()) count++;
        }
        return count;
    }

    List<MethodInfo> extractMethods(List<String> lines) {
        List<MethodInfo> methods = new ArrayList<>();
        int braceCount = 0;
        int methodStart = -1;
        boolean inMethod = false;

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);

            if (!inMethod && FUNCTION_PATTERN.matcher(line).find()) {
                methodStart = i;
                inMethod = true;
                braceCount = 0;
            }

            if (inMethod) {
                for (char c : line.toCharArray()) {
                    if (c == '{') braceCount++;
                    else if (c == '}') braceCount--;
                }

                if (braceCount == 0 && methodStart != i) {
                    methods.add(new MethodInfo(methodStart, i - methodStart + 1));
                    inMethod = false;
                }
            }
        }
        return methods;
    }

    private long countOccurrences(String text, String target) {
        long count = 0;
        int index = 0;
        while ((index = text.indexOf(target, index)) != -1) {
            count++;
            index += target.length();
        }
        return count;
    }

    static class MethodInfo {
        final int startLine;
        final int length;
        MethodInfo(int startLine, int length) {
            this.startLine = startLine;
            this.length = length;
        }
    }
}
