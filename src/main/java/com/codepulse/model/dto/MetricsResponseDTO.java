package com.codepulse.model.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class MetricsResponseDTO {

    private Long id;
    private Long analysisRunId;
    private Integer totalFiles;
    private Integer totalLines;
    private Double avgCyclomaticComplexity;
    private Double maxCyclomaticComplexity;
    private Double avgMethodLength;
    private Integer maxMethodLength;
    private Integer duplicateBlockCount;
    private Integer codeSmellCount;
    private Integer testFileCount;
    private Double testCoverageEstimate;
    private Double overallHealthScore;
    private String healthGrade;
    private LocalDateTime createdAt;

    // Deltas compared to previous run
    private Double healthScoreDelta;
    private Integer codeSmellCountDelta;
    private Double complexityDelta;
}
