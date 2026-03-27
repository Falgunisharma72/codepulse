package com.codepulse.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class RepositoryDTO {

    private Long id;

    @NotBlank(message = "GitHub repo URL is required")
    private String githubRepoUrl;

    @NotBlank(message = "Repository name is required")
    private String repoName;

    private String defaultBranch;
    private String webhookSecret;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private Double latestHealthScore;
    private String latestHealthGrade;
    private Long activeAlertCount;
    private LocalDateTime lastAnalysisAt;
}
