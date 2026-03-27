package com.codepulse.controller;

import com.codepulse.exception.GlobalExceptionHandler;
import com.codepulse.exception.RepositoryNotFoundException;
import com.codepulse.exception.WebhookVerificationException;
import com.codepulse.service.WebhookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class WebhookControllerTest {

    @Mock
    private WebhookService webhookService;

    @InjectMocks
    private WebhookController webhookController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(webhookController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void handleGithubWebhook_validPush_returns202() throws Exception {
        doNothing().when(webhookService).processWebhook(any(), eq("push"), any());

        mockMvc.perform(post("/api/webhooks/github")
                        .header("X-GitHub-Event", "push")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ref\":\"refs/heads/main\"}"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("queued"));
    }

    @Test
    void handleGithubWebhook_invalidSignature_returns401() throws Exception {
        doThrow(new WebhookVerificationException("Invalid signature"))
                .when(webhookService).processWebhook(any(), eq("push"), any());

        mockMvc.perform(post("/api/webhooks/github")
                        .header("X-GitHub-Event", "push")
                        .header("X-Hub-Signature-256", "sha256=invalid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void handleGithubWebhook_unknownRepo_returns404() throws Exception {
        doThrow(new RepositoryNotFoundException("Not found"))
                .when(webhookService).processWebhook(any(), eq("push"), any());

        mockMvc.perform(post("/api/webhooks/github")
                        .header("X-GitHub-Event", "push")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void handleGithubWebhook_nonPushEvent_returns202() throws Exception {
        doNothing().when(webhookService).processWebhook(any(), eq("star"), any());

        mockMvc.perform(post("/api/webhooks/github")
                        .header("X-GitHub-Event", "star")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isAccepted());
    }
}
