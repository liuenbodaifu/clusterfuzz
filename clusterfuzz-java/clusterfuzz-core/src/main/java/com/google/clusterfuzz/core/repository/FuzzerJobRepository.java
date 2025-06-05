package com.google.clusterfuzz.core.repository;

import com.google.clusterfuzz.core.entity.FuzzerJob;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for FuzzerJob entities.
 * Provides data access methods for fuzzer-job mapping operations.
 */
@Repository
public interface FuzzerJobRepository extends JpaRepository<FuzzerJob, Long> {

    // Basic queries
    List<FuzzerJob> findByFuzzer(String fuzzer);
    
    List<FuzzerJob> findByJob(String job);
    
    List<FuzzerJob> findByPlatform(String platform);
    
    Optional<FuzzerJob> findByFuzzerAndJob(String fuzzer, String job);
    
    Optional<FuzzerJob> findByFuzzerAndJobAndPlatform(String fuzzer, String job, String platform);

    // Enabled/disabled queries
    List<FuzzerJob> findByEnabled(Boolean enabled);
    
    List<FuzzerJob> findByFuzzerAndEnabled(String fuzzer, Boolean enabled);
    
    List<FuzzerJob> findByJobAndEnabled(String job, Boolean enabled);
    
    List<FuzzerJob> findByPlatformAndEnabled(String platform, Boolean enabled);

    // Weight-based queries
    List<FuzzerJob> findByWeightGreaterThan(Double weight);
    
    List<FuzzerJob> findByActualWeightGreaterThan(Double actualWeight);
    
    @Query("SELECT fj FROM FuzzerJob fj WHERE fj.enabled = true AND fj.actualWeight > 0 ORDER BY fj.actualWeight DESC")
    List<FuzzerJob> findActiveOrderByActualWeightDesc();
    
    @Query("SELECT fj FROM FuzzerJob fj WHERE fj.fuzzer = :fuzzer AND fj.enabled = true AND fj.actualWeight > 0 ORDER BY fj.actualWeight DESC")
    List<FuzzerJob> findActiveByFuzzerOrderByActualWeightDesc(@Param("fuzzer") String fuzzer);
    
    @Query("SELECT fj FROM FuzzerJob fj WHERE fj.job = :job AND fj.enabled = true AND fj.actualWeight > 0 ORDER BY fj.actualWeight DESC")
    List<FuzzerJob> findActiveByJobOrderByActualWeightDesc(@Param("job") String job);

    // Platform-specific queries
    @Query("SELECT fj FROM FuzzerJob fj WHERE (fj.platform IS NULL OR fj.platform = :platform) AND fj.enabled = true")
    List<FuzzerJob> findApplicableForPlatform(@Param("platform") String platform);
    
    @Query("SELECT fj FROM FuzzerJob fj WHERE fj.fuzzer = :fuzzer AND (fj.platform IS NULL OR fj.platform = :platform) AND fj.enabled = true")
    List<FuzzerJob> findByFuzzerAndApplicableForPlatform(@Param("fuzzer") String fuzzer, @Param("platform") String platform);
    
    @Query("SELECT fj FROM FuzzerJob fj WHERE fj.job = :job AND (fj.platform IS NULL OR fj.platform = :platform) AND fj.enabled = true")
    List<FuzzerJob> findByJobAndApplicableForPlatform(@Param("job") String job, @Param("platform") String platform);

    // Selection queries for fuzzing
    @Query("SELECT fj FROM FuzzerJob fj WHERE fj.job = :job AND fj.enabled = true AND fj.actualWeight > 0 " +
           "AND (fj.platform IS NULL OR fj.platform = :platform) ORDER BY fj.actualWeight DESC")
    List<FuzzerJob> findSelectableFuzzersForJob(@Param("job") String job, @Param("platform") String platform);
    
    @Query("SELECT fj FROM FuzzerJob fj WHERE fj.fuzzer = :fuzzer AND fj.enabled = true AND fj.actualWeight > 0 " +
           "AND (fj.platform IS NULL OR fj.platform = :platform) ORDER BY fj.actualWeight DESC")
    List<FuzzerJob> findSelectableJobsForFuzzer(@Param("fuzzer") String fuzzer, @Param("platform") String platform);

    // Weighted selection (for probabilistic selection)
    @Query("SELECT fj, fj.actualWeight FROM FuzzerJob fj WHERE fj.job = :job AND fj.enabled = true AND fj.actualWeight > 0 " +
           "AND (fj.platform IS NULL OR fj.platform = :platform)")
    List<Object[]> findWeightedFuzzersForJob(@Param("job") String job, @Param("platform") String platform);
    
    @Query("SELECT fj, fj.actualWeight FROM FuzzerJob fj WHERE fj.fuzzer = :fuzzer AND fj.enabled = true AND fj.actualWeight > 0 " +
           "AND (fj.platform IS NULL OR fj.platform = :platform)")
    List<Object[]> findWeightedJobsForFuzzer(@Param("fuzzer") String fuzzer, @Param("platform") String platform);

    // Statistics queries
    @Query("SELECT COUNT(fj) FROM FuzzerJob fj WHERE fj.fuzzer = :fuzzer")
    long countByFuzzer(@Param("fuzzer") String fuzzer);
    
    @Query("SELECT COUNT(fj) FROM FuzzerJob fj WHERE fj.job = :job")
    long countByJob(@Param("job") String job);
    
    @Query("SELECT COUNT(fj) FROM FuzzerJob fj WHERE fj.enabled = true")
    long countEnabled();
    
    @Query("SELECT COUNT(fj) FROM FuzzerJob fj WHERE fj.enabled = true AND fj.actualWeight > 0")
    long countActive();

    // Aggregation queries
    @Query("SELECT fj.fuzzer, COUNT(fj), SUM(fj.actualWeight), AVG(fj.actualWeight) FROM FuzzerJob fj WHERE fj.enabled = true GROUP BY fj.fuzzer ORDER BY fj.fuzzer")
    List<Object[]> getFuzzerStatistics();
    
    @Query("SELECT fj.job, COUNT(fj), SUM(fj.actualWeight), AVG(fj.actualWeight) FROM FuzzerJob fj WHERE fj.enabled = true GROUP BY fj.job ORDER BY fj.job")
    List<Object[]> getJobStatistics();
    
    @Query("SELECT fj.platform, COUNT(fj), SUM(fj.actualWeight) FROM FuzzerJob fj WHERE fj.enabled = true GROUP BY fj.platform ORDER BY fj.platform")
    List<Object[]> getPlatformStatistics();

    // Weight distribution queries
    @Query("SELECT SUM(fj.actualWeight) FROM FuzzerJob fj WHERE fj.job = :job AND fj.enabled = true AND (fj.platform IS NULL OR fj.platform = :platform)")
    Double getTotalWeightForJob(@Param("job") String job, @Param("platform") String platform);
    
    @Query("SELECT SUM(fj.actualWeight) FROM FuzzerJob fj WHERE fj.fuzzer = :fuzzer AND fj.enabled = true AND (fj.platform IS NULL OR fj.platform = :platform)")
    Double getTotalWeightForFuzzer(@Param("fuzzer") String fuzzer, @Param("platform") String platform);

    // Configuration queries
    @Query("SELECT fj FROM FuzzerJob fj WHERE fj.configuration IS NOT NULL AND fj.configuration != ''")
    List<FuzzerJob> findWithConfiguration();
    
    @Query("SELECT fj FROM FuzzerJob fj WHERE fj.fuzzer = :fuzzer AND fj.configuration IS NOT NULL AND fj.configuration != ''")
    List<FuzzerJob> findWithConfigurationByFuzzer(@Param("fuzzer") String fuzzer);

    // Bulk operations
    @Modifying
    @Query("UPDATE FuzzerJob fj SET fj.enabled = :enabled WHERE fj.fuzzer = :fuzzer")
    int updateEnabledByFuzzer(@Param("fuzzer") String fuzzer, @Param("enabled") Boolean enabled);
    
    @Modifying
    @Query("UPDATE FuzzerJob fj SET fj.enabled = :enabled WHERE fj.job = :job")
    int updateEnabledByJob(@Param("job") String job, @Param("enabled") Boolean enabled);
    
    @Modifying
    @Query("UPDATE FuzzerJob fj SET fj.weight = :weight, fj.actualWeight = fj.weight * fj.multiplier WHERE fj.fuzzer = :fuzzer")
    int updateWeightByFuzzer(@Param("fuzzer") String fuzzer, @Param("weight") Double weight);
    
    @Modifying
    @Query("UPDATE FuzzerJob fj SET fj.multiplier = :multiplier, fj.actualWeight = fj.weight * fj.multiplier WHERE fj.job = :job")
    int updateMultiplierByJob(@Param("job") String job, @Param("multiplier") Double multiplier);

    // Recalculate actual weights
    @Modifying
    @Query("UPDATE FuzzerJob fj SET fj.actualWeight = fj.weight * fj.multiplier")
    int recalculateAllActualWeights();
    
    @Modifying
    @Query("UPDATE FuzzerJob fj SET fj.actualWeight = fj.weight * fj.multiplier WHERE fj.fuzzer = :fuzzer")
    int recalculateActualWeightsByFuzzer(@Param("fuzzer") String fuzzer);

    // Existence checks
    boolean existsByFuzzerAndJob(String fuzzer, String job);
    
    boolean existsByFuzzerAndJobAndPlatform(String fuzzer, String job, String platform);
    
    @Query("SELECT CASE WHEN COUNT(fj) > 0 THEN true ELSE false END FROM FuzzerJob fj WHERE fj.fuzzer = :fuzzer AND fj.enabled = true")
    boolean hasEnabledMappingsForFuzzer(@Param("fuzzer") String fuzzer);
    
    @Query("SELECT CASE WHEN COUNT(fj) > 0 THEN true ELSE false END FROM FuzzerJob fj WHERE fj.job = :job AND fj.enabled = true")
    boolean hasEnabledMappingsForJob(@Param("job") String job);

    // Pagination support
    Page<FuzzerJob> findByFuzzer(String fuzzer, Pageable pageable);
    
    Page<FuzzerJob> findByJob(String job, Pageable pageable);
    
    Page<FuzzerJob> findByEnabled(Boolean enabled, Pageable pageable);

    // Complex filtering
    @Query("SELECT fj FROM FuzzerJob fj WHERE " +
           "(:fuzzer IS NULL OR fj.fuzzer = :fuzzer) AND " +
           "(:job IS NULL OR fj.job = :job) AND " +
           "(:platform IS NULL OR fj.platform = :platform OR fj.platform IS NULL) AND " +
           "(:enabled IS NULL OR fj.enabled = :enabled) AND " +
           "(:minWeight IS NULL OR fj.actualWeight >= :minWeight) " +
           "ORDER BY fj.actualWeight DESC")
    Page<FuzzerJob> findWithFilters(
        @Param("fuzzer") String fuzzer,
        @Param("job") String job,
        @Param("platform") String platform,
        @Param("enabled") Boolean enabled,
        @Param("minWeight") Double minWeight,
        Pageable pageable
    );

    // Unique constraint helpers
    @Query("SELECT fj FROM FuzzerJob fj WHERE fj.fuzzer = :fuzzer AND fj.job = :job AND " +
           "(:platform IS NULL AND fj.platform IS NULL OR fj.platform = :platform)")
    Optional<FuzzerJob> findExistingMapping(@Param("fuzzer") String fuzzer, @Param("job") String job, @Param("platform") String platform);

    // Cleanup queries
    @Query("SELECT fj FROM FuzzerJob fj WHERE fj.enabled = false AND fj.updatedAt < :cutoffDate")
    List<FuzzerJob> findDisabledOlderThan(@Param("cutoffDate") java.time.LocalDateTime cutoffDate);
    
    @Query("DELETE FROM FuzzerJob fj WHERE fj.enabled = false AND fj.updatedAt < :cutoffDate")
    void deleteDisabledOlderThan(@Param("cutoffDate") java.time.LocalDateTime cutoffDate);
}