package com.codepulse.model.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class TrendDataDTO {

    private Long repositoryId;
    private List<TrendPoint> dataPoints;

    @Getter @Setter
    @NoArgsConstructor @AllArgsConstructor
    @Builder
    public static class TrendPoint {
        private LocalDateTime timestamp;
        private String commitSha;
        private Double healthScore;
        private Double avgComplexity;
        private Integer totalLines;
        private Integer codeSmellCount;
        private Integer totalFiles;
    }
}
