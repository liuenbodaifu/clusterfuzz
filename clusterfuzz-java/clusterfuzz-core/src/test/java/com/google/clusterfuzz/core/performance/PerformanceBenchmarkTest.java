package com.google.clusterfuzz.core.performance;

import com.google.clusterfuzz.core.entity.*;
import com.google.clusterfuzz.core.repository.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Performance benchmark tests for ClusterFuzz entities and repositories.
 * Establishes baseline performance metrics and validates scalability.
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Performance Benchmark Tests")
class PerformanceBenchmarkTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TestcaseRepository testcaseRepository;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private BuildMetadataRepository buildMetadataRepository;

    @Autowired
    private CoverageInformationRepository coverageRepository;

    @Autowired
    private FiledBugRepository filedBugRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    private static final int SMALL_DATASET = 100;
    private static final int MEDIUM_DATASET = 1000;
    private static final int LARGE_DATASET = 10000;

    @BeforeEach
    void setUp() {
        // Clear any existing data
        entityManager.clear();
    }

    @Test
    @DisplayName("Benchmark: Entity Creation Performance")
    void benchmarkEntityCreation() {
        System.out.println("\n=== Entity Creation Performance Benchmark ===");

        // Benchmark Testcase creation
        long startTime = System.nanoTime();
        List<Testcase> testcases = createTestcases(MEDIUM_DATASET);
        long creationTime = System.nanoTime() - startTime;
        
        System.out.printf("Created %d testcases in %d ms (%.2f μs per entity)%n", 
                         MEDIUM_DATASET, 
                         TimeUnit.NANOSECONDS.toMillis(creationTime),
                         TimeUnit.NANOSECONDS.toMicros(creationTime) / (double) MEDIUM_DATASET);

        // Benchmark Job creation
        startTime = System.nanoTime();
        List<Job> jobs = createJobs(SMALL_DATASET);
        creationTime = System.nanoTime() - startTime;
        
        System.out.printf("Created %d jobs in %d ms (%.2f μs per entity)%n", 
                         SMALL_DATASET, 
                         TimeUnit.NANOSECONDS.toMillis(creationTime),
                         TimeUnit.NANOSECONDS.toMicros(creationTime) / (double) SMALL_DATASET);

        // Performance assertions
        Assertions.assertTrue(TimeUnit.NANOSECONDS.toMicros(creationTime) / SMALL_DATASET < 100, 
                            "Entity creation should be under 100 microseconds per entity");
    }

    @Test
    @DisplayName("Benchmark: Database Operations")
    void benchmarkDatabaseOperations() {
        System.out.println("\n=== Database Operations Performance Benchmark ===");

        List<Testcase> testcases = createTestcases(MEDIUM_DATASET);

        // Benchmark batch save
        long startTime = System.nanoTime();
        testcaseRepository.saveAll(testcases);
        entityManager.flush();
        long saveTime = System.nanoTime() - startTime;
        
        System.out.printf("Batch saved %d testcases in %d ms (%.2f ms per 100 entities)%n", 
                         MEDIUM_DATASET, 
                         TimeUnit.NANOSECONDS.toMillis(saveTime),
                         TimeUnit.NANOSECONDS.toMillis(saveTime) * 100.0 / MEDIUM_DATASET);

        // Benchmark individual queries
        startTime = System.nanoTime();
        List<Testcase> openTestcases = testcaseRepository.findByOpen(true);
        long queryTime = System.nanoTime() - startTime;
        
        System.out.printf("Queried %d open testcases in %d ms%n", 
                         openTestcases.size(), TimeUnit.NANOSECONDS.toMillis(queryTime));

        // Performance assertions
        Assertions.assertTrue(TimeUnit.NANOSECONDS.toMillis(saveTime) < 5000, 
                            "Batch save should be under 5 seconds");
        Assertions.assertTrue(TimeUnit.NANOSECONDS.toMillis(queryTime) < 500, 
                            "Simple queries should be under 500ms");
    }

    @Test
    @DisplayName("Benchmark: Complex Query Performance")
    void benchmarkComplexQueries() {
        System.out.println("\n=== Complex Query Performance Benchmark ===");

        // Setup test data
        List<Testcase> testcases = createTestcases(MEDIUM_DATASET);
        testcaseRepository.saveAll(testcases);
        entityManager.flush();

        // Benchmark aggregation queries
        long startTime = System.nanoTime();
        List<Object[]> crashStats = testcaseRepository.getCrashTypeStatistics();
        long queryTime = System.nanoTime() - startTime;
        
        System.out.printf("Generated crash statistics (%d types) in %d ms%n", 
                         crashStats.size(), TimeUnit.NANOSECONDS.toMillis(queryTime));

        // Benchmark filtering queries
        startTime = System.nanoTime();
        List<Testcase> filteredTestcases = testcaseRepository.findByJobTypeAndPlatform("libfuzzer_chrome_asan", "linux");
        queryTime = System.nanoTime() - startTime;
        
        System.out.printf("Filtered testcases (%d results) in %d ms%n", 
                         filteredTestcases.size(), TimeUnit.NANOSECONDS.toMillis(queryTime));

        // Performance assertions
        Assertions.assertTrue(TimeUnit.NANOSECONDS.toMillis(queryTime) < 1000, 
                            "Complex queries should be under 1 second");
    }

    @Test
    @DisplayName("Benchmark: Memory Usage")
    void benchmarkMemoryUsage() {
        System.out.println("\n=== Memory Usage Performance Benchmark ===");

        Runtime runtime = Runtime.getRuntime();
        
        // Measure initial memory
        runtime.gc();
        long initialMemory = runtime.totalMemory() - runtime.freeMemory();
        
        // Create and save entities
        List<Testcase> testcases = createTestcases(LARGE_DATASET);
        
        // Measure memory after creation
        long afterCreationMemory = runtime.totalMemory() - runtime.freeMemory();
        long creationMemoryUsage = afterCreationMemory - initialMemory;
        
        System.out.printf("Memory usage for %d testcases: %.2f MB (%.2f KB per entity)%n", 
                         LARGE_DATASET,
                         creationMemoryUsage / (1024.0 * 1024.0),
                         creationMemoryUsage / (1024.0 * LARGE_DATASET));

        // Save in batches to test memory efficiency
        int batchSize = 500;
        long startTime = System.nanoTime();
        
        for (int i = 0; i < testcases.size(); i += batchSize) {
            int endIndex = Math.min(i + batchSize, testcases.size());
            testcaseRepository.saveAll(testcases.subList(i, endIndex));
            entityManager.flush();
            entityManager.clear(); // Clear persistence context
        }
        
        long batchSaveTime = System.nanoTime() - startTime;
        
        System.out.printf("Batch saved %d testcases in %d batches in %d ms%n", 
                         LARGE_DATASET, 
                         (LARGE_DATASET + batchSize - 1) / batchSize,
                         TimeUnit.NANOSECONDS.toMillis(batchSaveTime));

        // Memory assertions
        double memoryPerEntity = creationMemoryUsage / (double) LARGE_DATASET;
        Assertions.assertTrue(memoryPerEntity < 1024, // Less than 1KB per entity
                            "Memory usage per entity should be reasonable");
    }

    // Helper methods for creating test data
    private List<Testcase> createTestcases(int count) {
        List<Testcase> testcases = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Testcase testcase = new Testcase();
            testcase.setCrashType("Heap-buffer-overflow-" + (i % 10));
            testcase.setCrashState("READ " + (i % 16));
            testcase.setJobType("libfuzzer_chrome_asan");
            testcase.setProjectName("chromium");
            testcase.setPlatform("linux");
            testcase.setStatus(i % 3);
            testcase.setOpen(i % 4 != 0);
            testcase.setReproduced(i % 5 != 0);
            testcase.setTimestamp(LocalDateTime.now().minusDays(i % 30));
            testcases.add(testcase);
        }
        return testcases;
    }

    private List<Job> createJobs(int count) {
        List<Job> jobs = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Job job = new Job();
            job.setName("test-job-" + i);
            job.setEnabled(i % 10 != 0);
            job.setPlatform("linux");
            job.setDescription("Test job " + i);
            jobs.add(job);
        }
        return jobs;
    }
}