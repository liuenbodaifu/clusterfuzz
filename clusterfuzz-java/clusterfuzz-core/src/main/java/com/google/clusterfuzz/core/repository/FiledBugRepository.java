package com.google.clusterfuzz.core.repository;

import com.google.clusterfuzz.core.entity.FiledBug;
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
 * Repository interface for FiledBug entities.
 * Provides data access methods for bug filing and tracking operations.
 */
@Repository
public interface FiledBugRepository extends JpaRepository<FiledBug, Long> {

    // Basic queries
    List<FiledBug> findByTestcaseId(Long testcaseId);
    
    Optional<FiledBug> findByTestcaseIdAndBugInformation(Long testcaseId, Integer bugInformation);
    
    List<FiledBug> findByBugInformation(Integer bugInformation);
    
    List<FiledBug> findByGroupId(Long groupId);

    // Security flag queries
    List<FiledBug> findBySecurityFlag(Boolean securityFlag);
    
    @Query("SELECT f FROM FiledBug f WHERE f.securityFlag = true ORDER BY f.timestamp DESC")
    List<FiledBug> findSecurityBugs();
    
    @Query("SELECT f FROM FiledBug f WHERE f.securityFlag = false ORDER BY f.timestamp DESC")
    List<FiledBug> findFunctionalBugs();

    // Project and job type queries
    List<FiledBug> findByProjectName(String projectName);
    
    List<FiledBug> findByJobType(String jobType);
    
    List<FiledBug> findByProjectNameAndJobType(String projectName, String jobType);

    // Platform queries
    List<FiledBug> findByPlatformId(String platformId);
    
    @Query("SELECT f FROM FiledBug f WHERE f.projectName = :projectName AND f.platformId = :platformId")
    List<FiledBug> findByProjectAndPlatform(@Param("projectName") String projectName, @Param("platformId") String platformId);

    // Crash type and state queries
    List<FiledBug> findByCrashType(String crashType);
    
    List<FiledBug> findByCrashState(String crashState);
    
    List<FiledBug> findByCrashTypeAndCrashState(String crashType, String crashState);

    // Issue tracker queries
    List<FiledBug> findByIssueTrackerType(String issueTrackerType);
    
    List<FiledBug> findByIssueStatus(String issueStatus);
    
    @Query("SELECT f FROM FiledBug f WHERE f.externalIssueUrl IS NOT NULL AND f.externalIssueUrl != ''")
    List<FiledBug> findWithExternalTracking();
    
    @Query("SELECT f FROM FiledBug f WHERE f.issueStatus IS NULL OR f.issueStatus NOT IN ('closed', 'resolved', 'fixed', 'done')")
    List<FiledBug> findOpenIssues();

    // Time-based queries
    List<FiledBug> findByTimestampAfter(LocalDateTime timestamp);
    
    List<FiledBug> findByTimestampBetween(LocalDateTime start, LocalDateTime end);
    
    @Query("SELECT f FROM FiledBug f WHERE f.timestamp >= :since ORDER BY f.timestamp DESC")
    List<FiledBug> findRecentBugs(@Param("since") LocalDateTime since);

    // Statistics queries
    @Query("SELECT COUNT(f) FROM FiledBug f WHERE f.testcaseId = :testcaseId")
    long countByTestcaseId(@Param("testcaseId") Long testcaseId);
    
    @Query("SELECT COUNT(f) FROM FiledBug f WHERE f.securityFlag = true")
    long countSecurityBugs();
    
    @Query("SELECT COUNT(f) FROM FiledBug f WHERE f.securityFlag = false")
    long countFunctionalBugs();
    
    @Query("SELECT COUNT(f) FROM FiledBug f WHERE f.projectName = :projectName")
    long countByProject(@Param("projectName") String projectName);

    // Aggregation queries
    @Query("SELECT f.projectName, COUNT(f) FROM FiledBug f GROUP BY f.projectName ORDER BY COUNT(f) DESC")
    List<Object[]> getBugCountsByProject();
    
    @Query("SELECT f.crashType, COUNT(f) FROM FiledBug f WHERE f.crashType IS NOT NULL GROUP BY f.crashType ORDER BY COUNT(f) DESC")
    List<Object[]> getBugCountsByCrashType();
    
    @Query("SELECT f.platformId, COUNT(f) FROM FiledBug f WHERE f.platformId IS NOT NULL GROUP BY f.platformId ORDER BY COUNT(f) DESC")
    List<Object[]> getBugCountsByPlatform();
    
    @Query("SELECT f.jobType, COUNT(f), COUNT(CASE WHEN f.securityFlag = true THEN 1 END) " +
           "FROM FiledBug f WHERE f.jobType IS NOT NULL GROUP BY f.jobType ORDER BY COUNT(f) DESC")
    List<Object[]> getBugStatsByJobType();

    // Bug information queries
    @Query("SELECT f FROM FiledBug f WHERE f.bugInformation > 0 ORDER BY f.timestamp DESC")
    List<FiledBug> findFiledBugs();
    
    @Query("SELECT f FROM FiledBug f WHERE f.bugInformation = 0 OR f.bugInformation IS NULL ORDER BY f.timestamp DESC")
    List<FiledBug> findUnfiledBugs();
    
    @Query("SELECT COUNT(f) FROM FiledBug f WHERE f.bugInformation > 0")
    long countFiledBugs();

    // Group queries
    @Query("SELECT f FROM FiledBug f WHERE f.groupId IS NOT NULL ORDER BY f.groupId, f.timestamp")
    List<FiledBug> findGroupedBugs();
    
    @Query("SELECT f FROM FiledBug f WHERE f.groupId IS NULL ORDER BY f.timestamp DESC")
    List<FiledBug> findUngroupedBugs();
    
    @Query("SELECT f.groupId, COUNT(f) FROM FiledBug f WHERE f.groupId IS NOT NULL GROUP BY f.groupId ORDER BY COUNT(f) DESC")
    List<Object[]> getBugCountsByGroup();

    // Search queries
    @Query("SELECT f FROM FiledBug f WHERE " +
           "f.crashType LIKE %:searchTerm% OR " +
           "f.crashState LIKE %:searchTerm% OR " +
           "f.projectName LIKE %:searchTerm% OR " +
           "f.jobType LIKE %:searchTerm% " +
           "ORDER BY f.timestamp DESC")
    List<FiledBug> searchBugs(@Param("searchTerm") String searchTerm);

    // Duplicate detection
    @Query("SELECT f FROM FiledBug f WHERE f.crashType = :crashType AND f.crashState = :crashState " +
           "AND f.projectName = :projectName AND f.testcaseId != :excludeTestcaseId")
    List<FiledBug> findPotentialDuplicates(
        @Param("crashType") String crashType,
        @Param("crashState") String crashState,
        @Param("projectName") String projectName,
        @Param("excludeTestcaseId") Long excludeTestcaseId
    );

    // Trend analysis
    @Query("SELECT DATE(f.timestamp), COUNT(f), COUNT(CASE WHEN f.securityFlag = true THEN 1 END) " +
           "FROM FiledBug f WHERE f.timestamp >= :since GROUP BY DATE(f.timestamp) ORDER BY DATE(f.timestamp)")
    List<Object[]> getDailyBugTrends(@Param("since") LocalDateTime since);
    
    @Query("SELECT f.projectName, DATE(f.timestamp), COUNT(f) " +
           "FROM FiledBug f WHERE f.timestamp >= :since AND f.projectName IS NOT NULL " +
           "GROUP BY f.projectName, DATE(f.timestamp) ORDER BY f.projectName, DATE(f.timestamp)")
    List<Object[]> getProjectBugTrends(@Param("since") LocalDateTime since);

    // Issue status tracking
    @Query("SELECT f FROM FiledBug f WHERE f.issueStatus != :oldStatus AND f.updatedAt >= :since")
    List<FiledBug> findStatusChanges(@Param("oldStatus") String oldStatus, @Param("since") LocalDateTime since);
    
    @Query("SELECT f.issueStatus, COUNT(f) FROM FiledBug f WHERE f.issueStatus IS NOT NULL " +
           "GROUP BY f.issueStatus ORDER BY COUNT(f) DESC")
    List<Object[]> getIssueStatusDistribution();

    // Cleanup queries
    @Query("SELECT f FROM FiledBug f WHERE f.timestamp < :cutoffDate ORDER BY f.timestamp ASC")
    List<FiledBug> findOldBugs(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    @Query("DELETE FROM FiledBug f WHERE f.timestamp < :cutoffDate")
    void deleteOldBugs(@Param("cutoffDate") LocalDateTime cutoffDate);

    // Pagination support
    Page<FiledBug> findByProjectName(String projectName, Pageable pageable);
    
    Page<FiledBug> findBySecurityFlag(Boolean securityFlag, Pageable pageable);
    
    Page<FiledBug> findByCrashType(String crashType, Pageable pageable);

    // Existence checks
    boolean existsByTestcaseId(Long testcaseId);
    
    boolean existsByTestcaseIdAndBugInformation(Long testcaseId, Integer bugInformation);
    
    boolean existsByBugInformation(Integer bugInformation);

    // Complex filtering
    @Query("SELECT f FROM FiledBug f WHERE " +
           "(:projectName IS NULL OR f.projectName = :projectName) AND " +
           "(:jobType IS NULL OR f.jobType = :jobType) AND " +
           "(:crashType IS NULL OR f.crashType = :crashType) AND " +
           "(:securityFlag IS NULL OR f.securityFlag = :securityFlag) AND " +
           "(:platformId IS NULL OR f.platformId = :platformId) AND " +
           "(:startDate IS NULL OR f.timestamp >= :startDate) AND " +
           "(:endDate IS NULL OR f.timestamp <= :endDate) " +
           "ORDER BY f.timestamp DESC")
    Page<FiledBug> findWithFilters(
        @Param("projectName") String projectName,
        @Param("jobType") String jobType,
        @Param("crashType") String crashType,
        @Param("securityFlag") Boolean securityFlag,
        @Param("platformId") String platformId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        Pageable pageable
    );

    // Recent activity
    @Query("SELECT f FROM FiledBug f WHERE f.updatedAt >= :since ORDER BY f.updatedAt DESC")
    List<FiledBug> findRecentActivity(@Param("since") LocalDateTime since);
    
    @Query("SELECT f FROM FiledBug f WHERE f.projectName = :projectName AND f.updatedAt >= :since ORDER BY f.updatedAt DESC")
    List<FiledBug> findRecentActivityByProject(@Param("projectName") String projectName, @Param("since") LocalDateTime since);
}