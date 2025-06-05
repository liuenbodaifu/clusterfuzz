package com.google.clusterfuzz.core.repository;

import com.google.clusterfuzz.core.entity.Fuzzer;
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
 * Repository interface for Fuzzer entities.
 * Provides data access methods for fuzzer operations.
 */
@Repository
public interface FuzzerRepository extends JpaRepository<Fuzzer, Long> {

    // Basic queries
    Optional<Fuzzer> findByName(String name);
    
    List<Fuzzer> findBySource(String source);
    
    List<Fuzzer> findByRevision(Long revision);
    
    // Builtin and differential fuzzer queries
    List<Fuzzer> findByBuiltinTrue();
    
    List<Fuzzer> findByBuiltinFalse();
    
    List<Fuzzer> findByDifferentialTrue();
    
    List<Fuzzer> findByDifferentialFalse();
    
    // External contribution queries
    List<Fuzzer> findByExternalContributionTrue();
    
    List<Fuzzer> findByExternalContributionFalse();
    
    // Content type queries
    List<Fuzzer> findByUntrustedContentTrue();
    
    List<Fuzzer> findByUntrustedContentFalse();
    
    List<Fuzzer> findByHasLargeTestcasesTrue();
    
    List<Fuzzer> findByHasLargeTestcasesFalse();
    
    // Platform-related queries
    @Query("SELECT f FROM Fuzzer f WHERE f.supportedPlatforms LIKE CONCAT('%', :platform, '%')")
    List<Fuzzer> findBySupportedPlatform(@Param("platform") String platform);
    
    // Data bundle queries
    List<Fuzzer> findByDataBundleName(String dataBundleName);
    
    @Query("SELECT f FROM Fuzzer f WHERE f.dataBundleName IS NOT NULL AND f.dataBundleName != ''")
    List<Fuzzer> findFuzzersWithDataBundle();
    
    @Query("SELECT f FROM Fuzzer f WHERE f.dataBundleName IS NULL OR f.dataBundleName = ''")
    List<Fuzzer> findFuzzersWithoutDataBundle();
    
    // Job-related queries
    @Query("SELECT DISTINCT f FROM Fuzzer f JOIN f.jobs j WHERE j = :jobName")
    List<Fuzzer> findByJob(@Param("jobName") String jobName);
    
    @Query("SELECT f FROM Fuzzer f WHERE f.jobs IS NOT EMPTY")
    List<Fuzzer> findFuzzersWithJobs();
    
    @Query("SELECT f FROM Fuzzer f WHERE f.jobs IS EMPTY OR f.jobs IS NULL")
    List<Fuzzer> findFuzzersWithoutJobs();
    
    // Time-based queries
    List<Fuzzer> findByTimestampAfter(LocalDateTime timestamp);
    
    List<Fuzzer> findByTimestampBetween(LocalDateTime start, LocalDateTime end);
    
    List<Fuzzer> findByResultTimestampAfter(LocalDateTime timestamp);
    
    List<Fuzzer> findByCreatedAfter(LocalDateTime created);
    
    // Result and status queries
    @Query("SELECT f FROM Fuzzer f WHERE f.returnCode = :returnCode")
    List<Fuzzer> findByReturnCode(@Param("returnCode") Integer returnCode);
    
    @Query("SELECT f FROM Fuzzer f WHERE f.returnCode = 0")
    List<Fuzzer> findSuccessfulFuzzers();
    
    @Query("SELECT f FROM Fuzzer f WHERE f.returnCode != 0 AND f.returnCode IS NOT NULL")
    List<Fuzzer> findFailedFuzzers();
    
    @Query("SELECT f FROM Fuzzer f WHERE f.result IS NOT NULL AND f.result != ''")
    List<Fuzzer> findFuzzersWithResults();
    
    // Custom binary and launcher queries
    @Query("SELECT f FROM Fuzzer f WHERE f.blobstoreKey IS NOT NULL AND f.blobstoreKey != ''")
    List<Fuzzer> findFuzzersWithBlobstore();
    
    @Query("SELECT f FROM Fuzzer f WHERE f.launcherScript IS NOT NULL AND f.launcherScript != ''")
    List<Fuzzer> findFuzzersWithLauncher();
    
    @Query("SELECT f FROM Fuzzer f WHERE f.executablePath IS NOT NULL AND f.executablePath != ''")
    List<Fuzzer> findFuzzersWithExecutable();
    
    // Sample testcase queries
    @Query("SELECT f FROM Fuzzer f WHERE f.sampleTestcase IS NOT NULL AND f.sampleTestcase != ''")
    List<Fuzzer> findFuzzersWithSampleTestcase();
    
    // Search queries
    @Query("SELECT f FROM Fuzzer f WHERE " +
           "LOWER(f.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(f.source) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(f.filename) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Fuzzer> searchFuzzers(@Param("searchTerm") String searchTerm);
    
    @Query("SELECT f FROM Fuzzer f WHERE " +
           "LOWER(f.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(f.source) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(f.filename) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Fuzzer> searchFuzzers(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    // Pagination queries
    Page<Fuzzer> findBySource(String source, Pageable pageable);
    
    Page<Fuzzer> findByBuiltinTrue(Pageable pageable);
    
    Page<Fuzzer> findByExternalContributionTrue(Pageable pageable);
    
    // Statistics queries
    @Query("SELECT COUNT(f) FROM Fuzzer f WHERE f.source = :source")
    long countBySource(@Param("source") String source);
    
    @Query("SELECT COUNT(f) FROM Fuzzer f WHERE f.builtin = true")
    long countBuiltinFuzzers();
    
    @Query("SELECT COUNT(f) FROM Fuzzer f WHERE f.differential = true")
    long countDifferentialFuzzers();
    
    @Query("SELECT COUNT(f) FROM Fuzzer f WHERE f.externalContribution = true")
    long countExternalContributions();
    
    @Query("SELECT COUNT(f) FROM Fuzzer f WHERE f.returnCode = 0")
    long countSuccessfulFuzzers();
    
    @Query("SELECT COUNT(f) FROM Fuzzer f WHERE f.timestamp >= :since")
    long countRecentFuzzers(@Param("since") LocalDateTime since);
    
    // Distinct value queries for filtering
    @Query("SELECT DISTINCT f.source FROM Fuzzer f WHERE f.source IS NOT NULL ORDER BY f.source")
    List<String> findDistinctSources();
    
    @Query("SELECT DISTINCT f.dataBundleName FROM Fuzzer f WHERE f.dataBundleName IS NOT NULL AND f.dataBundleName != '' ORDER BY f.dataBundleName")
    List<String> findDistinctDataBundleNames();
    
    // Timeout-related queries
    @Query("SELECT f FROM Fuzzer f WHERE f.timeout >= :minTimeout")
    List<Fuzzer> findByMinTimeout(@Param("minTimeout") Integer minTimeout);
    
    @Query("SELECT f FROM Fuzzer f WHERE f.timeout BETWEEN :minTimeout AND :maxTimeout")
    List<Fuzzer> findByTimeoutRange(@Param("minTimeout") Integer minTimeout, 
                                   @Param("maxTimeout") Integer maxTimeout);
    
    // Max testcases queries
    @Query("SELECT f FROM Fuzzer f WHERE f.maxTestcases >= :minTestcases")
    List<Fuzzer> findByMinMaxTestcases(@Param("minTestcases") Integer minTestcases);
    
    @Query("SELECT f FROM Fuzzer f WHERE f.maxTestcases IS NOT NULL")
    List<Fuzzer> findFuzzersWithMaxTestcasesLimit();
    
    // Batch operations support
    @Query("SELECT f FROM Fuzzer f WHERE f.name IN :names")
    List<Fuzzer> findByNameIn(@Param("names") List<String> names);
    
    @Query("SELECT f FROM Fuzzer f WHERE f.id IN :ids")
    List<Fuzzer> findByIdIn(@Param("ids") List<Long> ids);
    
    // Exists queries for performance
    boolean existsByName(String name);
    
    boolean existsByBlobstoreKey(String blobstoreKey);
    
    boolean existsBySourceAndName(String source, String name);
    
    // Complex business logic queries
    @Query("SELECT f FROM Fuzzer f WHERE f.builtin = false AND f.externalContribution = true")
    List<Fuzzer> findExternalNonBuiltinFuzzers();
    
    @Query("SELECT f FROM Fuzzer f WHERE f.differential = true AND f.builtin = false")
    List<Fuzzer> findCustomDifferentialFuzzers();
    
    @Query("SELECT f FROM Fuzzer f WHERE f.untrustedContent = true AND f.hasLargeTestcases = true")
    List<Fuzzer> findUntrustedLargeFuzzers();
    
    @Query("SELECT f FROM Fuzzer f WHERE f.returnCode = 0 AND f.resultTimestamp >= :since")
    List<Fuzzer> findRecentSuccessfulFuzzers(@Param("since") LocalDateTime since);
    
    // Ordering queries
    List<Fuzzer> findAllByOrderByNameAsc();
    
    List<Fuzzer> findBySourceOrderByNameAsc(String source);
    
    List<Fuzzer> findByBuiltinTrueOrderByNameAsc();
    
    @Query("SELECT f FROM Fuzzer f ORDER BY f.timestamp DESC")
    List<Fuzzer> findAllOrderByTimestampDesc();
    
    @Query("SELECT f FROM Fuzzer f WHERE f.resultTimestamp IS NOT NULL ORDER BY f.resultTimestamp DESC")
    List<Fuzzer> findAllWithResultsOrderByResultTimestampDesc();
}