package com.codepulse.repository;

import com.codepulse.model.entity.Alert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {
    List<Alert> findByRepositoryIdAndIsResolvedFalseOrderByCreatedAtDesc(Long repositoryId);
    List<Alert> findByAnalysisRunId(Long analysisRunId);
    long countByIsResolvedFalse();
    long countByRepositoryIdAndIsResolvedFalse(Long repositoryId);

    @Query("SELECT a FROM Alert a WHERE a.isResolved = false ORDER BY a.createdAt DESC")
    List<Alert> findAllActiveAlerts();
}
