package com.codepulse.analyzer;

import com.codepulse.model.entity.FileMetrics;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Slf4j
public class JavaAnalyzer implements CodeAnalyzer {

    private static final Pattern METHOD_PATTERN = Pattern.compile(
            "\\b(public|private|protected|static|final|abstract|synchronized|native)\\s+" +
            ".*\\s+\\w+\\s*\\([^)]*\\)\\s*(throws\\s+\\w+[^{]*)?\\{");

    private static final Pattern SINGLE_LINE_COMMENT = Pattern.compile("^\\s*//.*$");
    private static final Pattern BLANK_LINE = Pattern.compile("^\\s*$");

    @Override
    public String getSupportedLanguage() {
        return "java";
    }

    @Override
    public FileMetrics analyzeFile(Path filePath) {
        try {
            List<String> lines = Files.readAllLines(filePath);
            int linesOfCode = countLinesOfCode(lines);
            int cyclomaticComplexity = calculateCyclomaticComplexity(lines);
            List<MethodInfo> methods = extractMethods(lines);
            int nestingDepth = calculateNestingDepth(lines);

            int methodCount = methods.size();
            double avgMethodLength = methods.isEmpty() ? 0 :
                    methods.stream().mapToInt(m -> m.length).average().orElse(0);
            int maxMethodLength = methods.stream().mapToInt(m -> m.length).max().orElse(0);

            return FileMetrics.builder()
                    .filePath(filePath.toString())
                    .language("java")
                    .linesOfCode(linesOfCode)
                    .cyclomaticComplexity(cyclomaticComplexity)
                    .methodCount(methodCount)
                    .avgMethodLength(avgMethodLength)
                    .maxMethodLength(maxMethodLength)
                    .hasLongMethods(maxMethodLength > 30)
                    .hasDeepNesting(nestingDepth > 4)
                    .nestingDepth(nestingDepth)
                    .build();
        } catch (IOException e) {
            log.error("Failed to analyze file: {}", filePath, e);
            return FileMetrics.builder()
                    .filePath(filePath.toString())
                    .language("java")
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
                if (trimmed.contains("*/")) {
                    inBlockComment = false;
                }
                continue;
            }

            if (trimmed.startsWith("/*")) {
                inBlockComment = true;
                if (trimmed.contains("*/")) {
                    inBlockComment = false;
                }
                continue;
            }

            if (!BLANK_LINE.matcher(line).matches() && !SINGLE_LINE_COMMENT.matcher(line).matches()) {
                count++;
            }
        }
        return count;
    }

    int calculateCyclomaticComplexity(List<String> lines) {
        int complexity = 1;

        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.startsWith("//") || trimmed.startsWith("*") || trimmed.startsWith("/*")) {
                continue;
            }

            if (trimmed.matches(".*\\bif\\s*\\(.*") || trimmed.matches(".*\\bif\\(.*")) complexity++;
            if (trimmed.matches(".*\\belse\\s+if\\s*\\(.*")) complexity--; // undo double count, re-add below
            if (trimmed.matches(".*\\belse\\s+if\\s*\\(.*")) complexity++;
            if (trimmed.matches(".*\\bfor\\s*\\(.*")) complexity++;
            if (trimmed.matches(".*\\bwhile\\s*\\(.*") && !trimmed.matches(".*\\bdo\\b.*")) complexity++;
            if (trimmed.matches(".*\\bdo\\b.*")) complexity++;
            if (trimmed.matches(".*\\bcase\\s+.*:.*")) complexity++;
            if (trimmed.matches(".*\\bcatch\\s*\\(.*")) complexity++;

            // Count logical operators
            long andCount = countOccurrences(trimmed, "&&");
            long orCount = countOccurrences(trimmed, "||");
            complexity += (int) (andCount + orCount);

            // Ternary operator
            if (trimmed.contains("?") && trimmed.contains(":") && !trimmed.matches(".*\\bcase\\s+.*:.*")) {
                complexity++;
            }
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

        // Subtract 1 for class-level brace
        return Math.max(0, maxDepth - 1);
    }

    List<MethodInfo> extractMethods(List<String> lines) {
        List<MethodInfo> methods = new ArrayList<>();
        int braceCount = 0;
        int methodStart = -1;
        boolean inMethod = false;

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);

            if (!inMethod && METHOD_PATTERN.matcher(line).find()) {
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
                    int length = i - methodStart + 1;
                    methods.add(new MethodInfo(methodStart, length));
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
