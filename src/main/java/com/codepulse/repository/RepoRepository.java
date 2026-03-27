package com.codepulse.repository;

import com.codepulse.model.entity.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

@org.springframework.stereotype.Repository
public interface RepoRepository extends JpaRepository<Repository, Long> {
    List<Repository> findByUserId(Long userId);
    Optional<Repository> findByGithubRepoUrl(String githubRepoUrl);
    boolean existsByUserIdAndGithubRepoUrl(Long userId, String githubRepoUrl);
    List<Repository> findByIsActiveTrue();
}
