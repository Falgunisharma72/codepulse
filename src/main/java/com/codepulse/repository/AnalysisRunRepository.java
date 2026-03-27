package com.codepulse.repository;

import com.codepulse.model.entity.AnalysisRun;
import com.codepulse.model.enums.AnalysisStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface AnalysisRunRepository extends JpaRepository<AnalysisRun, Long> {
    Page<AnalysisRun> findByRepositoryIdOrderByCreatedAtDesc(Long repositoryId, Pageable pageable);
    Optional<AnalysisRun> findFirstByRepositoryIdAndStatusOrderByCreatedAtDesc(Long repositoryId, AnalysisStatus status);
    Optional<AnalysisRun> findFirstByRepositoryIdOrderByCreatedAtDesc(Long repositoryId);
    long countByRepositoryId(Long repositoryId);
}
