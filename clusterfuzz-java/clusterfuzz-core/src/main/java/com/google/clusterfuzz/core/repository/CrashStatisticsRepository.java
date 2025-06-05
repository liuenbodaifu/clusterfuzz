package com.google.clusterfuzz.core.repository;

import com.google.clusterfuzz.core.entity.CrashStatistics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for CrashStatistics entities.
 */
@Repository
public interface CrashStatisticsRepository extends JpaRepository<CrashStatistics, Long> {

    /**
     * Find statistics for a specific date and job.
     */
    Optional<CrashStatistics> findByDateAndJobName(LocalDate date, String jobName);

    /**
     * Find statistics for a specific date, job, and fuzzer.
     */
    Optional<CrashStatistics> findByDateAndJobNameAndFuzzerName(LocalDate date, String jobName, String fuzzerName);

    /**
     * Find statistics for a specific date, job, fuzzer, and platform.
     */
    Optional<CrashStatistics> findByDateAndJobNameAndFuzzerNameAndPlatform(
            LocalDate date, String jobName, String fuzzerName, String platform);

    /**
     * Find statistics for a job within a date range.
     */
    @Query("SELECT cs FROM CrashStatistics cs WHERE cs.jobName = :jobName AND cs.date BETWEEN :startDate AND :endDate ORDER BY cs.date")
    List<CrashStatistics> findByJobNameAndDateBetween(
            @Param("jobName") String jobName,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Find statistics for a fuzzer within a date range.
     */
    @Query("SELECT cs FROM CrashStatistics cs WHERE cs.fuzzerName = :fuzzerName AND cs.date BETWEEN :startDate AND :endDate ORDER BY cs.date")
    List<CrashStatistics> findByFuzzerNameAndDateBetween(
            @Param("fuzzerName") String fuzzerName,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Find statistics for a platform within a date range.
     */
    @Query("SELECT cs FROM CrashStatistics cs WHERE cs.platform = :platform AND cs.date BETWEEN :startDate AND :endDate ORDER BY cs.date")
    List<CrashStatistics> findByPlatformAndDateBetween(
            @Param("platform") String platform,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Get aggregated statistics for a job over a date range.
     */
    @Query("SELECT " +
           "SUM(cs.totalCrashes), " +
           "SUM(cs.uniqueCrashes), " +
           "SUM(cs.securityCrashes), " +
           "SUM(cs.totalExecutions), " +
           "SUM(cs.totalFuzzingTimeSeconds), " +
           "SUM(cs.bugsFiled) " +
           "FROM CrashStatistics cs " +
           "WHERE cs.jobName = :jobName AND cs.date BETWEEN :startDate AND :endDate")
    Object[] getAggregatedStatsByJob(
            @Param("jobName") String jobName,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Get aggregated statistics for a fuzzer over a date range.
     */
    @Query("SELECT " +
           "SUM(cs.totalCrashes), " +
           "SUM(cs.uniqueCrashes), " +
           "SUM(cs.securityCrashes), " +
           "SUM(cs.totalExecutions), " +
           "SUM(cs.totalFuzzingTimeSeconds), " +
           "AVG(cs.avgExecutionsPerSecond) " +
           "FROM CrashStatistics cs " +
           "WHERE cs.fuzzerName = :fuzzerName AND cs.date BETWEEN :startDate AND :endDate")
    Object[] getAggregatedStatsByFuzzer(
            @Param("fuzzerName") String fuzzerName,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Get daily crash trends for a job.
     */
    @Query("SELECT cs.date, cs.totalCrashes, cs.uniqueCrashes, cs.securityCrashes " +
           "FROM CrashStatistics cs " +
           "WHERE cs.jobName = :jobName AND cs.date BETWEEN :startDate AND :endDate " +
           "ORDER BY cs.date")
    List<Object[]> getDailyCrashTrends(
            @Param("jobName") String jobName,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Get performance trends for a job.
     */
    @Query("SELECT cs.date, cs.totalExecutions, cs.avgExecutionsPerSecond, cs.totalCoverage " +
           "FROM CrashStatistics cs " +
           "WHERE cs.jobName = :jobName AND cs.date BETWEEN :startDate AND :endDate " +
           "ORDER BY cs.date")
    List<Object[]> getPerformanceTrends(
            @Param("jobName") String jobName,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Get top performing jobs by unique crashes.
     */
    @Query("SELECT cs.jobName, SUM(cs.uniqueCrashes) as totalUniqueCrashes " +
           "FROM CrashStatistics cs " +
           "WHERE cs.date BETWEEN :startDate AND :endDate " +
           "GROUP BY cs.jobName " +
           "ORDER BY totalUniqueCrashes DESC")
    List<Object[]> getTopJobsByUniqueCrashes(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Get top performing fuzzers by crash rate.
     */
    @Query("SELECT cs.fuzzerName, " +
           "SUM(cs.totalCrashes) as totalCrashes, " +
           "SUM(cs.totalExecutions) as totalExecutions, " +
           "(CAST(SUM(cs.totalCrashes) AS DOUBLE) / SUM(cs.totalExecutions)) as crashRate " +
           "FROM CrashStatistics cs " +
           "WHERE cs.fuzzerName IS NOT NULL AND cs.date BETWEEN :startDate AND :endDate " +
           "GROUP BY cs.fuzzerName " +
           "HAVING SUM(cs.totalExecutions) > 0 " +
           "ORDER BY crashRate DESC")
    List<Object[]> getTopFuzzersByCrashRate(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Get crash type distribution for a job.
     */
    @Query("SELECT " +
           "SUM(cs.heapBufferOverflowCrashes), " +
           "SUM(cs.stackBufferOverflowCrashes), " +
           "SUM(cs.useAfterFreeCrashes), " +
           "SUM(cs.nullPointerCrashes), " +
           "SUM(cs.assertionFailures), " +
           "SUM(cs.timeoutCrashes), " +
           "SUM(cs.outOfMemoryCrashes) " +
           "FROM CrashStatistics cs " +
           "WHERE cs.jobName = :jobName AND cs.date BETWEEN :startDate AND :endDate")
    Object[] getCrashTypeDistribution(
            @Param("jobName") String jobName,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Get security crash statistics.
     */
    @Query("SELECT cs.jobName, SUM(cs.securityCrashes), SUM(cs.totalCrashes) " +
           "FROM CrashStatistics cs " +
           "WHERE cs.date BETWEEN :startDate AND :endDate " +
           "GROUP BY cs.jobName " +
           "HAVING SUM(cs.securityCrashes) > 0 " +
           "ORDER BY SUM(cs.securityCrashes) DESC")
    List<Object[]> getSecurityCrashStats(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Get efficiency metrics (crashes per CPU hour).
     */
    @Query("SELECT cs.jobName, " +
           "SUM(cs.uniqueCrashes) as uniqueCrashes, " +
           "SUM(cs.cpuHours) as cpuHours, " +
           "(CAST(SUM(cs.uniqueCrashes) AS DOUBLE) / SUM(cs.cpuHours)) as efficiency " +
           "FROM CrashStatistics cs " +
           "WHERE cs.cpuHours IS NOT NULL AND cs.cpuHours > 0 AND cs.date BETWEEN :startDate AND :endDate " +
           "GROUP BY cs.jobName " +
           "ORDER BY efficiency DESC")
    List<Object[]> getEfficiencyMetrics(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Get coverage growth trends.
     */
    @Query("SELECT cs.date, cs.totalCoverage, cs.newCoverage " +
           "FROM CrashStatistics cs " +
           "WHERE cs.jobName = :jobName AND cs.totalCoverage IS NOT NULL AND cs.date BETWEEN :startDate AND :endDate " +
           "ORDER BY cs.date")
    List<Object[]> getCoverageGrowthTrends(
            @Param("jobName") String jobName,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Get corpus growth statistics.
     */
    @Query("SELECT cs.date, cs.corpusFilesGenerated, cs.corpusSizeBytes " +
           "FROM CrashStatistics cs " +
           "WHERE cs.jobName = :jobName AND cs.date BETWEEN :startDate AND :endDate " +
           "ORDER BY cs.date")
    List<Object[]> getCorpusGrowthStats(
            @Param("jobName") String jobName,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Get bug filing statistics.
     */
    @Query("SELECT cs.jobName, SUM(cs.bugsFiled), SUM(cs.uniqueCrashes) " +
           "FROM CrashStatistics cs " +
           "WHERE cs.date BETWEEN :startDate AND :endDate " +
           "GROUP BY cs.jobName " +
           "ORDER BY SUM(cs.bugsFiled) DESC")
    List<Object[]> getBugFilingStats(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Get platform comparison statistics.
     */
    @Query("SELECT cs.platform, " +
           "SUM(cs.totalCrashes), " +
           "SUM(cs.uniqueCrashes), " +
           "SUM(cs.totalExecutions), " +
           "AVG(cs.avgExecutionsPerSecond) " +
           "FROM CrashStatistics cs " +
           "WHERE cs.platform IS NOT NULL AND cs.date BETWEEN :startDate AND :endDate " +
           "GROUP BY cs.platform")
    List<Object[]> getPlatformComparisonStats(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Find statistics for the latest date.
     */
    @Query("SELECT cs FROM CrashStatistics cs WHERE cs.date = (SELECT MAX(cs2.date) FROM CrashStatistics cs2)")
    List<CrashStatistics> findLatestStatistics();

    /**
     * Find statistics for a specific date.
     */
    List<CrashStatistics> findByDate(LocalDate date);

    /**
     * Get all unique job names.
     */
    @Query("SELECT DISTINCT cs.jobName FROM CrashStatistics cs ORDER BY cs.jobName")
    List<String> findAllJobNames();

    /**
     * Get all unique fuzzer names.
     */
    @Query("SELECT DISTINCT cs.fuzzerName FROM CrashStatistics cs WHERE cs.fuzzerName IS NOT NULL ORDER BY cs.fuzzerName")
    List<String> findAllFuzzerNames();

    /**
     * Get all unique platforms.
     */
    @Query("SELECT DISTINCT cs.platform FROM CrashStatistics cs WHERE cs.platform IS NOT NULL ORDER BY cs.platform")
    List<String> findAllPlatforms();

    /**
     * Get date range of available statistics.
     */
    @Query("SELECT MIN(cs.date), MAX(cs.date) FROM CrashStatistics cs")
    Object[] getDateRange();

    /**
     * Delete old statistics beyond retention period.
     */
    @Query("DELETE FROM CrashStatistics cs WHERE cs.date < :cutoffDate")
    int deleteOldStatistics(@Param("cutoffDate") LocalDate cutoffDate);
}