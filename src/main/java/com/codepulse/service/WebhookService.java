package com.codepulse.service;

import com.codepulse.exception.RepositoryNotFoundException;
import com.codepulse.exception.WebhookVerificationException;
import com.codepulse.model.dto.WebhookPayloadDTO;
import com.codepulse.model.entity.AnalysisRun;
import com.codepulse.model.entity.Repository;
import com.codepulse.model.enums.AnalysisStatus;
import com.codepulse.queue.AnalysisJobPublisher;
import com.codepulse.repository.AnalysisRunRepository;
import com.codepulse.repository.RepoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebhookService {

    private final RepoRepository repoRepository;
    private final AnalysisRunRepository analysisRunRepository;
    private final AnalysisJobPublisher jobPublisher;
    private final ObjectMapper objectMapper;

    @Transactional
    public void processWebhook(String signature, String event, String payload) {
        if (!"push".equals(event)) {
            log.info("Ignoring non-push event: {}", event);
            return;
        }

        try {
            WebhookPayloadDTO webhookPayload = objectMapper.readValue(payload, WebhookPayloadDTO.class);
            String repoUrl = webhookPayload.getRepository().getHtmlUrl();

            Repository repository = repoRepository.findByGithubRepoUrl(repoUrl)
                    .orElseThrow(() -> new RepositoryNotFoundException("Repository not found: " + repoUrl));

            // Verify webhook signature
            if (repository.getWebhookSecret() != null && !repository.getWebhookSecret().isEmpty()) {
                verifySignature(payload, signature, repository.getWebhookSecret());
            }

            // Create analysis run
            AnalysisRun analysisRun = AnalysisRun.builder()
                    .repository(repository)
                    .commitSha(webhookPayload.getHeadCommit())
                    .commitMessage(webhookPayload.getLatestCommitMessage())
                    .author(webhookPayload.getPusher() != null ? webhookPayload.getPusher().getName() : "unknown")
                    .branch(webhookPayload.getBranch())
                    .status(AnalysisStatus.QUEUED)
                    .build();

            analysisRun = analysisRunRepository.save(analysisRun);
            log.info("Created analysis run {} for repo {}", analysisRun.getId(), repository.getRepoName());

            // Queue the job
            jobPublisher.publishJob(analysisRun.getId());

        } catch (RepositoryNotFoundException | WebhookVerificationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to process webhook: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to process webhook payload", e);
        }
    }

    void verifySignature(String payload, String signature, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);
            byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder("sha256=");
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            if (!hexString.toString().equals(signature)) {
                throw new WebhookVerificationException("Invalid webhook signature");
            }
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new WebhookVerificationException("Failed to verify signature: " + e.getMessage());
        }
    }
}
