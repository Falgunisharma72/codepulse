package com.codepulse.model.entity;

import com.codepulse.model.enums.AlertType;
import com.codepulse.model.enums.Severity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "alerts")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Alert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "analysis_run_id")
    private AnalysisRun analysisRun;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repository_id")
    private Repository repository;

    @Enumerated(EnumType.STRING)
    @Column(name = "alert_type", length = 50)
    private AlertType alertType;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity", length = 20)
    private Severity severity;

    @Column(name = "message")
    private String message;

    @Column(name = "file_path", length = 1000)
    private String filePath;

    @Column(name = "is_resolved")
    @Builder.Default
    private Boolean isResolved = false;

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
