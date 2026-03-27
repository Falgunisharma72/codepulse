package com.codepulse.service;

import com.codepulse.model.entity.FileMetrics;
import com.codepulse.model.entity.Metrics;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

@Service
public class MetricsCalculatorService {

    private static final int DUPLICATE_BLOCK_SIZE = 6;

    public Metrics aggregate(List<FileMetrics> fileMetricsList, List<String> allFileContents) {
        if (fileMetricsList.isEmpty()) {
            return Metrics.builder()
                    .totalFiles(0)
                    .totalLines(0)
                    .avgCyclomaticComplexity(0.0)
                    .maxCyclomaticComplexity(0.0)
                    .avgMethodLength(0.0)
                    .maxMethodLength(0)
                    .duplicateBlockCount(0)
                    .codeSmellCount(0)
                    .testFileCount(0)
                    .testCoverageEstimate(0.0)
                    .overallHealthScore(0.0)
                    .build();
        }

        int totalFiles = fileMetricsList.size();
        int totalLines = fileMetricsList.stream().mapToInt(FileMetrics::getLinesOfCode).sum();

        double avgComplexity = fileMetricsList.stream()
                .mapToInt(FileMetrics::getCyclomaticComplexity).average().orElse(0);
        double maxComplexity = fileMetricsList.stream()
                .mapToInt(FileMetrics::getCyclomaticComplexity).max().orElse(0);

        double avgMethodLength = fileMetricsList.stream()
                .filter(fm -> fm.getAvgMethodLength() != null && fm.getAvgMethodLength() > 0)
                .mapToDouble(FileMetrics::getAvgMethodLength).average().orElse(0);
        int maxMethodLength = fileMetricsList.stream()
                .mapToInt(fm -> fm.getMaxMethodLength() != null ? fm.getMaxMethodLength() : 0).max().orElse(0);

        int duplicateBlockCount = detectDuplicates(allFileContents);

        int codeSmellCount = (int) fileMetricsList.stream()
                .filter(fm -> fm.getHasLongMethods() || fm.getHasDeepNesting() || fm.getCyclomaticComplexity() > 10)
                .count();

        int testFileCount = (int) fileMetricsList.stream()
                .filter(this::isTestFile)
                .count();
        int sourceFileCount = totalFiles - testFileCount;
        double testCoverageEstimate = sourceFileCount > 0 ?
                Math.min(100.0, (double) testFileCount / sourceFileCount * 100) : 0;

        return Metrics.builder()
                .totalFiles(totalFiles)
                .totalLines(totalLines)
                .avgCyclomaticComplexity(Math.round(avgComplexity * 100.0) / 100.0)
                .maxCyclomaticComplexity(maxComplexity)
                .avgMethodLength(Math.round(avgMethodLength * 100.0) / 100.0)
                .maxMethodLength(maxMethodLength)
                .duplicateBlockCount(duplicateBlockCount)
                .codeSmellCount(codeSmellCount)
                .testFileCount(testFileCount)
                .testCoverageEstimate(Math.round(testCoverageEstimate * 100.0) / 100.0)
                .build();
    }

    int detectDuplicates(List<String> fileContents) {
        Set<String> blockHashes = new HashSet<>();
        int duplicateCount = 0;

        for (String content : fileContents) {
            String[] lines = content.split("\n");
            List<String> normalized = new ArrayList<>();
            for (String line : lines) {
                String trimmed = line.trim().toLowerCase();
                if (!trimmed.isEmpty()) {
                    normalized.add(trimmed);
                }
            }

            for (int i = 0; i <= normalized.size() - DUPLICATE_BLOCK_SIZE; i++) {
                StringBuilder block = new StringBuilder();
                for (int j = i; j < i + DUPLICATE_BLOCK_SIZE; j++) {
                    block.append(normalized.get(j));
                }
                String hash = sha256(block.toString());
                if (!blockHashes.add(hash)) {
                    duplicateCount++;
                }
            }
        }

        return duplicateCount;
    }

    private String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    private boolean isTestFile(FileMetrics fm) {
        String path = fm.getFilePath().toLowerCase();
        return path.contains("test") || path.contains("spec") ||
               path.endsWith("test.java") || path.endsWith("test.js") ||
               path.endsWith("test.py") || path.endsWith("spec.js") ||
               path.startsWith("test_");
    }
}
