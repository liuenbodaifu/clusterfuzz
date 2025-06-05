package com.google.clusterfuzz.core.performance;

import com.google.clusterfuzz.core.entity.Bot;
import com.google.clusterfuzz.core.entity.CrashStatistics;
import com.google.clusterfuzz.core.entity.Issue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Timeout;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Performance benchmark tests for entity operations.
 */
@DisplayName("Entity Performance Benchmarks")
class EntityPerformanceBenchmark {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Bot entity creation performance")
    @Timeout(value = 1, unit = TimeUnit.SECONDS)
    void botEntityCreationPerformance() {
        long startTime = System.nanoTime();
        
        // Create 1000 bot entities
        for (int i = 0; i < 1000; i++) {
            Bot bot = Bot.builder()
                    .name("performance-bot-" + i)
                    .platform("linux")
                    .version("1.0.0")
                    .status(Bot.BotStatus.IDLE)
                    .available(true)
                    .lastBeatTime(LocalDateTime.now())
                    .ipAddress("192.168.1." + (i % 255))
                    .cpuCores(8)
                    .memoryMb(16384)
                    .tasksCompleted((long) i)
                    .crashesFound((long) (i / 10))
                    .build();
            
            assertNotNull(bot);
            assertEquals("performance-bot-" + i, bot.getName());
        }
        
        long endTime = System.nanoTime();
        long durationMs = (endTime - startTime) / 1_000_000;
        
        System.out.println("Created 1000 Bot entities in " + durationMs + "ms");
        assertTrue(durationMs < 1000, "Bot creation should complete within 1 second");
    }

    @Test
    @DisplayName("Bot entity validation performance")
    @Timeout(value = 2, unit = TimeUnit.SECONDS)
    void botEntityValidationPerformance() {
        long startTime = System.nanoTime();
        
        // Validate 1000 bot entities
        for (int i = 0; i < 1000; i++) {
            Bot bot = Bot.builder()
                    .name("validation-bot-" + i)
                    .platform("linux")
                    .version("1.0.0")
                    .status(Bot.BotStatus.IDLE)
                    .build();
            
            Set<ConstraintViolation<Bot>> violations = validator.validate(bot);
            assertTrue(violations.isEmpty());
        }
        
        long endTime = System.nanoTime();
        long durationMs = (endTime - startTime) / 1_000_000;
        
        System.out.println("Validated 1000 Bot entities in " + durationMs + "ms");
        assertTrue(durationMs < 2000, "Bot validation should complete within 2 seconds");
    }

    @Test
    @DisplayName("Issue entity creation performance")
    @Timeout(value = 1, unit = TimeUnit.SECONDS)
    void issueEntityCreationPerformance() {
        long startTime = System.nanoTime();
        
        // Create 1000 issue entities
        for (int i = 0; i < 1000; i++) {
            Issue issue = Issue.builder()
                    .externalId("ISSUE-" + i)
                    .tracker(Issue.IssueTracker.GITHUB)
                    .project("test-project")
                    .title("Performance Test Issue " + i)
                    .description("This is a performance test issue number " + i)
                    .status(Issue.IssueStatus.NEW)
                    .priority(Issue.IssuePriority.MEDIUM)
                    .severity(Issue.IssueSeverity.MAJOR)
                    .reporter("test-user-" + (i % 10))
                    .isSecurity(i % 10 == 0) // Every 10th issue is security
                    .isPublic(i % 5 != 0) // Every 5th issue is private
                    .build();
            
            assertNotNull(issue);
            assertEquals("ISSUE-" + i, issue.getExternalId());
        }
        
        long endTime = System.nanoTime();
        long durationMs = (endTime - startTime) / 1_000_000;
        
        System.out.println("Created 1000 Issue entities in " + durationMs + "ms");
        assertTrue(durationMs < 1000, "Issue creation should complete within 1 second");
    }

    @Test
    @DisplayName("Issue entity validation performance")
    @Timeout(value = 2, unit = TimeUnit.SECONDS)
    void issueEntityValidationPerformance() {
        long startTime = System.nanoTime();
        
        // Validate 1000 issue entities
        for (int i = 0; i < 1000; i++) {
            Issue issue = Issue.builder()
                    .externalId("VALIDATION-ISSUE-" + i)
                    .tracker(Issue.IssueTracker.GITHUB)
                    .project("validation-project")
                    .title("Validation Test Issue " + i)
                    .status(Issue.IssueStatus.NEW)
                    .build();
            
            Set<ConstraintViolation<Issue>> violations = validator.validate(issue);
            assertTrue(violations.isEmpty());
        }
        
        long endTime = System.nanoTime();
        long durationMs = (endTime - startTime) / 1_000_000;
        
        System.out.println("Validated 1000 Issue entities in " + durationMs + "ms");
        assertTrue(durationMs < 2000, "Issue validation should complete within 2 seconds");
    }

    @Test
    @DisplayName("CrashStatistics entity creation performance")
    @Timeout(value = 1, unit = TimeUnit.SECONDS)
    void crashStatisticsEntityCreationPerformance() {
        long startTime = System.nanoTime();
        
        // Create 1000 crash statistics entities
        for (int i = 0; i < 1000; i++) {
            CrashStatistics stats = CrashStatistics.builder()
                    .date(LocalDate.now().minusDays(i % 365))
                    .jobName("performance-job-" + (i % 10))
                    .fuzzerName("libfuzzer")
                    .platform("linux")
                    .totalCrashes((long) (i * 10))
                    .uniqueCrashes((long) (i * 5))
                    .securityCrashes((long) i)
                    .totalExecutions((long) (i * 1000))
                    .totalFuzzingTimeSeconds((long) (i * 3600))
                    .avgExecutionsPerSecond(277.8 + i)
                    .corpusFilesGenerated((long) (i * 2))
                    .bugsFiled((long) (i / 10))
                    .build();
            
            assertNotNull(stats);
            assertEquals("performance-job-" + (i % 10), stats.getJobName());
        }
        
        long endTime = System.nanoTime();
        long durationMs = (endTime - startTime) / 1_000_000;
        
        System.out.println("Created 1000 CrashStatistics entities in " + durationMs + "ms");
        assertTrue(durationMs < 1000, "CrashStatistics creation should complete within 1 second");
    }

    @Test
    @DisplayName("CrashStatistics calculation performance")
    @Timeout(value = 1, unit = TimeUnit.SECONDS)
    void crashStatisticsCalculationPerformance() {
        long startTime = System.nanoTime();
        
        // Perform calculations on 1000 crash statistics entities
        for (int i = 1; i <= 1000; i++) {
            CrashStatistics stats = CrashStatistics.builder()
                    .date(LocalDate.now())
                    .jobName("calc-job")
                    .totalCrashes((long) (i * 100))
                    .uniqueCrashes((long) (i * 50))
                    .securityCrashes((long) (i * 10))
                    .totalExecutions((long) (i * 10000))
                    .bugsFiled((long) (i * 5))
                    .duplicateCrashes((long) (i * 20))
                    .cpuHours((double) i)
                    .heapBufferOverflowCrashes((long) (i * 15))
                    .stackBufferOverflowCrashes((long) (i * 10))
                    .useAfterFreeCrashes((long) (i * 20))
                    .nullPointerCrashes((long) (i * 5))
                    .build();
            
            // Perform all calculations
            double crashRate = stats.getCrashRate();
            double uniqueCrashRate = stats.getUniqueCrashRate();
            double securityPercentage = stats.getSecurityCrashPercentage();
            double bugFilingRate = stats.getBugFilingRate();
            double duplicateRate = stats.getDuplicateRate();
            double efficiencyScore = stats.getEfficiencyScore();
            String mostCommonType = stats.getMostCommonCrashType();
            
            // Verify calculations are reasonable
            assertTrue(crashRate >= 0);
            assertTrue(uniqueCrashRate >= 0);
            assertTrue(securityPercentage >= 0 && securityPercentage <= 100);
            assertTrue(bugFilingRate >= 0);
            assertTrue(duplicateRate >= 0);
            assertTrue(efficiencyScore >= 0);
            assertNotNull(mostCommonType);
        }
        
        long endTime = System.nanoTime();
        long durationMs = (endTime - startTime) / 1_000_000;
        
        System.out.println("Performed calculations on 1000 CrashStatistics entities in " + durationMs + "ms");
        assertTrue(durationMs < 1000, "CrashStatistics calculations should complete within 1 second");
    }

    @Test
    @DisplayName("Bot lifecycle operations performance")
    @Timeout(value = 2, unit = TimeUnit.SECONDS)
    void botLifecycleOperationsPerformance() {
        long startTime = System.nanoTime();
        
        // Perform lifecycle operations on 500 bots
        for (int i = 0; i < 500; i++) {
            Bot bot = Bot.builder()
                    .name("lifecycle-bot-" + i)
                    .platform("linux")
                    .status(Bot.BotStatus.IDLE)
                    .available(true)
                    .lastBeatTime(LocalDateTime.now().minusMinutes(2))
                    .tasksCompleted(0L)
                    .crashesFound(0L)
                    .build();
            
            // Simulate lifecycle operations
            assertTrue(bot.isAlive());
            assertTrue(bot.isAvailableForTasks());
            
            bot.updateHeartbeat();
            assertNotNull(bot.getLastBeatTime());
            
            bot.assignTask("test-task-" + i, "{\"type\":\"fuzz\"}", LocalDateTime.now().plusHours(1));
            assertEquals(Bot.BotStatus.BUSY, bot.getStatus());
            assertFalse(bot.getAvailable());
            
            bot.recordCrashFound();
            assertEquals(1L, bot.getCrashesFound());
            
            bot.completeTask();
            assertEquals(Bot.BotStatus.IDLE, bot.getStatus());
            assertTrue(bot.getAvailable());
            assertEquals(1L, bot.getTasksCompleted());
            
            bot.setError("Test error");
            assertEquals(Bot.BotStatus.ERROR, bot.getStatus());
            
            bot.clearError();
            assertEquals(Bot.BotStatus.IDLE, bot.getStatus());
        }
        
        long endTime = System.nanoTime();
        long durationMs = (endTime - startTime) / 1_000_000;
        
        System.out.println("Performed lifecycle operations on 500 bots in " + durationMs + "ms");
        assertTrue(durationMs < 2000, "Bot lifecycle operations should complete within 2 seconds");
    }

    @Test
    @DisplayName("Issue lifecycle operations performance")
    @Timeout(value = 2, unit = TimeUnit.SECONDS)
    void issueLifecycleOperationsPerformance() {
        long startTime = System.nanoTime();
        
        // Perform lifecycle operations on 500 issues
        for (int i = 0; i < 500; i++) {
            Issue issue = Issue.builder()
                    .externalId("LIFECYCLE-" + i)
                    .tracker(Issue.IssueTracker.GITHUB)
                    .project("lifecycle-project")
                    .title("Lifecycle Test Issue " + i)
                    .status(Issue.IssueStatus.NEW)
                    .build();
            
            // Simulate lifecycle operations
            assertTrue(issue.isOpen());
            assertFalse(issue.isClosed());
            
            issue.addLabel("bug");
            issue.addLabel("high-priority");
            assertEquals(2, issue.getLabels().size());
            
            issue.addComponent("ui");
            issue.addComponent("backend");
            assertEquals(2, issue.getComponents().size());
            
            issue.removeLabel("high-priority");
            assertEquals(1, issue.getLabels().size());
            
            issue.markFixed();
            assertEquals(Issue.IssueStatus.FIXED, issue.getStatus());
            assertTrue(issue.isClosed());
            assertNotNull(issue.getClosedAt());
            
            issue.reopen();
            assertEquals(Issue.IssueStatus.NEW, issue.getStatus());
            assertTrue(issue.isOpen());
            assertNull(issue.getClosedAt());
            
            issue.close(Issue.IssueStatus.INVALID, "Cannot reproduce");
            assertEquals(Issue.IssueStatus.INVALID, issue.getStatus());
            assertEquals("Cannot reproduce", issue.getCloseReason());
        }
        
        long endTime = System.nanoTime();
        long durationMs = (endTime - startTime) / 1_000_000;
        
        System.out.println("Performed lifecycle operations on 500 issues in " + durationMs + "ms");
        assertTrue(durationMs < 2000, "Issue lifecycle operations should complete within 2 seconds");
    }

    @Test
    @DisplayName("CrashStatistics aggregation performance")
    @Timeout(value = 1, unit = TimeUnit.SECONDS)
    void crashStatisticsAggregationPerformance() {
        long startTime = System.nanoTime();
        
        // Create base statistics
        CrashStatistics baseStats = CrashStatistics.builder()
                .date(LocalDate.now())
                .jobName("aggregation-job")
                .totalCrashes(100L)
                .uniqueCrashes(50L)
                .securityCrashes(10L)
                .totalExecutions(10000L)
                .totalFuzzingTimeSeconds(3600L)
                .corpusFilesGenerated(25L)
                .bugsFiled(5L)
                .build();
        
        // Aggregate 1000 statistics
        for (int i = 0; i < 1000; i++) {
            CrashStatistics additionalStats = CrashStatistics.builder()
                    .totalCrashes((long) i)
                    .uniqueCrashes((long) (i / 2))
                    .securityCrashes((long) (i / 10))
                    .totalExecutions((long) (i * 100))
                    .totalFuzzingTimeSeconds((long) (i * 36))
                    .corpusFilesGenerated((long) (i / 4))
                    .bugsFiled((long) (i / 20))
                    .build();
            
            baseStats.addStatistics(additionalStats);
        }
        
        long endTime = System.nanoTime();
        long durationMs = (endTime - startTime) / 1_000_000;
        
        System.out.println("Aggregated 1000 CrashStatistics in " + durationMs + "ms");
        assertTrue(durationMs < 1000, "CrashStatistics aggregation should complete within 1 second");
        
        // Verify final aggregated values are reasonable
        assertTrue(baseStats.getTotalCrashes() > 100L);
        assertTrue(baseStats.getUniqueCrashes() > 50L);
        assertTrue(baseStats.getTotalExecutions() > 10000L);
    }
}