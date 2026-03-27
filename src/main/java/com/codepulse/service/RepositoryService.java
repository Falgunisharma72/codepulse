package com.codepulse.service;

import com.codepulse.exception.RepositoryNotFoundException;
import com.codepulse.model.dto.RepositoryDTO;
import com.codepulse.model.entity.QualityThreshold;
import com.codepulse.model.entity.Repository;
import com.codepulse.model.entity.User;
import com.codepulse.model.entity.Metrics;
import com.codepulse.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RepositoryService {

    private final RepoRepository repoRepository;
    private final UserRepository userRepository;
    private final MetricsRepository metricsRepository;
    private final AlertRepository alertRepository;
    private final QualityThresholdRepository thresholdRepository;
    private final HealthScoreService healthScoreService;

    @Transactional
    public RepositoryDTO registerRepository(RepositoryDTO dto) {
        // Get or create user (simplified — using a default user for now)
        User user = userRepository.findByGithubUsername("default")
                .orElseGet(() -> userRepository.save(User.builder()
                        .githubUsername("default")
                        .build()));

        if (repoRepository.existsByUserIdAndGithubRepoUrl(user.getId(), dto.getGithubRepoUrl())) {
            throw new IllegalArgumentException("Repository already registered: " + dto.getGithubRepoUrl());
        }

        Repository repository = Repository.builder()
                .user(user)
                .githubRepoUrl(dto.getGithubRepoUrl())
                .repoName(dto.getRepoName())
                .defaultBranch(dto.getDefaultBranch() != null ? dto.getDefaultBranch() : "main")
                .webhookSecret(UUID.randomUUID().toString())
                .isActive(true)
                .build();

        repository = repoRepository.save(repository);

        // Create default quality thresholds
        QualityThreshold threshold = QualityThreshold.builder()
                .repository(repository)
                .build();
        thresholdRepository.save(threshold);

        log.info("Registered repository: {}", repository.getRepoName());
        return toDTO(repository);
    }

    public List<RepositoryDTO> getAllRepositories() {
        return repoRepository.findByIsActiveTrue().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public RepositoryDTO getRepository(Long id) {
        Repository repo = repoRepository.findById(id)
                .orElseThrow(() -> new RepositoryNotFoundException(id));
        return toDTO(repo);
    }

    @Transactional
    public RepositoryDTO updateRepository(Long id, RepositoryDTO dto) {
        Repository repo = repoRepository.findById(id)
                .orElseThrow(() -> new RepositoryNotFoundException(id));

        if (dto.getRepoName() != null) repo.setRepoName(dto.getRepoName());
        if (dto.getDefaultBranch() != null) repo.setDefaultBranch(dto.getDefaultBranch());
        if (dto.getIsActive() != null) repo.setIsActive(dto.getIsActive());

        repo = repoRepository.save(repo);
        return toDTO(repo);
    }

    @Transactional
    public void deleteRepository(Long id) {
        Repository repo = repoRepository.findById(id)
                .orElseThrow(() -> new RepositoryNotFoundException(id));
        repoRepository.delete(repo);
        log.info("Deleted repository: {}", repo.getRepoName());
    }

    private RepositoryDTO toDTO(Repository repo) {
        RepositoryDTO dto = RepositoryDTO.builder()
                .id(repo.getId())
                .githubRepoUrl(repo.getGithubRepoUrl())
                .repoName(repo.getRepoName())
                .defaultBranch(repo.getDefaultBranch())
                .webhookSecret(repo.getWebhookSecret())
                .isActive(repo.getIsActive())
                .createdAt(repo.getCreatedAt())
                .build();

        // Enrich with latest metrics
        metricsRepository.findLatestByRepositoryId(repo.getId()).ifPresent(metrics -> {
            dto.setLatestHealthScore(metrics.getOverallHealthScore());
            dto.setLatestHealthGrade(healthScoreService.getGrade(
                    metrics.getOverallHealthScore() != null ? metrics.getOverallHealthScore() : 0));
            dto.setLastAnalysisAt(metrics.getCreatedAt());
        });

        dto.setActiveAlertCount(alertRepository.countByRepositoryIdAndIsResolvedFalse(repo.getId()));
        return dto;
    }
}
