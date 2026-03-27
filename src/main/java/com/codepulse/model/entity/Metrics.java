package com.codepulse.model.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "metrics")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Metrics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "analysis_run_id")
    private AnalysisRun analysisRun;

    @Column(name = "total_files")
    private Integer totalFiles;

    @Column(name = "total_lines")
    private Integer totalLines;

    @Column(name = "avg_cyclomatic_complexity")
    private Double avgCyclomaticComplexity;

    @Column(name = "max_cyclomatic_complexity")
    private Double maxCyclomaticComplexity;

    @Column(name = "avg_method_length")
    private Double avgMethodLength;

    @Column(name = "max_method_length")
    private Integer maxMethodLength;

    @Column(name = "duplicate_block_count")
    private Integer duplicateBlockCount;

    @Column(name = "code_smell_count")
    private Integer codeSmellCount;

    @Column(name = "test_file_count")
    private Integer testFileCount;

    @Column(name = "test_coverage_estimate")
    private Double testCoverageEstimate;

    @Column(name = "overall_health_score")
    private Double overallHealthScore;

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
