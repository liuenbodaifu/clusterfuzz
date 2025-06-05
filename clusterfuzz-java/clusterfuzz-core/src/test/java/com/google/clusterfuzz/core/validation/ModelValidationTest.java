package com.google.clusterfuzz.core.validation;

import com.google.clusterfuzz.core.entity.*;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive validation tests for all entity models.
 * Tests Bean Validation annotations and business logic constraints.
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Model Validation Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ModelValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @Order(1)
    @DisplayName("Validation: Testcase Entity")
    void testTestcaseValidation() {
        // Test valid testcase
        Testcase validTestcase = new Testcase();
        validTestcase.setCrashType("Heap-buffer-overflow");
        validTestcase.setCrashState("READ 4");
        validTestcase.setJobType("libfuzzer_chrome_asan");
        validTestcase.setProjectName("chromium");
        validTestcase.setPlatform("linux");
        validTestcase.setStatus(1);
        validTestcase.setOpen(true);
        validTestcase.setReproduced(false);

        Set<ConstraintViolation<Testcase>> violations = validator.validate(validTestcase);
        assertTrue(violations.isEmpty(), "Valid testcase should have no violations");

        // Test business logic validation
        assertTrue(validTestcase.isActive());
        assertFalse(validTestcase.isClosed());
        assertEquals("Heap-buffer-overflow", validTestcase.getCrashType());
    }

    @Test
    @Order(2)
    @DisplayName("Validation: Job Entity")
    void testJobValidation() {
        // Test valid job
        Job validJob = new Job();
        validJob.setName("valid-job-name");
        validJob.setEnabled(true);
        validJob.setPlatform("linux");
        validJob.setDescription("Valid job description");

        Set<ConstraintViolation<Job>> violations = validator.validate(validJob);
        assertTrue(violations.isEmpty(), "Valid job should have no violations");

        // Test business logic
        assertTrue(validJob.isActive());
        assertEquals("linux", validJob.getPlatform());
    }

    @Test
    @Order(3)
    @DisplayName("Validation: BuildMetadata Entity")
    void testBuildMetadataValidation() {
        // Test valid build metadata
        BuildMetadata validBuild = new BuildMetadata();
        validBuild.setJobType("chrome_asan");
        validBuild.setRevision(12345);
        validBuild.setBadBuild(false);
        validBuild.setBotName("test-bot");
        validBuild.setTimestamp(LocalDateTime.now());

        Set<ConstraintViolation<BuildMetadata>> violations = validator.validate(validBuild);
        assertTrue(violations.isEmpty(), "Valid build metadata should have no violations");

        // Test business logic
        assertTrue(validBuild.isGoodBuild());
        assertFalse(validBuild.isBadBuild());
        assertEquals(Integer.valueOf(12345), validBuild.getRevision());
    }

    @Test
    @Order(4)
    @DisplayName("Validation: CoverageInformation Entity")
    void testCoverageInformationValidation() {
        // Test valid coverage information
        CoverageInformation validCoverage = new CoverageInformation();
        validCoverage.setFuzzer("libfuzzer");
        validCoverage.setDate(LocalDate.now());
        validCoverage.setFunctionsCovered(750);
        validCoverage.setFunctionsTotal(1000);
        validCoverage.setEdgesCovered(8500);
        validCoverage.setEdgesTotal(10000);
        validCoverage.setCorpusSizeUnits(100);
        validCoverage.setCorpusSizeBytes(1048576L);

        Set<ConstraintViolation<CoverageInformation>> violations = validator.validate(validCoverage);
        assertTrue(violations.isEmpty(), "Valid coverage information should have no violations");

        // Test calculated values
        assertEquals(75.0, validCoverage.getFunctionCoveragePercentage());
        assertEquals(85.0, validCoverage.getEdgeCoveragePercentage());
        assertTrue(validCoverage.hasCorpus());
    }

    @Test
    @Order(5)
    @DisplayName("Validation: FiledBug Entity")
    void testFiledBugValidation() {
        // Test valid filed bug
        FiledBug validBug = new FiledBug();
        validBug.setTestcaseId(12345L);
        validBug.setBugInformation(67890);
        validBug.setCrashType("Heap-buffer-overflow");
        validBug.setCrashState("READ 4");
        validBug.setSecurityFlag(true);
        validBug.setProjectName("chromium");

        Set<ConstraintViolation<FiledBug>> violations = validator.validate(validBug);
        assertTrue(violations.isEmpty(), "Valid filed bug should have no violations");

        // Test business logic
        assertTrue(validBug.isSecurityIssue());
        assertEquals(Long.valueOf(12345L), validBug.getTestcaseId());
        assertEquals(Integer.valueOf(67890), validBug.getBugInformation());
    }

    @Test
    @Order(6)
    @DisplayName("Validation: Cross-Entity Business Rules")
    void testCrossEntityBusinessRules() {
        // Test testcase and filed bug consistency
        Testcase testcase = new Testcase();
        testcase.setCrashType("Heap-buffer-overflow");
        testcase.setCrashState("READ 4");
        testcase.setJobType("chrome_asan");
        testcase.setProjectName("chromium");

        FiledBug bug = new FiledBug();
        bug.setCrashType(testcase.getCrashType());
        bug.setCrashState(testcase.getCrashState());
        bug.setProjectName(testcase.getProjectName());

        // Verify consistency
        assertEquals(testcase.getCrashType(), bug.getCrashType());
        assertEquals(testcase.getCrashState(), bug.getCrashState());
        assertEquals(testcase.getProjectName(), bug.getProjectName());

        // Test coverage information consistency
        CoverageInformation coverage = new CoverageInformation();
        coverage.setFunctionsCovered(750);
        coverage.setFunctionsTotal(1000);
        coverage.setEdgesCovered(8500);
        coverage.setEdgesTotal(10000);

        assertTrue(coverage.getFunctionsCovered() <= coverage.getFunctionsTotal());
        assertTrue(coverage.getEdgesCovered() <= coverage.getEdgesTotal());
        assertTrue(coverage.getFunctionCoveragePercentage() >= 0.0);
        assertTrue(coverage.getFunctionCoveragePercentage() <= 100.0);
    }

    @Test
    @Order(7)
    @DisplayName("Validation: Entity State Transitions")
    void testEntityStateTransitions() {
        // Test testcase state transitions
        Testcase testcase = new Testcase();
        testcase.setStatus(0); // New
        testcase.setOpen(true);
        testcase.setReproduced(false);

        // Transition to reproduced
        testcase.setReproduced(true);
        testcase.setStatus(1); // Processed
        assertTrue(testcase.isReproduced());
        assertEquals(1, testcase.getStatus());

        // Transition to closed
        testcase.setOpen(false);
        testcase.setStatus(2); // Fixed
        assertFalse(testcase.isOpen());
        assertTrue(testcase.isClosed());

        // Test notification state transitions
        Notification notification = new Notification();
        notification.setSent(false);
        notification.setSendAttempts(0);

        // Attempt to send
        notification.setSendAttempts(1);
        assertFalse(notification.isSent());
        assertEquals(1, notification.getSendAttempts());

        // Successfully sent
        notification.setSent(true);
        notification.setSentAt(LocalDateTime.now());
        assertTrue(notification.isSent());
        assertNotNull(notification.getSentAt());
    }
}