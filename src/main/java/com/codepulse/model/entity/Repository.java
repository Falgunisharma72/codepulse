package com.codepulse.model.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "repositories", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "github_repo_url"})
})
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Repository {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "github_repo_url", nullable = false, length = 500)
    private String githubRepoUrl;

    @Column(name = "repo_name", nullable = false)
    private String repoName;

    @Column(name = "default_branch")
    @Builder.Default
    private String defaultBranch = "main";

    @Column(name = "webhook_secret")
    private String webhookSecret;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "repository", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<AnalysisRun> analysisRuns = new ArrayList<>();

    @OneToOne(mappedBy = "repository", cascade = CascadeType.ALL, orphanRemoval = true)
    private QualityThreshold qualityThreshold;
}
