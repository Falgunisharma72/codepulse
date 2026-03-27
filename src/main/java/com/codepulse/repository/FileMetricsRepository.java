package com.codepulse.repository;

import com.codepulse.model.entity.FileMetrics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FileMetricsRepository extends JpaRepository<FileMetrics, Long> {
    List<FileMetrics> findByAnalysisRunId(Long analysisRunId);

    @Query("SELECT fm FROM FileMetrics fm JOIN fm.analysisRun ar WHERE ar.repository.id = :repoId " +
           "AND ar.status = 'COMPLETED' ORDER BY ar.createdAt DESC, fm.cyclomaticComplexity DESC")
    List<FileMetrics> findLatestFileMetricsByRepositoryId(@Param("repoId") Long repoId);

    @Query("SELECT fm FROM FileMetrics fm JOIN fm.analysisRun ar WHERE ar.repository.id = :repoId " +
           "AND ar.id = (SELECT MAX(ar2.id) FROM AnalysisRun ar2 WHERE ar2.repository.id = :repoId AND ar2.status = 'COMPLETED') " +
           "ORDER BY fm.cyclomaticComplexity DESC")
    List<FileMetrics> findHotspotsByRepositoryId(@Param("repoId") Long repoId);
}
