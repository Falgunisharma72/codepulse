package com.codepulse.model.dto;

import com.codepulse.model.enums.AlertType;
import com.codepulse.model.enums.Severity;
import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class AlertDTO {

    private Long id;
    private Long repositoryId;
    private String repoName;
    private Long analysisRunId;
    private AlertType alertType;
    private Severity severity;
    private String message;
    private String filePath;
    private Boolean isResolved;
    private LocalDateTime createdAt;
}
