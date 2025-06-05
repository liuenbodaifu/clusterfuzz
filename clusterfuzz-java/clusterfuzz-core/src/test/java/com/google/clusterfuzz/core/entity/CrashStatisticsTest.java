package com.google.clusterfuzz.core.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for CrashStatistics entity.
 */
@DisplayName("CrashStatistics Entity Tests")
class CrashStatisticsTest {

    private Validator validator;
    private CrashStatistics.CrashStatisticsBuilder validStatsBuilder;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        
        validStatsBuilder = CrashStatistics.builder()
                .date(LocalDate.now())
                .jobName("test-job")
                .fuzzerName("libfuzzer")
                .platform("linux")
                .totalCrashes(100L)
                .uniqueCrashes(50L)
                .securityCrashes(10L)
                .totalExecutions(1000000L)
                .totalFuzzingTimeSeconds(3600L)
                .avgExecutionsPerSecond(277.8)
                .corpusFilesGenerated(25L)
                .bugsFiled(5L);
    }

    @Test
    @DisplayName("Should create valid crash statistics with all required fields")
    void shouldCreateValidCrashStatistics() {
        CrashStatistics stats = validStatsBuilder.build();
        
        Set<ConstraintViolation<CrashStatistics>> violations = validator.validate(stats);
        assertTrue(violations.isEmpty(), "Valid crash statistics should have no validation violations");
        
        assertEquals(LocalDate.now(), stats.getDate());
        assertEquals("test-job", stats.getJobName());
        assertEquals("libfuzzer", stats.getFuzzerName());
        assertEquals("linux", stats.getPlatform());
        assertEquals(100L, stats.getTotalCrashes());
        assertEquals(50L, stats.getUniqueCrashes());
    }

    @Test
    @DisplayName("Should fail validation when date is null")
    void shouldFailValidationWhenDateIsNull() {
        CrashStatistics stats = validStatsBuilder.date(null).build();
        
        Set<ConstraintViolation<CrashStatistics>> violations = validator.validate(stats);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("date")));
    }

    @Test
    @DisplayName("Should fail validation when job name is null")
    void shouldFailValidationWhenJobNameIsNull() {
        CrashStatistics stats = validStatsBuilder.jobName(null).build();
        
        Set<ConstraintViolation<CrashStatistics>> violations = validator.validate(stats);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("jobName")));
    }

    @Test
    @DisplayName("Should fail validation when job name is blank")
    void shouldFailValidationWhenJobNameIsBlank() {
        CrashStatistics stats = validStatsBuilder.jobName("   ").build();
        
        Set<ConstraintViolation<CrashStatistics>> violations = validator.validate(stats);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("jobName")));
    }

    @Test
    @DisplayName("Should fail validation when job name exceeds max length")
    void shouldFailValidationWhenJobNameExceedsMaxLength() {
        String longJobName = "a".repeat(101); // Max is 100
        CrashStatistics stats = validStatsBuilder.jobName(longJobName).build();
        
        Set<ConstraintViolation<CrashStatistics>> violations = validator.validate(stats);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("jobName")));
    }

    @Test
    @DisplayName("Should calculate crash rate correctly")
    void shouldCalculateCrashRateCorrectly() {
        CrashStatistics stats = validStatsBuilder
                .totalCrashes(100L)
                .totalExecutions(10000L)
                .build();
        
        double crashRate = stats.getCrashRate();
        assertEquals(0.01, crashRate, 0.001);
    }

    @Test
    @DisplayName("Should return zero crash rate when no executions")
    void shouldReturnZeroCrashRateWhenNoExecutions() {
        CrashStatistics stats = validStatsBuilder
                .totalCrashes(100L)
                .totalExecutions(0L)
                .build();
        
        double crashRate = stats.getCrashRate();
        assertEquals(0.0, crashRate);
    }

    @Test
    @DisplayName("Should calculate unique crash rate correctly")
    void shouldCalculateUniqueCrashRateCorrectly() {
        CrashStatistics stats = validStatsBuilder
                .uniqueCrashes(25L)
                .totalExecutions(10000L)
                .build();
        
        double uniqueCrashRate = stats.getUniqueCrashRate();
        assertEquals(0.0025, uniqueCrashRate, 0.0001);
    }

    @Test
    @DisplayName("Should calculate security crash percentage correctly")
    void shouldCalculateSecurityCrashPercentageCorrectly() {
        CrashStatistics stats = validStatsBuilder
                .securityCrashes(20L)
                .totalCrashes(100L)
                .build();
        
        double securityPercentage = stats.getSecurityCrashPercentage();
        assertEquals(20.0, securityPercentage, 0.1);
    }

    @Test
    @DisplayName("Should return zero security crash percentage when no crashes")
    void shouldReturnZeroSecurityCrashPercentageWhenNoCrashes() {
        CrashStatistics stats = validStatsBuilder
                .securityCrashes(0L)
                .totalCrashes(0L)
                .build();
        
        double securityPercentage = stats.getSecurityCrashPercentage();
        assertEquals(0.0, securityPercentage);
    }

    @Test
    @DisplayName("Should calculate bug filing rate correctly")
    void shouldCalculateBugFilingRateCorrectly() {
        CrashStatistics stats = validStatsBuilder
                .bugsFiled(15L)
                .uniqueCrashes(50L)
                .build();
        
        double bugFilingRate = stats.getBugFilingRate();
        assertEquals(0.3, bugFilingRate, 0.01);
    }

    @Test
    @DisplayName("Should return zero bug filing rate when no unique crashes")
    void shouldReturnZeroBugFilingRateWhenNoUniqueCrashes() {
        CrashStatistics stats = validStatsBuilder
                .bugsFiled(5L)
                .uniqueCrashes(0L)
                .build();
        
        double bugFilingRate = stats.getBugFilingRate();
        assertEquals(0.0, bugFilingRate);
    }

    @Test
    @DisplayName("Should calculate duplicate rate correctly")
    void shouldCalculateDuplicateRateCorrectly() {
        CrashStatistics stats = validStatsBuilder
                .duplicateCrashes(30L)
                .totalCrashes(100L)
                .build();
        
        double duplicateRate = stats.getDuplicateRate();
        assertEquals(0.3, duplicateRate, 0.01);
    }

    @Test
    @DisplayName("Should calculate efficiency score correctly")
    void shouldCalculateEfficiencyScoreCorrectly() {
        CrashStatistics stats = validStatsBuilder
                .uniqueCrashes(50L)
                .cpuHours(10.0)
                .build();
        
        double efficiencyScore = stats.getEfficiencyScore();
        assertEquals(5.0, efficiencyScore, 0.1);
    }

    @Test
    @DisplayName("Should return zero efficiency score when no CPU hours")
    void shouldReturnZeroEfficiencyScoreWhenNoCpuHours() {
        CrashStatistics stats = validStatsBuilder
                .uniqueCrashes(50L)
                .cpuHours(0.0)
                .build();
        
        double efficiencyScore = stats.getEfficiencyScore();
        assertEquals(0.0, efficiencyScore);
    }

    @Test
    @DisplayName("Should return zero efficiency score when CPU hours is null")
    void shouldReturnZeroEfficiencyScoreWhenCpuHoursIsNull() {
        CrashStatistics stats = validStatsBuilder
                .uniqueCrashes(50L)
                .cpuHours(null)
                .build();
        
        double efficiencyScore = stats.getEfficiencyScore();
        assertEquals(0.0, efficiencyScore);
    }

    @Test
    @DisplayName("Should identify most common crash type correctly")
    void shouldIdentifyMostCommonCrashTypeCorrectly() {
        CrashStatistics stats = validStatsBuilder
                .heapBufferOverflowCrashes(30L)
                .stackBufferOverflowCrashes(20L)
                .useAfterFreeCrashes(40L) // This should be the highest
                .nullPointerCrashes(10L)
                .build();
        
        String mostCommonType = stats.getMostCommonCrashType();
        assertEquals("use-after-free", mostCommonType);
    }

    @Test
    @DisplayName("Should identify heap buffer overflow as most common crash type")
    void shouldIdentifyHeapBufferOverflowAsMostCommonCrashType() {
        CrashStatistics stats = validStatsBuilder
                .heapBufferOverflowCrashes(50L) // This should be the highest
                .stackBufferOverflowCrashes(20L)
                .useAfterFreeCrashes(30L)
                .nullPointerCrashes(10L)
                .build();
        
        String mostCommonType = stats.getMostCommonCrashType();
        assertEquals("heap-buffer-overflow", mostCommonType);
    }

    @Test
    @DisplayName("Should return unknown when no crashes")
    void shouldReturnUnknownWhenNoCrashes() {
        CrashStatistics stats = validStatsBuilder
                .heapBufferOverflowCrashes(0L)
                .stackBufferOverflowCrashes(0L)
                .useAfterFreeCrashes(0L)
                .nullPointerCrashes(0L)
                .assertionFailures(0L)
                .timeoutCrashes(0L)
                .outOfMemoryCrashes(0L)
                .build();
        
        String mostCommonType = stats.getMostCommonCrashType();
        assertEquals("unknown", mostCommonType);
    }

    @Test
    @DisplayName("Should add statistics correctly")
    void shouldAddStatisticsCorrectly() {
        CrashStatistics stats1 = validStatsBuilder
                .totalCrashes(100L)
                .uniqueCrashes(50L)
                .securityCrashes(10L)
                .totalExecutions(10000L)
                .totalFuzzingTimeSeconds(3600L)
                .corpusFilesGenerated(25L)
                .bugsFiled(5L)
                .build();
        
        CrashStatistics stats2 = CrashStatistics.builder()
                .totalCrashes(50L)
                .uniqueCrashes(25L)
                .securityCrashes(5L)
                .totalExecutions(5000L)
                .totalFuzzingTimeSeconds(1800L)
                .corpusFilesGenerated(15L)
                .bugsFiled(3L)
                .build();
        
        stats1.addStatistics(stats2);
        
        assertEquals(150L, stats1.getTotalCrashes());
        assertEquals(75L, stats1.getUniqueCrashes());
        assertEquals(15L, stats1.getSecurityCrashes());
        assertEquals(15000L, stats1.getTotalExecutions());
        assertEquals(5400L, stats1.getTotalFuzzingTimeSeconds());
        assertEquals(40L, stats1.getCorpusFilesGenerated());
        assertEquals(8L, stats1.getBugsFiled());
        
        // Check that derived field is recalculated
        double expectedAvgExecPerSec = 15000.0 / 5400.0;
        assertEquals(expectedAvgExecPerSec, stats1.getAvgExecutionsPerSecond(), 0.01);
    }

    @Test
    @DisplayName("Should handle crash type statistics correctly")
    void shouldHandleCrashTypeStatisticsCorrectly() {
        CrashStatistics stats = validStatsBuilder
                .heapBufferOverflowCrashes(25L)
                .stackBufferOverflowCrashes(15L)
                .useAfterFreeCrashes(20L)
                .nullPointerCrashes(10L)
                .assertionFailures(5L)
                .timeoutCrashes(3L)
                .outOfMemoryCrashes(2L)
                .build();
        
        assertEquals(25L, stats.getHeapBufferOverflowCrashes());
        assertEquals(15L, stats.getStackBufferOverflowCrashes());
        assertEquals(20L, stats.getUseAfterFreeCrashes());
        assertEquals(10L, stats.getNullPointerCrashes());
        assertEquals(5L, stats.getAssertionFailures());
        assertEquals(3L, stats.getTimeoutCrashes());
        assertEquals(2L, stats.getOutOfMemoryCrashes());
    }

    @Test
    @DisplayName("Should initialize default values correctly")
    void shouldInitializeDefaultValuesCorrectly() {
        CrashStatistics stats = CrashStatistics.builder()
                .date(LocalDate.now())
                .jobName("test-job")
                .build();
        
        assertEquals(0L, stats.getTotalCrashes());
        assertEquals(0L, stats.getUniqueCrashes());
        assertEquals(0L, stats.getSecurityCrashes());
        assertEquals(0L, stats.getTotalExecutions());
        assertEquals(0L, stats.getTotalFuzzingTimeSeconds());
        assertEquals(0L, stats.getCorpusFilesGenerated());
        assertEquals(0L, stats.getBugsFiled());
        assertEquals(0L, stats.getDuplicateCrashes());
        assertEquals(0L, stats.getUnreproducibleCrashes());
        assertEquals(0L, stats.getMinimizedCrashes());
    }

    @Test
    @DisplayName("Should handle coverage statistics correctly")
    void shouldHandleCoverageStatisticsCorrectly() {
        CrashStatistics stats = validStatsBuilder
                .totalCoverage(50000L)
                .newCoverage(1500L)
                .build();
        
        assertEquals(50000L, stats.getTotalCoverage());
        assertEquals(1500L, stats.getNewCoverage());
    }

    @Test
    @DisplayName("Should handle resource usage statistics correctly")
    void shouldHandleResourceUsageStatisticsCorrectly() {
        CrashStatistics stats = validStatsBuilder
                .activeBots(5)
                .cpuHours(24.5)
                .memoryGbHours(128.0)
                .build();
        
        assertEquals(5, stats.getActiveBots());
        assertEquals(24.5, stats.getCpuHours(), 0.1);
        assertEquals(128.0, stats.getMemoryGbHours(), 0.1);
    }

    @Test
    @DisplayName("Should validate fuzzer name length")
    void shouldValidateFuzzerNameLength() {
        String longFuzzerName = "a".repeat(101); // Max is 100
        CrashStatistics stats = validStatsBuilder.fuzzerName(longFuzzerName).build();
        
        Set<ConstraintViolation<CrashStatistics>> violations = validator.validate(stats);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("fuzzerName")));
    }

    @Test
    @DisplayName("Should validate platform length")
    void shouldValidatePlatformLength() {
        String longPlatform = "a".repeat(51); // Max is 50
        CrashStatistics stats = validStatsBuilder.platform(longPlatform).build();
        
        Set<ConstraintViolation<CrashStatistics>> violations = validator.validate(stats);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("platform")));
    }
}