package com.codepulse.analyzer;

import com.codepulse.model.entity.FileMetrics;
import java.nio.file.Path;

public interface CodeAnalyzer {
    String getSupportedLanguage();
    FileMetrics analyzeFile(Path filePath);
}
