package com.codepulse.model.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "quality_thresholds")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class QualityThreshold {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repository_id", unique = true)
    private Repository repository;

    @Column(name = "max_cyclomatic_complexity")
    @Builder.Default
    private Integer maxCyclomaticComplexity = 10;

    @Column(name = "max_method_length")
    @Builder.Default
    private Integer maxMethodLength = 30;

    @Column(name = "max_file_length")
    @Builder.Default
    private Integer maxFileLength = 300;

    @Column(name = "max_nesting_depth")
    @Builder.Default
    private Integer maxNestingDepth = 4;

    @Column(name = "min_health_score")
    @Builder.Default
    private Double minHealthScore = 70.0;

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
