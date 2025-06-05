package com.google.clusterfuzz.core.repository;

import com.google.clusterfuzz.core.entity.Testcase;
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
 * Repository interface for Testcase entities.
 * Provides data access methods for testcase operations.
 */
@Repository
public interface TestcaseRepository extends JpaRepository<Testcase, Long> {

    // Basic queries
    List<Testcase> findByStatus(String status);
    
    List<Testcase> findByJobType(String jobType);
    
    List<Testcase> findByFuzzerName(String fuzzerName);
    
    List<Testcase> findByProjectName(String projectName);
    
    List<Testcase> findByPlatform(String platform);
    
    // Security-related queries
    List<Testcase> findBySecurityFlagTrue();
    
    List<Testcase> findBySecurityFlagTrueAndOpenTrue();
    
    @Query("SELECT t FROM Testcase t WHERE t.securityFlag = true AND t.securitySeverity >= :minSeverity")
    List<Testcase> findHighSeveritySecurityBugs(@Param("minSeverity") Integer minSeverity);
    
    // Open/closed testcases
    List<Testcase> findByOpenTrue();
    
    List<Testcase> findByOpenFalse();
    
    // Group-related queries
    List<Testcase> findByGroupId(Long groupId);
    
    List<Testcase> findByIsLeaderTrue();
    
    List<Testcase> findByGroupIdAndIsLeaderTrue(Long groupId);
    
    // Bug-related queries
    List<Testcase> findByHasBugFlagTrue();
    
    List<Testcase> findByHasBugFlagFalse();
    
    List<Testcase> findByDuplicateOf(Long duplicateOf);
    
    List<Testcase> findByIsADuplicateFlagTrue();
    
    // Time-based queries
    List<Testcase> findByTimestampAfter(LocalDateTime timestamp);
    
    List<Testcase> findByTimestampBetween(LocalDateTime start, LocalDateTime end);
    
    List<Testcase> findByCreatedAfter(LocalDateTime created);
    
    // Crash-related queries
    List<Testcase> findByCrashType(String crashType);
    
    List<Testcase> findByCrashState(String crashState);
    
    @Query("SELECT t FROM Testcase t WHERE t.crashType = :crashType AND t.crashState = :crashState")
    List<Testcase> findByCrashTypeAndState(@Param("crashType") String crashType, 
                                          @Param("crashState") String crashState);
    
    // Complex queries
    @Query("SELECT t FROM Testcase t WHERE t.status = :status AND t.jobType = :jobType AND t.open = true")
    List<Testcase> findOpenTestcasesByStatusAndJobType(@Param("status") String status, 
                                                      @Param("jobType") String jobType);
    
    @Query("SELECT t FROM Testcase t WHERE t.projectName = :projectName AND t.securityFlag = true AND t.open = true")
    List<Testcase> findOpenSecurityBugsByProject(@Param("projectName") String projectName);
    
    @Query("SELECT t FROM Testcase t WHERE t.fuzzerName = :fuzzerName AND t.timestamp >= :since")
    List<Testcase> findRecentTestcasesByFuzzer(@Param("fuzzerName") String fuzzerName, 
                                              @Param("since") LocalDateTime since);
    
    // Pagination queries
    Page<Testcase> findByStatus(String status, Pageable pageable);
    
    Page<Testcase> findByProjectName(String projectName, Pageable pageable);
    
    Page<Testcase> findBySecurityFlagTrue(Pageable pageable);
    
    Page<Testcase> findByOpenTrue(Pageable pageable);
    
    // Statistics queries
    @Query("SELECT COUNT(t) FROM Testcase t WHERE t.status = :status")
    long countByStatus(@Param("status") String status);
    
    @Query("SELECT COUNT(t) FROM Testcase t WHERE t.securityFlag = true")
    long countSecurityBugs();
    
    @Query("SELECT COUNT(t) FROM Testcase t WHERE t.open = true")
    long countOpenTestcases();
    
    @Query("SELECT COUNT(t) FROM Testcase t WHERE t.projectName = :projectName")
    long countByProject(@Param("projectName") String projectName);
    
    @Query("SELECT COUNT(t) FROM Testcase t WHERE t.fuzzerName = :fuzzerName")
    long countByFuzzer(@Param("fuzzerName") String fuzzerName);
    
    @Query("SELECT COUNT(t) FROM Testcase t WHERE t.timestamp >= :since")
    long countRecentTestcases(@Param("since") LocalDateTime since);
    
    // Search queries
    @Query("SELECT t FROM Testcase t WHERE " +
           "LOWER(t.crashType) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(t.crashState) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(t.fuzzerName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(t.jobType) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Testcase> searchTestcases(@Param("searchTerm") String searchTerm);
    
    @Query("SELECT t FROM Testcase t WHERE " +
           "LOWER(t.crashType) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(t.crashState) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(t.fuzzerName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(t.jobType) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Testcase> searchTestcases(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    // Keyword-based search (for when keywords are implemented as a separate table)
    @Query("SELECT DISTINCT t FROM Testcase t JOIN t.keywords k WHERE LOWER(k) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Testcase> findByKeywordContaining(@Param("keyword") String keyword);
    
    // Custom queries for specific business logic
    @Query("SELECT t FROM Testcase t WHERE t.groupId = :groupId AND t.isLeader = false")
    List<Testcase> findGroupMembers(@Param("groupId") Long groupId);
    
    @Query("SELECT t FROM Testcase t WHERE t.status = 'Processed' AND t.triaged = false")
    List<Testcase> findUntriagedProcessedTestcases();
    
    @Query("SELECT t FROM Testcase t WHERE t.oneTimeCrasherFlag = true AND t.timestamp >= :since")
    List<Testcase> findRecentOneTimeCrashers(@Param("since") LocalDateTime since);
    
    @Query("SELECT t FROM Testcase t WHERE t.flakyStack = true")
    List<Testcase> findFlakyTestcases();
    
    // Regression and fix tracking
    @Query("SELECT t FROM Testcase t WHERE t.regression IS NOT NULL AND t.regression != ''")
    List<Testcase> findTestcasesWithRegression();
    
    @Query("SELECT t FROM Testcase t WHERE t.fixed IS NOT NULL AND t.fixed != ''")
    List<Testcase> findFixedTestcases();
    
    // Platform-specific queries
    @Query("SELECT t FROM Testcase t WHERE t.platform = :platform AND t.timestamp >= :since")
    List<Testcase> findRecentTestcasesByPlatform(@Param("platform") String platform, 
                                                @Param("since") LocalDateTime since);
    
    // Batch operations support
    @Query("SELECT t FROM Testcase t WHERE t.id IN :ids")
    List<Testcase> findByIdIn(@Param("ids") List<Long> ids);
    
    // Exists queries for performance
    boolean existsByGroupId(Long groupId);
    
    boolean existsByCrashTypeAndCrashState(String crashType, String crashState);
    
    boolean existsByFuzzerNameAndJobType(String fuzzerName, String jobType);
}