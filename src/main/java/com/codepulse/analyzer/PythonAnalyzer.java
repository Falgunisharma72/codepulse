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
public class PythonAnalyzer implements CodeAnalyzer {

    private static final Pattern DEF_PATTERN = Pattern.compile("^\\s*def\\s+\\w+\\s*\\(");
    private static final Pattern COMMENT_PATTERN = Pattern.compile("^\\s*#.*$");
    private static final Pattern BLANK_PATTERN = Pattern.compile("^\\s*$");

    @Override
    public String getSupportedLanguage() {
        return "python";
    }

    @Override
    public FileMetrics analyzeFile(Path filePath) {
        try {
            List<String> lines = Files.readAllLines(filePath);
            int linesOfCode = countLinesOfCode(lines);
            int complexity = calculateCyclomaticComplexity(lines);
            List<MethodInfo> methods = extractMethods(lines);
            int nestingDepth = calculateNestingDepth(lines);

            int methodCount = methods.size();
            double avgMethodLength = methods.isEmpty() ? 0 :
                    methods.stream().mapToInt(m -> m.length).average().orElse(0);
            int maxMethodLength = methods.stream().mapToInt(m -> m.length).max().orElse(0);

            return FileMetrics.builder()
                    .filePath(filePath.toString())
                    .language("python")
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
            log.error("Failed to analyze Python file: {}", filePath, e);
            return FileMetrics.builder()
                    .filePath(filePath.toString())
                    .language("python")
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
        boolean inDocstring = false;
        int count = 0;

        for (String line : lines) {
            String trimmed = line.trim();

            if (trimmed.startsWith("\"\"\"") || trimmed.startsWith("'''")) {
                long quoteCount = trimmed.chars().filter(c -> c == '"').count();
                if (quoteCount >= 6 || trimmed.endsWith("\"\"\"") && trimmed.length() > 3) {
                    continue; // Single-line docstring
                }
                inDocstring = !inDocstring;
                continue;
            }

            if (inDocstring) continue;
            if (BLANK_PATTERN.matcher(line).matches()) continue;
            if (COMMENT_PATTERN.matcher(line).matches()) continue;

            count++;
        }
        return count;
    }

    int calculateCyclomaticComplexity(List<String> lines) {
        int complexity = 1;

        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.startsWith("#")) continue;

            if (trimmed.matches(".*\\bif\\b.*:.*")) complexity++;
            if (trimmed.matches(".*\\belif\\b.*:.*")) complexity++;
            if (trimmed.matches(".*\\bfor\\b.*:.*")) complexity++;
            if (trimmed.matches(".*\\bwhile\\b.*:.*")) complexity++;
            if (trimmed.matches(".*\\bexcept\\b.*:.*")) complexity++;

            long andCount = countOccurrences(trimmed, " and ");
            long orCount = countOccurrences(trimmed, " or ");
            complexity += (int) (andCount + orCount);

            // List/dict/set comprehensions with conditions
            if (trimmed.contains(" if ") && (trimmed.contains("[") || trimmed.contains("("))) {
                complexity++;
            }
        }

        return complexity;
    }

    int calculateNestingDepth(List<String> lines) {
        int maxDepth = 0;

        for (String line : lines) {
            if (BLANK_PATTERN.matcher(line).matches()) continue;
            int spaces = 0;
            for (char c : line.toCharArray()) {
                if (c == ' ') spaces++;
                else if (c == '\t') spaces += 4;
                else break;
            }
            int depth = spaces / 4;
            maxDepth = Math.max(maxDepth, depth);
        }

        return maxDepth;
    }

    List<MethodInfo> extractMethods(List<String> lines) {
        List<MethodInfo> methods = new ArrayList<>();

        for (int i = 0; i < lines.size(); i++) {
            if (DEF_PATTERN.matcher(lines.get(i)).find()) {
                int indent = getIndentation(lines.get(i));
                int end = i + 1;
                while (end < lines.size()) {
                    String nextLine = lines.get(end);
                    if (!BLANK_PATTERN.matcher(nextLine).matches() && getIndentation(nextLine) <= indent) {
                        break;
                    }
                    end++;
                }
                methods.add(new MethodInfo(i, end - i));
            }
        }

        return methods;
    }

    private int getIndentation(String line) {
        int spaces = 0;
        for (char c : line.toCharArray()) {
            if (c == ' ') spaces++;
            else if (c == '\t') spaces += 4;
            else break;
        }
        return spaces;
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
