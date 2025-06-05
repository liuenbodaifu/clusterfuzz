package com.google.clusterfuzz.core.integration;

import com.google.clusterfuzz.core.entity.*;
import com.google.clusterfuzz.core.repository.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for repository layer and database interactions.
 * Tests the complete data access layer with real database operations.
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Repository Integration Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RepositoryIntegrationTest {

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

    @Autowired
    private FuzzerJobRepository fuzzerJobRepository;

    private Testcase savedTestcase;
    private Job savedJob;
    private BuildMetadata savedBuild;

    @BeforeEach
    void setUp() {
        // Create and save test data for integration tests
        setupTestData();
    }

    @Test
    @Order(1)
    @DisplayName("Integration: Testcase CRUD Operations")
    void testTestcaseCrudOperations() {
        // Test Create
        Testcase testcase = new Testcase();
        testcase.setCrashType("Heap-buffer-overflow");
        testcase.setCrashState("READ 4");
        testcase.setJobType("libfuzzer_chrome_asan");
        testcase.setProjectName("chromium");
        testcase.setPlatform("linux");
        testcase.setStatus(1);
        testcase.setOpen(true);
        testcase.setReproduced(true);

        Testcase saved = testcaseRepository.save(testcase);
        assertNotNull(saved.getId());
        assertEquals("Heap-buffer-overflow", saved.getCrashType());

        // Test Read
        Optional<Testcase> found = testcaseRepository.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals("READ 4", found.get().getCrashState());

        // Test Update
        found.get().setCrashState("WRITE 8");
        Testcase updated = testcaseRepository.save(found.get());
        assertEquals("WRITE 8", updated.getCrashState());

        // Test Delete
        testcaseRepository.delete(updated);
        Optional<Testcase> deleted = testcaseRepository.findById(saved.getId());
        assertFalse(deleted.isPresent());
    }

    @Test
    @Order(2)
    @DisplayName("Integration: Complex Query Operations")
    void testComplexQueryOperations() {
        // Test complex filtering
        List<Testcase> openTestcases = testcaseRepository.findByOpen(true);
        assertFalse(openTestcases.isEmpty());

        // Test aggregation queries
        List<Object[]> crashStats = testcaseRepository.getCrashTypeStatistics();
        assertNotNull(crashStats);

        // Test pagination
        Page<Testcase> page = testcaseRepository.findByJobType("libfuzzer_chrome_asan", PageRequest.of(0, 10));
        assertNotNull(page);

        // Test custom queries with parameters
        List<Testcase> recentTestcases = testcaseRepository.findRecentTestcases(LocalDateTime.now().minusDays(1));
        assertNotNull(recentTestcases);
    }

    @Test
    @Order(3)
    @DisplayName("Integration: Cross-Entity Relationships")
    void testCrossEntityRelationships() {
        // Test FiledBug creation linked to Testcase
        FiledBug bug = new FiledBug();
        bug.setTestcaseId(savedTestcase.getId());
        bug.setBugInformation(12345);
        bug.setCrashType(savedTestcase.getCrashType());
        bug.setCrashState(savedTestcase.getCrashState());
        bug.setSecurityFlag(true);
        bug.setProjectName(savedTestcase.getProjectName());

        FiledBug savedBug = filedBugRepository.save(bug);
        assertNotNull(savedBug.getId());

        // Test Notification creation linked to Testcase
        Notification notification = new Notification();
        notification.setTestcaseId(savedTestcase.getId());
        notification.setUserEmail("test@example.com");
        notification.setNotificationType("NEW_CRASH");
        notification.setSent(false);

        Notification savedNotification = notificationRepository.save(notification);
        assertNotNull(savedNotification.getId());

        // Verify relationships
        List<FiledBug> bugsForTestcase = filedBugRepository.findByTestcaseId(savedTestcase.getId());
        assertEquals(1, bugsForTestcase.size());

        List<Notification> notificationsForTestcase = notificationRepository.findByTestcaseId(savedTestcase.getId());
        assertEquals(1, notificationsForTestcase.size());
    }

    @Test
    @Order(4)
    @DisplayName("Integration: Bulk Operations")
    void testBulkOperations() {
        // Create multiple entities for bulk testing
        List<BuildMetadata> builds = List.of(
            createBuildMetadata("job1", 1001, false),
            createBuildMetadata("job1", 1002, true),
            createBuildMetadata("job1", 1003, false),
            createBuildMetadata("job2", 2001, false),
            createBuildMetadata("job2", 2002, true)
        );

        // Test bulk save
        List<BuildMetadata> savedBuilds = buildMetadataRepository.saveAll(builds);
        assertEquals(5, savedBuilds.size());

        // Test bulk queries
        List<BuildMetadata> job1Builds = buildMetadataRepository.findByJobType("job1");
        assertEquals(3, job1Builds.size());

        List<BuildMetadata> badBuilds = buildMetadataRepository.findBadBuildsByJobType("job1");
        assertEquals(1, badBuilds.size());

        // Test bulk statistics
        List<Object[]> buildStats = buildMetadataRepository.getBuildStatsByJobType();
        assertFalse(buildStats.isEmpty());
    }

    @Test
    @Order(5)
    @DisplayName("Integration: Transaction Rollback")
    @Transactional
    void testTransactionRollback() {
        // Save initial count
        long initialCount = testcaseRepository.count();

        try {
            // Create a testcase
            Testcase testcase = new Testcase();
            testcase.setCrashType("Test-crash");
            testcase.setJobType("test-job");
            testcaseRepository.save(testcase);

            // Verify it's saved
            assertEquals(initialCount + 1, testcaseRepository.count());

            // Force an exception to trigger rollback
            throw new RuntimeException("Simulated error");

        } catch (RuntimeException e) {
            // Exception expected
        }

        // After rollback, count should be back to initial
        entityManager.flush();
        entityManager.clear();
        assertEquals(initialCount, testcaseRepository.count());
    }

    @Test
    @Order(6)
    @DisplayName("Integration: Coverage Information Calculations")
    void testCoverageInformationCalculations() {
        // Create coverage data
        CoverageInformation coverage = new CoverageInformation();
        coverage.setFuzzer("libfuzzer");
        coverage.setDate(LocalDate.now());
        coverage.setFunctionsCovered(750);
        coverage.setFunctionsTotal(1000);
        coverage.setEdgesCovered(8500);
        coverage.setEdgesTotal(10000);
        coverage.setCorpusSizeUnits(100);
        coverage.setCorpusSizeBytes(1048576L);

        CoverageInformation saved = coverageRepository.save(coverage);

        // Test calculated values
        assertEquals(75.0, saved.getFunctionCoveragePercentage());
        assertEquals(85.0, saved.getEdgeCoveragePercentage());
        assertTrue(saved.hasCorpus());

        // Test aggregation queries
        List<Object[]> coverageStats = coverageRepository.getCoverageStatsByFuzzer();
        assertFalse(coverageStats.isEmpty());
    }

    @Test
    @Order(7)
    @DisplayName("Integration: Performance Under Load")
    void testPerformanceUnderLoad() {
        // Create a larger dataset for performance testing
        int testSize = 100;
        
        long startTime = System.currentTimeMillis();
        
        // Bulk create testcases
        for (int i = 0; i < testSize; i++) {
            Testcase testcase = new Testcase();
            testcase.setCrashType("Perf-test-" + i);
            testcase.setCrashState("STATE-" + i);
            testcase.setJobType("perf-job");
            testcase.setProjectName("perf-project");
            testcase.setPlatform("linux");
            testcase.setStatus(i % 3);
            testcase.setOpen(i % 2 == 0);
            testcaseRepository.save(testcase);
            
            if (i % 20 == 0) {
                entityManager.flush();
                entityManager.clear();
            }
        }
        
        entityManager.flush();
        long saveTime = System.currentTimeMillis() - startTime;
        
        // Test query performance
        startTime = System.currentTimeMillis();
        List<Testcase> results = testcaseRepository.findByJobType("perf-job");
        long queryTime = System.currentTimeMillis() - startTime;
        
        // Assertions
        assertEquals(testSize, results.size());
        assertTrue(saveTime < 5000, "Bulk save should complete within 5 seconds");
        assertTrue(queryTime < 1000, "Query should complete within 1 second");
        
        System.out.printf("Performance Test: Saved %d entities in %d ms, queried in %d ms%n", 
                         testSize, saveTime, queryTime);
    }

    // Helper methods
    private void setupTestData() {
        // Create and save a test job
        Job job = new Job();
        job.setName("test-integration-job");
        job.setEnabled(true);
        job.setPlatform("linux");
        job.setDescription("Integration test job");
        savedJob = jobRepository.save(job);

        // Create and save a test testcase
        Testcase testcase = new Testcase();
        testcase.setCrashType("Integration-test-crash");
        testcase.setCrashState("READ 4");
        testcase.setJobType(savedJob.getName());
        testcase.setProjectName("integration-project");
        testcase.setPlatform("linux");
        testcase.setStatus(1);
        testcase.setOpen(true);
        testcase.setReproduced(true);
        savedTestcase = testcaseRepository.save(testcase);

        // Create and save a test build
        BuildMetadata build = new BuildMetadata();
        build.setJobType(savedJob.getName());
        build.setRevision(12345);
        build.setBadBuild(false);
        build.setBotName("integration-bot");
        savedBuild = buildMetadataRepository.save(build);

        entityManager.flush();
    }

    private BuildMetadata createBuildMetadata(String jobType, Integer revision, Boolean badBuild) {
        BuildMetadata build = new BuildMetadata();
        build.setJobType(jobType);
        build.setRevision(revision);
        build.setBadBuild(badBuild);
        build.setBotName("test-bot");
        build.setTimestamp(LocalDateTime.now());
        return build;
    }
}