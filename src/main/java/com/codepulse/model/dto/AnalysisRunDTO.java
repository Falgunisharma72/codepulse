package com.codepulse.model.dto;

import com.codepulse.model.enums.AnalysisStatus;
import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class AnalysisRunDTO {

    private Long id;
    private Long repositoryId;
    private String commitSha;
    private String commitMessage;
    private String author;
    private String branch;
    private AnalysisStatus status;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
    private Double healthScore;
}
