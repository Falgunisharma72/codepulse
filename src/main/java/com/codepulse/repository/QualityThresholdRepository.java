package com.codepulse.repository;

import com.codepulse.model.entity.QualityThreshold;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface QualityThresholdRepository extends JpaRepository<QualityThreshold, Long> {
    Optional<QualityThreshold> findByRepositoryId(Long repositoryId);
}
