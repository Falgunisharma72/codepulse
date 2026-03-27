package com.codepulse.model.entity;

import com.codepulse.model.enums.AnalysisStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "analysis_runs")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class AnalysisRun {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repository_id")
    private Repository repository;

    @Column(name = "commit_sha", nullable = false, length = 40)
    private String commitSha;

    @Column(name = "commit_message")
    private String commitMessage;

    @Column(name = "author")
    private String author;

    @Column(name = "branch")
    private String branch;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    @Builder.Default
    private AnalysisStatus status = AnalysisStatus.QUEUED;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToOne(mappedBy = "analysisRun", cascade = CascadeType.ALL, orphanRemoval = true)
    private Metrics metrics;

    @OneToMany(mappedBy = "analysisRun", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<FileMetrics> fileMetrics = new ArrayList<>();

    @OneToMany(mappedBy = "analysisRun", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Alert> alerts = new ArrayList<>();
}
