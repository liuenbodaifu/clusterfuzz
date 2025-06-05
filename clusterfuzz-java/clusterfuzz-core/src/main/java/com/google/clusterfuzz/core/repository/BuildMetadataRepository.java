package com.google.clusterfuzz.core.repository;

import com.google.clusterfuzz.core.entity.BuildMetadata;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for BuildMetadata entities.
 * Provides data access methods for build metadata operations.
 */
@Repository
public interface BuildMetadataRepository extends JpaRepository<BuildMetadata, Long> {

    // Basic queries
    List<BuildMetadata> findByJobType(String jobType);
    
    List<BuildMetadata> findByJobTypeOrderByTimestampDesc(String jobType);
    
    Optional<BuildMetadata> findByJobTypeAndRevision(String jobType, Integer revision);
    
    List<BuildMetadata> findByRevision(Integer revision);
    
    List<BuildMetadata> findByBotName(String botName);

    // Build status queries
    List<BuildMetadata> findByBadBuild(Boolean badBuild);
    
    List<BuildMetadata> findByJobTypeAndBadBuild(String jobType, Boolean badBuild);
    
    @Query("SELECT b FROM BuildMetadata b WHERE b.jobType = :jobType AND b.badBuild = false ORDER BY b.revision DESC")
    List<BuildMetadata> findGoodBuildsByJobType(@Param("jobType") String jobType);
    
    @Query("SELECT b FROM BuildMetadata b WHERE b.jobType = :jobType AND b.badBuild = true ORDER BY b.revision DESC")
    List<BuildMetadata> findBadBuildsByJobType(@Param("jobType") String jobType);

    // Latest build queries
    @Query("SELECT b FROM BuildMetadata b WHERE b.jobType = :jobType ORDER BY b.revision DESC LIMIT 1")
    Optional<BuildMetadata> findLatestByJobType(@Param("jobType") String jobType);
    
    @Query("SELECT b FROM BuildMetadata b WHERE b.jobType = :jobType AND b.badBuild = false ORDER BY b.revision DESC LIMIT 1")
    Optional<BuildMetadata> findLatestGoodBuildByJobType(@Param("jobType") String jobType);
    
    @Query("SELECT b FROM BuildMetadata b WHERE b.jobType = :jobType AND b.badBuild = true ORDER BY b.revision DESC LIMIT 1")
    Optional<BuildMetadata> findLatestBadBuildByJobType(@Param("jobType") String jobType);

    // Revision range queries
    List<BuildMetadata> findByJobTypeAndRevisionBetween(String jobType, Integer startRevision, Integer endRevision);
    
    @Query("SELECT b FROM BuildMetadata b WHERE b.jobType = :jobType AND b.revision >= :minRevision ORDER BY b.revision ASC")
    List<BuildMetadata> findByJobTypeAndRevisionGreaterThanEqual(@Param("jobType") String jobType, @Param("minRevision") Integer minRevision);
    
    @Query("SELECT b FROM BuildMetadata b WHERE b.jobType = :jobType AND b.revision <= :maxRevision ORDER BY b.revision DESC")
    List<BuildMetadata> findByJobTypeAndRevisionLessThanEqual(@Param("jobType") String jobType, @Param("maxRevision") Integer maxRevision);

    // Time-based queries
    List<BuildMetadata> findByTimestampAfter(LocalDateTime timestamp);
    
    List<BuildMetadata> findByTimestampBetween(LocalDateTime start, LocalDateTime end);
    
    @Query("SELECT b FROM BuildMetadata b WHERE b.jobType = :jobType AND b.timestamp >= :since ORDER BY b.timestamp DESC")
    List<BuildMetadata> findRecentBuildsByJobType(@Param("jobType") String jobType, @Param("since") LocalDateTime since);

    // Statistics queries
    @Query("SELECT COUNT(b) FROM BuildMetadata b WHERE b.jobType = :jobType")
    long countByJobType(@Param("jobType") String jobType);
    
    @Query("SELECT COUNT(b) FROM BuildMetadata b WHERE b.jobType = :jobType AND b.badBuild = :badBuild")
    long countByJobTypeAndBadBuild(@Param("jobType") String jobType, @Param("badBuild") Boolean badBuild);
    
    @Query("SELECT COUNT(b) FROM BuildMetadata b WHERE b.jobType = :jobType AND b.badBuild = false")
    long countGoodBuildsByJobType(@Param("jobType") String jobType);
    
    @Query("SELECT COUNT(b) FROM BuildMetadata b WHERE b.jobType = :jobType AND b.badBuild = true")
    long countBadBuildsByJobType(@Param("jobType") String jobType);

    // Build success rate
    @Query("SELECT (COUNT(b) * 100.0 / (SELECT COUNT(b2) FROM BuildMetadata b2 WHERE b2.jobType = :jobType)) " +
           "FROM BuildMetadata b WHERE b.jobType = :jobType AND b.badBuild = false")
    Double calculateSuccessRateByJobType(@Param("jobType") String jobType);

    // Console output queries
    @Query("SELECT b FROM BuildMetadata b WHERE b.consoleOutput IS NOT NULL AND b.consoleOutput != ''")
    List<BuildMetadata> findBuildsWithConsoleOutput();
    
    @Query("SELECT b FROM BuildMetadata b WHERE b.jobType = :jobType AND b.consoleOutput IS NOT NULL AND b.consoleOutput != ''")
    List<BuildMetadata> findBuildsWithConsoleOutputByJobType(@Param("jobType") String jobType);

    // Symbol data queries
    @Query("SELECT b FROM BuildMetadata b WHERE b.symbols IS NOT NULL AND b.symbols != ''")
    List<BuildMetadata> findBuildsWithSymbols();
    
    @Query("SELECT b FROM BuildMetadata b WHERE b.jobType = :jobType AND b.symbols IS NOT NULL AND b.symbols != ''")
    List<BuildMetadata> findBuildsWithSymbolsByJobType(@Param("jobType") String jobType);

    // Bot-specific queries
    List<BuildMetadata> findByBotNameOrderByTimestampDesc(String botName);
    
    @Query("SELECT COUNT(b) FROM BuildMetadata b WHERE b.botName = :botName")
    long countByBotName(@Param("botName") String botName);
    
    @Query("SELECT b.botName, COUNT(b) FROM BuildMetadata b GROUP BY b.botName ORDER BY COUNT(b) DESC")
    List<Object[]> getBuildCountsByBot();

    // Cleanup queries
    @Query("SELECT b FROM BuildMetadata b WHERE b.timestamp < :cutoffDate ORDER BY b.timestamp ASC")
    List<BuildMetadata> findOldBuilds(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    @Query("DELETE FROM BuildMetadata b WHERE b.timestamp < :cutoffDate")
    void deleteOldBuilds(@Param("cutoffDate") LocalDateTime cutoffDate);

    // Pagination support
    Page<BuildMetadata> findByJobType(String jobType, Pageable pageable);
    
    Page<BuildMetadata> findByJobTypeAndBadBuild(String jobType, Boolean badBuild, Pageable pageable);
    
    Page<BuildMetadata> findByBotName(String botName, Pageable pageable);

    // Existence checks
    boolean existsByJobTypeAndRevision(String jobType, Integer revision);
    
    boolean existsByJobTypeAndRevisionAndBadBuild(String jobType, Integer revision, Boolean badBuild);

    // Custom complex queries
    @Query("SELECT b FROM BuildMetadata b WHERE " +
           "(:jobType IS NULL OR b.jobType = :jobType) AND " +
           "(:badBuild IS NULL OR b.badBuild = :badBuild) AND " +
           "(:botName IS NULL OR b.botName = :botName) AND " +
           "(:minRevision IS NULL OR b.revision >= :minRevision) AND " +
           "(:maxRevision IS NULL OR b.revision <= :maxRevision) " +
           "ORDER BY b.timestamp DESC")
    Page<BuildMetadata> findWithFilters(
        @Param("jobType") String jobType,
        @Param("badBuild") Boolean badBuild,
        @Param("botName") String botName,
        @Param("minRevision") Integer minRevision,
        @Param("maxRevision") Integer maxRevision,
        Pageable pageable
    );

    // Aggregation queries
    @Query("SELECT b.jobType, COUNT(b), AVG(CASE WHEN b.badBuild = false THEN 1.0 ELSE 0.0 END) * 100 " +
           "FROM BuildMetadata b GROUP BY b.jobType ORDER BY b.jobType")
    List<Object[]> getBuildStatsByJobType();
    
    @Query("SELECT DATE(b.timestamp), COUNT(b), COUNT(CASE WHEN b.badBuild = false THEN 1 END) " +
           "FROM BuildMetadata b WHERE b.timestamp >= :since GROUP BY DATE(b.timestamp) ORDER BY DATE(b.timestamp)")
    List<Object[]> getDailyBuildStats(@Param("since") LocalDateTime since);
}