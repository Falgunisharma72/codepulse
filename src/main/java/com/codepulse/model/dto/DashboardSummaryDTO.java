package com.codepulse.model.dto;

import lombok.*;
import java.util.List;
import java.util.Map;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class DashboardSummaryDTO {

    private Long totalRepos;
    private Long totalAnalyses;
    private Double avgHealthScore;
    private Long activeAlerts;
    private List<AnalysisRunDTO> recentAnalyses;
    private Map<String, Long> healthDistribution;
}
