package com.codepulse.controller;

import com.codepulse.model.dto.ApiResponse;
import com.codepulse.service.WebhookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/webhooks")
@RequiredArgsConstructor
@Tag(name = "Webhooks", description = "GitHub webhook endpoints")
public class WebhookController {

    private final WebhookService webhookService;

    @PostMapping("/github")
    @Operation(summary = "Handle GitHub push webhook")
    public ResponseEntity<ApiResponse<Map<String, String>>> handleGithubWebhook(
            @RequestHeader(value = "X-Hub-Signature-256", required = false) String signature,
            @RequestHeader("X-GitHub-Event") String event,
            @RequestBody String payload) {

        webhookService.processWebhook(signature, event, payload);
        return ResponseEntity.accepted()
                .body(ApiResponse.ok(Map.of("status", "queued")));
    }
}
