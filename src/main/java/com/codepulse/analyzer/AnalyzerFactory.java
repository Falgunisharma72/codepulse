package com.codepulse.analyzer;

import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class AnalyzerFactory {

    private final Map<String, CodeAnalyzer> analyzers = new HashMap<>();

    private static final Map<String, String> EXTENSION_TO_LANGUAGE = Map.of(
            "java", "java",
            "py", "python",
            "js", "javascript",
            "jsx", "javascript",
            "ts", "javascript",
            "tsx", "javascript"
    );

    public AnalyzerFactory(List<CodeAnalyzer> analyzerList) {
        for (CodeAnalyzer analyzer : analyzerList) {
            analyzers.put(analyzer.getSupportedLanguage(), analyzer);
        }
    }

    public CodeAnalyzer getAnalyzer(String language) {
        return analyzers.get(language);
    }

    public String detectLanguage(Path filePath) {
        String fileName = filePath.getFileName().toString();
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex == -1) return null;

        String extension = fileName.substring(dotIndex + 1).toLowerCase();
        return EXTENSION_TO_LANGUAGE.get(extension);
    }

    public boolean isSupported(Path filePath) {
        return detectLanguage(filePath) != null;
    }
}
