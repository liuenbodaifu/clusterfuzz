package com.google.clusterfuzz.core.service;

import com.google.clusterfuzz.core.entity.BuildMetadata;
import com.google.clusterfuzz.core.repository.BuildMetadataRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service for managing BuildMetadata entities.
 * Provides business logic for build metadata operations.
 */
@Service
@Transactional
public class BuildMetadataService {

    private static final Logger logger = LoggerFactory.getLogger(BuildMetadataService.class);

    private final BuildMetadataRepository buildMetadataRepository;

    @Autowired
    public BuildMetadataService(BuildMetadataRepository buildMetadataRepository) {
        this.buildMetadataRepository = buildMetadataRepository;
    }

    /**
     * Create a new build metadata record.
     */
    public BuildMetadata createBuildMetadata(String jobType, Integer revision, String botName) {
        logger.info("Creating build metadata for job: {}, revision: {}, bot: {}", jobType, revision, botName);
        
        BuildMetadata metadata = new BuildMetadata(jobType, revision);
        metadata.setBotName(botName);
        metadata.setTimestamp(LocalDateTime.now());
        
        return buildMetadataRepository.save(metadata);
    }

    /**
     * Mark a build as bad.
     */
    public BuildMetadata markBuildAsBad(Long id, String consoleOutput) {
        logger.warn("Marking build {} as bad", id);
        
        BuildMetadata metadata = buildMetadataRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Build metadata not found: " + id));
        
        metadata.markAsBadBuild();
        if (consoleOutput != null) {
            metadata.setConsoleOutput(consoleOutput);
        }
        
        return buildMetadataRepository.save(metadata);
    }

    /**
     * Mark a build as good.
     */
    public BuildMetadata markBuildAsGood(Long id) {
        logger.info("Marking build {} as good", id);
        
        BuildMetadata metadata = buildMetadataRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Build metadata not found: " + id));
        
        metadata.markAsGoodBuild();
        
        return buildMetadataRepository.save(metadata);
    }

    /**
     * Get the latest build for a job type.
     */
    @Transactional(readOnly = true)
    public Optional<BuildMetadata> getLatestBuild(String jobType) {
        return buildMetadataRepository.findLatestByJobType(jobType);
    }

    /**
     * Get the latest good build for a job type.
     */
    @Transactional(readOnly = true)
    public Optional<BuildMetadata> getLatestGoodBuild(String jobType) {
        return buildMetadataRepository.findLatestGoodBuildByJobType(jobType);
    }

    /**
     * Get build success rate for a job type.
     */
    @Transactional(readOnly = true)
    public double getBuildSuccessRate(String jobType) {
        Double rate = buildMetadataRepository.calculateSuccessRateByJobType(jobType);
        return rate != null ? rate : 0.0;
    }

    /**
     * Get builds within a revision range.
     */
    @Transactional(readOnly = true)
    public List<BuildMetadata> getBuildsInRevisionRange(String jobType, Integer startRevision, Integer endRevision) {
        return buildMetadataRepository.findByJobTypeAndRevisionBetween(jobType, startRevision, endRevision);
    }

    /**
     * Get recent builds since a timestamp.
     */
    @Transactional(readOnly = true)
    public List<BuildMetadata> getRecentBuilds(String jobType, LocalDateTime since) {
        return buildMetadataRepository.findRecentBuildsByJobType(jobType, since);
    }

    /**
     * Get build statistics by job type.
     */
    @Transactional(readOnly = true)
    public List<Object[]> getBuildStatistics() {
        return buildMetadataRepository.getBuildStatsByJobType();
    }

    /**
     * Get daily build statistics.
     */
    @Transactional(readOnly = true)
    public List<Object[]> getDailyBuildStatistics(LocalDateTime since) {
        return buildMetadataRepository.getDailyBuildStats(since);
    }

    /**
     * Check if a build exists for job type and revision.
     */
    @Transactional(readOnly = true)
    public boolean buildExists(String jobType, Integer revision) {
        return buildMetadataRepository.existsByJobTypeAndRevision(jobType, revision);
    }

    /**
     * Get builds with filters and pagination.
     */
    @Transactional(readOnly = true)
    public Page<BuildMetadata> getBuildsWithFilters(
            String jobType, Boolean badBuild, String botName, 
            Integer minRevision, Integer maxRevision, Pageable pageable) {
        return buildMetadataRepository.findWithFilters(
            jobType, badBuild, botName, minRevision, maxRevision, pageable);
    }

    /**
     * Clean up old build metadata.
     */
    public int cleanupOldBuilds(LocalDateTime cutoffDate) {
        logger.info("Cleaning up build metadata older than {}", cutoffDate);
        
        List<BuildMetadata> oldBuilds = buildMetadataRepository.findOldBuilds(cutoffDate);
        int count = oldBuilds.size();
        
        if (count > 0) {
            buildMetadataRepository.deleteOldBuilds(cutoffDate);
            logger.info("Deleted {} old build metadata records", count);
        }
        
        return count;
    }

    /**
     * Update build with console output and symbols.
     */
    public BuildMetadata updateBuildDetails(Long id, String consoleOutput, String symbols) {
        BuildMetadata metadata = buildMetadataRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Build metadata not found: " + id));
        
        if (consoleOutput != null) {
            metadata.setConsoleOutput(consoleOutput);
        }
        if (symbols != null) {
            metadata.setSymbols(symbols);
        }
        
        return buildMetadataRepository.save(metadata);
    }

    /**
     * Get all builds for a specific bot.
     */
    @Transactional(readOnly = true)
    public List<BuildMetadata> getBuildsByBot(String botName) {
        return buildMetadataRepository.findByBotNameOrderByTimestampDesc(botName);
    }

    /**
     * Get build count by bot.
     */
    @Transactional(readOnly = true)
    public List<Object[]> getBuildCountsByBot() {
        return buildMetadataRepository.getBuildCountsByBot();
    }

    /**
     * Find build by job type and revision.
     */
    @Transactional(readOnly = true)
    public Optional<BuildMetadata> findBuild(String jobType, Integer revision) {
        return buildMetadataRepository.findByJobTypeAndRevision(jobType, revision);
    }

    /**
     * Get all builds for a job type.
     */
    @Transactional(readOnly = true)
    public List<BuildMetadata> getAllBuildsForJob(String jobType) {
        return buildMetadataRepository.findByJobTypeOrderByTimestampDesc(jobType);
    }

    /**
     * Get good builds for a job type.
     */
    @Transactional(readOnly = true)
    public List<BuildMetadata> getGoodBuilds(String jobType) {
        return buildMetadataRepository.findGoodBuildsByJobType(jobType);
    }

    /**
     * Get bad builds for a job type.
     */
    @Transactional(readOnly = true)
    public List<BuildMetadata> getBadBuilds(String jobType) {
        return buildMetadataRepository.findBadBuildsByJobType(jobType);
    }

    /**
     * Count builds for a job type.
     */
    @Transactional(readOnly = true)
    public long countBuilds(String jobType) {
        return buildMetadataRepository.countByJobType(jobType);
    }

    /**
     * Count good builds for a job type.
     */
    @Transactional(readOnly = true)
    public long countGoodBuilds(String jobType) {
        return buildMetadataRepository.countGoodBuildsByJobType(jobType);
    }

    /**
     * Count bad builds for a job type.
     */
    @Transactional(readOnly = true)
    public long countBadBuilds(String jobType) {
        return buildMetadataRepository.countBadBuildsByJobType(jobType);
    }

    /**
     * Save or update build metadata.
     */
    public BuildMetadata save(BuildMetadata buildMetadata) {
        return buildMetadataRepository.save(buildMetadata);
    }

    /**
     * Find build metadata by ID.
     */
    @Transactional(readOnly = true)
    public Optional<BuildMetadata> findById(Long id) {
        return buildMetadataRepository.findById(id);
    }

    /**
     * Delete build metadata.
     */
    public void delete(BuildMetadata buildMetadata) {
        buildMetadataRepository.delete(buildMetadata);
    }

    /**
     * Delete build metadata by ID.
     */
    public void deleteById(Long id) {
        buildMetadataRepository.deleteById(id);
    }
}