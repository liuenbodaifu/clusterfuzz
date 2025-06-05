package com.google.clusterfuzz.core.repository;

import com.google.clusterfuzz.core.entity.CoverageInformation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for CoverageInformation entities.
 * Provides data access methods for coverage analysis and reporting.
 */
@Repository
public interface CoverageInformationRepository extends JpaRepository<CoverageInformation, Long> {

    // Basic queries
    List<CoverageInformation> findByFuzzer(String fuzzer);
    
    List<CoverageInformation> findByFuzzerOrderByDateDesc(String fuzzer);
    
    List<CoverageInformation> findByDate(LocalDate date);
    
    Optional<CoverageInformation> findByFuzzerAndDate(String fuzzer, LocalDate date);

    // Latest coverage queries
    @Query("SELECT c FROM CoverageInformation c WHERE c.fuzzer = :fuzzer ORDER BY c.date DESC LIMIT 1")
    Optional<CoverageInformation> findLatestByFuzzer(@Param("fuzzer") String fuzzer);
    
    @Query("SELECT c FROM CoverageInformation c WHERE c.date = (SELECT MAX(c2.date) FROM CoverageInformation c2 WHERE c2.fuzzer = c.fuzzer) ORDER BY c.fuzzer")
    List<CoverageInformation> findLatestForAllFuzzers();

    // Date range queries
    List<CoverageInformation> findByDateBetween(LocalDate startDate, LocalDate endDate);
    
    List<CoverageInformation> findByFuzzerAndDateBetween(String fuzzer, LocalDate startDate, LocalDate endDate);
    
    @Query("SELECT c FROM CoverageInformation c WHERE c.date >= :since ORDER BY c.date DESC, c.fuzzer")
    List<CoverageInformation> findRecentCoverage(@Param("since") LocalDate since);

    // Function coverage queries
    @Query("SELECT c FROM CoverageInformation c WHERE c.functionsCovered IS NOT NULL ORDER BY c.functionsCovered DESC")
    List<CoverageInformation> findByFunctionsCoveredNotNullOrderByFunctionsCoveredDesc();
    
    @Query("SELECT c FROM CoverageInformation c WHERE c.fuzzer = :fuzzer AND c.functionsCovered IS NOT NULL ORDER BY c.date DESC")
    List<CoverageInformation> findFunctionCoverageByFuzzer(@Param("fuzzer") String fuzzer);
    
    @Query("SELECT c FROM CoverageInformation c WHERE c.functionsCovered >= :minCovered ORDER BY c.functionsCovered DESC")
    List<CoverageInformation> findByFunctionsCoveredGreaterThanEqual(@Param("minCovered") Integer minCovered);

    // Edge coverage queries
    @Query("SELECT c FROM CoverageInformation c WHERE c.edgesCovered IS NOT NULL ORDER BY c.edgesCovered DESC")
    List<CoverageInformation> findByEdgesCoveredNotNullOrderByEdgesCoveredDesc();
    
    @Query("SELECT c FROM CoverageInformation c WHERE c.fuzzer = :fuzzer AND c.edgesCovered IS NOT NULL ORDER BY c.date DESC")
    List<CoverageInformation> findEdgeCoverageByFuzzer(@Param("fuzzer") String fuzzer);
    
    @Query("SELECT c FROM CoverageInformation c WHERE c.edgesCovered >= :minCovered ORDER BY c.edgesCovered DESC")
    List<CoverageInformation> findByEdgesCoveredGreaterThanEqual(@Param("minCovered") Integer minCovered);

    // Coverage percentage calculations
    @Query("SELECT c FROM CoverageInformation c WHERE c.functionsCovered IS NOT NULL AND c.functionsTotal IS NOT NULL AND c.functionsTotal > 0 " +
           "ORDER BY (c.functionsCovered * 100.0 / c.functionsTotal) DESC")
    List<CoverageInformation> findOrderByFunctionCoveragePercentageDesc();
    
    @Query("SELECT c FROM CoverageInformation c WHERE c.edgesCovered IS NOT NULL AND c.edgesTotal IS NOT NULL AND c.edgesTotal > 0 " +
           "ORDER BY (c.edgesCovered * 100.0 / c.edgesTotal) DESC")
    List<CoverageInformation> findOrderByEdgeCoveragePercentageDesc();

    // Corpus size queries
    @Query("SELECT c FROM CoverageInformation c WHERE c.corpusSizeUnits IS NOT NULL AND c.corpusSizeUnits > 0 ORDER BY c.corpusSizeUnits DESC")
    List<CoverageInformation> findWithCorpusOrderBySizeDesc();
    
    @Query("SELECT c FROM CoverageInformation c WHERE c.fuzzer = :fuzzer AND c.corpusSizeUnits IS NOT NULL ORDER BY c.date DESC")
    List<CoverageInformation> findCorpusSizeByFuzzer(@Param("fuzzer") String fuzzer);
    
    @Query("SELECT c FROM CoverageInformation c WHERE c.corpusSizeBytes >= :minBytes ORDER BY c.corpusSizeBytes DESC")
    List<CoverageInformation> findByCorpusSizeBytesGreaterThanEqual(@Param("minBytes") Integer minBytes);

    // Quarantine queries
    @Query("SELECT c FROM CoverageInformation c WHERE c.quarantineSizeUnits IS NOT NULL AND c.quarantineSizeUnits > 0 ORDER BY c.quarantineSizeUnits DESC")
    List<CoverageInformation> findWithQuarantineOrderBySizeDesc();
    
    @Query("SELECT c FROM CoverageInformation c WHERE c.fuzzer = :fuzzer AND c.quarantineSizeUnits IS NOT NULL ORDER BY c.date DESC")
    List<CoverageInformation> findQuarantineSizeByFuzzer(@Param("fuzzer") String fuzzer);

    // HTML report queries
    @Query("SELECT c FROM CoverageInformation c WHERE c.htmlReportUrl IS NOT NULL AND c.htmlReportUrl != ''")
    List<CoverageInformation> findWithHtmlReports();
    
    @Query("SELECT c FROM CoverageInformation c WHERE c.fuzzer = :fuzzer AND c.htmlReportUrl IS NOT NULL AND c.htmlReportUrl != '' ORDER BY c.date DESC")
    List<CoverageInformation> findHtmlReportsByFuzzer(@Param("fuzzer") String fuzzer);

    // Statistics queries
    @Query("SELECT COUNT(c) FROM CoverageInformation c WHERE c.fuzzer = :fuzzer")
    long countByFuzzer(@Param("fuzzer") String fuzzer);
    
    @Query("SELECT COUNT(DISTINCT c.fuzzer) FROM CoverageInformation c")
    long countDistinctFuzzers();
    
    @Query("SELECT c.fuzzer, COUNT(c) FROM CoverageInformation c GROUP BY c.fuzzer ORDER BY COUNT(c) DESC")
    List<Object[]> getCoverageCountsByFuzzer();

    // Trend analysis queries
    @Query("SELECT c FROM CoverageInformation c WHERE c.fuzzer = :fuzzer AND c.date >= :since ORDER BY c.date ASC")
    List<CoverageInformation> findCoverageTrendByFuzzer(@Param("fuzzer") String fuzzer, @Param("since") LocalDate since);
    
    @Query("SELECT c.date, AVG(CASE WHEN c.functionsTotal > 0 THEN c.functionsCovered * 100.0 / c.functionsTotal ELSE 0 END) " +
           "FROM CoverageInformation c WHERE c.date >= :since GROUP BY c.date ORDER BY c.date")
    List<Object[]> getAverageFunctionCoverageTrend(@Param("since") LocalDate since);
    
    @Query("SELECT c.date, AVG(CASE WHEN c.edgesTotal > 0 THEN c.edgesCovered * 100.0 / c.edgesTotal ELSE 0 END) " +
           "FROM CoverageInformation c WHERE c.date >= :since GROUP BY c.date ORDER BY c.date")
    List<Object[]> getAverageEdgeCoverageTrend(@Param("since") LocalDate since);

    // Comparison queries
    @Query("SELECT c1 FROM CoverageInformation c1 WHERE c1.fuzzer = :fuzzer AND c1.date = :date1")
    Optional<CoverageInformation> findForComparison(@Param("fuzzer") String fuzzer, @Param("date1") LocalDate date1);
    
    @Query("SELECT c FROM CoverageInformation c WHERE c.fuzzer IN :fuzzers AND c.date = :date ORDER BY c.fuzzer")
    List<CoverageInformation> findForFuzzersOnDate(@Param("fuzzers") List<String> fuzzers, @Param("date") LocalDate date);

    // Best coverage queries
    @Query("SELECT c FROM CoverageInformation c WHERE c.fuzzer = :fuzzer AND c.functionsCovered = " +
           "(SELECT MAX(c2.functionsCovered) FROM CoverageInformation c2 WHERE c2.fuzzer = :fuzzer)")
    List<CoverageInformation> findBestFunctionCoverageByFuzzer(@Param("fuzzer") String fuzzer);
    
    @Query("SELECT c FROM CoverageInformation c WHERE c.fuzzer = :fuzzer AND c.edgesCovered = " +
           "(SELECT MAX(c2.edgesCovered) FROM CoverageInformation c2 WHERE c2.fuzzer = :fuzzer)")
    List<CoverageInformation> findBestEdgeCoverageByFuzzer(@Param("fuzzer") String fuzzer);

    // Aggregation queries
    @Query("SELECT c.fuzzer, " +
           "MAX(c.functionsCovered), MAX(c.edgesCovered), " +
           "MAX(c.corpusSizeUnits), MAX(c.corpusSizeBytes), " +
           "COUNT(c) " +
           "FROM CoverageInformation c GROUP BY c.fuzzer ORDER BY c.fuzzer")
    List<Object[]> getCoverageSummaryByFuzzer();
    
    @Query("SELECT c.date, " +
           "AVG(CASE WHEN c.functionsTotal > 0 THEN c.functionsCovered * 100.0 / c.functionsTotal ELSE 0 END), " +
           "AVG(CASE WHEN c.edgesTotal > 0 THEN c.edgesCovered * 100.0 / c.edgesTotal ELSE 0 END), " +
           "AVG(c.corpusSizeUnits), COUNT(c) " +
           "FROM CoverageInformation c WHERE c.date >= :since GROUP BY c.date ORDER BY c.date")
    List<Object[]> getDailyCoverageStats(@Param("since") LocalDate since);

    // Cleanup queries
    @Query("SELECT c FROM CoverageInformation c WHERE c.date < :cutoffDate ORDER BY c.date ASC")
    List<CoverageInformation> findOldCoverage(@Param("cutoffDate") LocalDate cutoffDate);
    
    @Query("DELETE FROM CoverageInformation c WHERE c.date < :cutoffDate")
    void deleteOldCoverage(@Param("cutoffDate") LocalDate cutoffDate);

    // Pagination support
    Page<CoverageInformation> findByFuzzer(String fuzzer, Pageable pageable);
    
    Page<CoverageInformation> findByDateBetween(LocalDate startDate, LocalDate endDate, Pageable pageable);

    // Existence checks
    boolean existsByFuzzerAndDate(String fuzzer, LocalDate date);
    
    boolean existsByFuzzer(String fuzzer);

    // Custom complex queries
    @Query("SELECT c FROM CoverageInformation c WHERE " +
           "(:fuzzer IS NULL OR c.fuzzer = :fuzzer) AND " +
           "(:startDate IS NULL OR c.date >= :startDate) AND " +
           "(:endDate IS NULL OR c.date <= :endDate) AND " +
           "(:minFunctionsCovered IS NULL OR c.functionsCovered >= :minFunctionsCovered) AND " +
           "(:minEdgesCovered IS NULL OR c.edgesCovered >= :minEdgesCovered) " +
           "ORDER BY c.date DESC, c.fuzzer")
    Page<CoverageInformation> findWithFilters(
        @Param("fuzzer") String fuzzer,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate,
        @Param("minFunctionsCovered") Integer minFunctionsCovered,
        @Param("minEdgesCovered") Integer minEdgesCovered,
        Pageable pageable
    );
}