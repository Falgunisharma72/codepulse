package com.codepulse.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

@Service
@Slf4j
public class GitCloneService {

    @Value("${codepulse.analysis.clone-dir:/tmp/codepulse-repos}")
    private String cloneDir;

    @Value("${codepulse.analysis.timeout-seconds:300}")
    private int timeoutSeconds;

    public Path cloneRepository(String repoUrl, String commitSha, Long runId, String accessToken) throws IOException, InterruptedException {
        Path targetDir = Path.of(cloneDir, "run-" + runId);
        Files.createDirectories(targetDir);

        String cloneUrl = repoUrl;
        if (accessToken != null && !accessToken.isEmpty()) {
            cloneUrl = repoUrl.replace("https://", "https://" + accessToken + "@");
        }

        // Shallow clone for speed
        ProcessBuilder pb = new ProcessBuilder(
                "git", "clone", "--depth", "1", "--single-branch", cloneUrl, targetDir.toString()
        );
        pb.redirectErrorStream(true);
        Process process = pb.start();

        boolean finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);
        if (!finished) {
            process.destroyForcibly();
            throw new IOException("Git clone timed out after " + timeoutSeconds + " seconds");
        }

        if (process.exitValue() != 0) {
            String output = new String(process.getInputStream().readAllBytes());
            throw new IOException("Git clone failed: " + output);
        }

        // Checkout specific commit if provided
        if (commitSha != null && !commitSha.isEmpty()) {
            ProcessBuilder checkoutPb = new ProcessBuilder("git", "checkout", commitSha);
            checkoutPb.directory(targetDir.toFile());
            checkoutPb.redirectErrorStream(true);
            Process checkoutProcess = checkoutPb.start();
            checkoutProcess.waitFor(30, TimeUnit.SECONDS);
        }

        log.info("Cloned repository to: {}", targetDir);
        return targetDir;
    }

    public void cleanup(Path directory) {
        if (directory == null || !Files.exists(directory)) return;

        try (Stream<Path> walk = Files.walk(directory)) {
            walk.sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException e) {
                            log.warn("Failed to delete: {}", path);
                        }
                    });
            log.info("Cleaned up directory: {}", directory);
        } catch (IOException e) {
            log.error("Failed to cleanup directory: {}", directory, e);
        }
    }
}
