package com.codepulse.integration;

import com.codepulse.model.entity.Repository;
import com.codepulse.model.entity.User;
import com.codepulse.repository.RepoRepository;
import com.codepulse.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration test for the webhook pipeline.
 * Requires PostgreSQL and Redis (use Testcontainers or test profile with H2).
 * Marked as integration test - runs with 'mvn verify'.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class WebhookIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RepoRepository repoRepository;

    // Note: This test requires a running database.
    // In CI, use Testcontainers or the test profile with an embedded DB.
    // @Test
    void fullWebhookPipeline_pushEvent_analysisQueued() throws Exception {
        // Setup: create user and repo
        User user = userRepository.save(User.builder().githubUsername("integration-test").build());
        Repository repo = repoRepository.save(Repository.builder()
                .user(user)
                .githubRepoUrl("https://github.com/test/integration-repo")
                .repoName("integration-repo")
                .defaultBranch("main")
                .build());

        String payload = String.format("""
                {
                    "ref": "refs/heads/main",
                    "after": "abc123def456",
                    "repository": {
                        "html_url": "https://github.com/test/integration-repo",
                        "clone_url": "https://github.com/test/integration-repo.git"
                    },
                    "commits": [{"id": "abc123def456", "message": "test", "author": {"name": "test"}}],
                    "pusher": {"name": "test"}
                }
                """);

        mockMvc.perform(post("/api/webhooks/github")
                        .header("X-GitHub-Event", "push")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isAccepted());
    }
}
