package com.codepulse.model.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "file_metrics")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class FileMetrics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "analysis_run_id")
    private AnalysisRun analysisRun;

    @Column(name = "file_path", nullable = false, length = 1000)
    private String filePath;

    @Column(name = "language", length = 50)
    private String language;

    @Column(name = "lines_of_code")
    private Integer linesOfCode;

    @Column(name = "cyclomatic_complexity")
    private Integer cyclomaticComplexity;

    @Column(name = "method_count")
    private Integer methodCount;

    @Column(name = "avg_method_length")
    private Double avgMethodLength;

    @Column(name = "max_method_length")
    private Integer maxMethodLength;

    @Column(name = "has_long_methods")
    @Builder.Default
    private Boolean hasLongMethods = false;

    @Column(name = "has_deep_nesting")
    @Builder.Default
    private Boolean hasDeepNesting = false;

    @Column(name = "nesting_depth")
    private Integer nestingDepth;

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
