package com.codepulse.repository;

import com.codepulse.model.entity.Metrics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MetricsRepository extends JpaRepository<Metrics, Long> {
    Optional<Metrics> findByAnalysisRunId(Long analysisRunId);

    @Query("SELECT m FROM Metrics m JOIN m.analysisRun ar WHERE ar.repository.id = :repoId " +
           "AND ar.status = 'COMPLETED' ORDER BY ar.createdAt DESC LIMIT 1")
    Optional<Metrics> findLatestByRepositoryId(@Param("repoId") Long repoId);

    @Query("SELECT m FROM Metrics m JOIN m.analysisRun ar WHERE ar.repository.id = :repoId " +
           "AND ar.status = 'COMPLETED' AND ar.createdAt BETWEEN :from AND :to ORDER BY ar.createdAt ASC")
    List<Metrics> findTrendByRepositoryId(@Param("repoId") Long repoId,
                                           @Param("from") LocalDateTime from,
                                           @Param("to") LocalDateTime to);
}
