package com.google.clusterfuzz.core.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for FiledBug entity.
 */
@DisplayName("FiledBug Entity Tests")
class FiledBugTest {

    private FiledBug filedBug;

    @BeforeEach
    void setUp() {
        filedBug = new FiledBug();
    }

    @Test
    @DisplayName("Should create FiledBug with default values")
    void shouldCreateWithDefaults() {
        assertNotNull(filedBug);
        assertEquals(0, filedBug.getBugInformation());
    }

    @Test
    @DisplayName("Should create FiledBug with testcase ID and bug information")
    void shouldCreateWithTestcaseAndBugInfo() {
        FiledBug bug = new FiledBug(123L, 456);
        
        assertEquals(123L, bug.getTestcaseId());
        assertEquals(456, bug.getBugInformation());
    }

    @Test
    @DisplayName("Should create FiledBug with crash details")
    void shouldCreateWithCrashDetails() {
        FiledBug bug = new FiledBug(123L, 456, "Heap-buffer-overflow", "READ 4");
        
        assertEquals(123L, bug.getTestcaseId());
        assertEquals(456, bug.getBugInformation());
        assertEquals("Heap-buffer-overflow", bug.getCrashType());
        assertEquals("READ 4", bug.getCrashState());
    }

    @Test
    @DisplayName("Should handle security bug detection")
    void shouldHandleSecurityBugDetection() {
        assertFalse(filedBug.isSecurityBug());
        
        filedBug.setSecurityFlag(true);
        assertTrue(filedBug.isSecurityBug());
        
        filedBug.setSecurityFlag(false);
        assertFalse(filedBug.isSecurityBug());
        
        filedBug.setSecurityFlag(null);
        assertFalse(filedBug.isSecurityBug());
    }

    @Test
    @DisplayName("Should handle bug filing status")
    void shouldHandleBugFilingStatus() {
        assertFalse(filedBug.isFiled());
        
        filedBug.setBugInformation(0);
        assertFalse(filedBug.isFiled());
        
        filedBug.setBugInformation(123);
        assertTrue(filedBug.isFiled());
        
        filedBug.setBugInformation(null);
        assertFalse(filedBug.isFiled());
    }

    @Test
    @DisplayName("Should handle group membership")
    void shouldHandleGroupMembership() {
        assertFalse(filedBug.isGrouped());
        
        filedBug.setGroupId(456L);
        assertTrue(filedBug.isGrouped());
        
        filedBug.setGroupId(null);
        assertFalse(filedBug.isGrouped());
    }

    @Test
    @DisplayName("Should handle external tracking")
    void shouldHandleExternalTracking() {
        assertFalse(filedBug.hasExternalTracking());
        
        filedBug.setExternalIssueUrl("");
        assertFalse(filedBug.hasExternalTracking());
        
        filedBug.setExternalIssueUrl("   ");
        assertFalse(filedBug.hasExternalTracking());
        
        filedBug.setExternalIssueUrl("https://github.com/project/issues/123");
        assertTrue(filedBug.hasExternalTracking());
    }

    @Test
    @DisplayName("Should generate correct bug display ID")
    void shouldGenerateCorrectBugDisplayId() {
        assertEquals("Not Filed", filedBug.getBugDisplayId());
        
        filedBug.setBugInformation(0);
        assertEquals("Not Filed", filedBug.getBugDisplayId());
        
        filedBug.setBugInformation(123);
        assertEquals("123", filedBug.getBugDisplayId());
    }

    @Test
    @DisplayName("Should get security classification")
    void shouldGetSecurityClassification() {
        assertEquals("Functional", filedBug.getSecurityClassification());
        
        filedBug.setSecurityFlag(true);
        assertEquals("Security", filedBug.getSecurityClassification());
        
        filedBug.setSecurityFlag(false);
        assertEquals("Functional", filedBug.getSecurityClassification());
    }

    @Test
    @DisplayName("Should handle issue status")
    void shouldHandleIssueStatus() {
        assertTrue(filedBug.isOpen()); // null status is considered open
        assertFalse(filedBug.isClosed());
        
        filedBug.setIssueStatus("open");
        assertTrue(filedBug.isOpen());
        assertFalse(filedBug.isClosed());
        
        filedBug.setIssueStatus("closed");
        assertFalse(filedBug.isOpen());
        assertTrue(filedBug.isClosed());
        
        filedBug.setIssueStatus("resolved");
        assertFalse(filedBug.isOpen());
        assertTrue(filedBug.isClosed());
        
        filedBug.setIssueStatus("fixed");
        assertFalse(filedBug.isOpen());
        assertTrue(filedBug.isClosed());
        
        filedBug.setIssueStatus("in-progress");
        assertTrue(filedBug.isOpen());
        assertFalse(filedBug.isClosed());
    }

    @Test
    @DisplayName("Should generate bug summary")
    void shouldGenerateBugSummary() {
        filedBug.setBugInformation(123);
        assertEquals("Bug 123", filedBug.getBugSummary());
        
        filedBug.setCrashType("Heap-buffer-overflow");
        assertEquals("Bug 123 - Heap-buffer-overflow", filedBug.getBugSummary());
        
        filedBug.setSecurityFlag(true);
        assertEquals("Bug 123 - Heap-buffer-overflow [SECURITY]", filedBug.getBugSummary());
        
        filedBug.setProjectName("chromium");
        assertEquals("Bug 123 - Heap-buffer-overflow [SECURITY] (chromium)", filedBug.getBugSummary());
        
        // Test with unfiled bug
        filedBug.setBugInformation(0);
        assertEquals("Bug Not Filed - Heap-buffer-overflow [SECURITY] (chromium)", filedBug.getBugSummary());
    }

    @Test
    @DisplayName("Should update issue status")
    void shouldUpdateIssueStatus() {
        filedBug.updateIssueStatus("closed", "high");
        
        assertEquals("closed", filedBug.getIssueStatus());
        assertEquals("high", filedBug.getIssuePriority());
    }

    @Test
    @DisplayName("Should mark as security bug")
    void shouldMarkAsSecurityBug() {
        filedBug.markAsSecurityBug();
        assertTrue(filedBug.getSecurityFlag());
        assertTrue(filedBug.isSecurityBug());
    }

    @Test
    @DisplayName("Should mark as functional bug")
    void shouldMarkAsFunctionalBug() {
        filedBug.setSecurityFlag(true);
        filedBug.markAsFunctionalBug();
        assertFalse(filedBug.getSecurityFlag());
        assertFalse(filedBug.isSecurityBug());
    }

    @Test
    @DisplayName("Should handle all properties correctly")
    void shouldHandleAllProperties() {
        LocalDateTime now = LocalDateTime.now();
        
        filedBug.setTestcaseId(123L);
        filedBug.setBugInformation(456);
        filedBug.setGroupId(789L);
        filedBug.setCrashType("Use-after-free");
        filedBug.setCrashState("READ 8");
        filedBug.setSecurityFlag(true);
        filedBug.setPlatformId("linux");
        filedBug.setProjectName("chromium");
        filedBug.setJobType("libfuzzer_chrome_asan");
        filedBug.setIssueTrackerType("github");
        filedBug.setExternalIssueUrl("https://github.com/chromium/chromium/issues/123");
        filedBug.setIssueStatus("open");
        filedBug.setIssuePriority("high");
        filedBug.setTimestamp(now);
        
        assertEquals(123L, filedBug.getTestcaseId());
        assertEquals(456, filedBug.getBugInformation());
        assertEquals(789L, filedBug.getGroupId());
        assertEquals("Use-after-free", filedBug.getCrashType());
        assertEquals("READ 8", filedBug.getCrashState());
        assertTrue(filedBug.getSecurityFlag());
        assertEquals("linux", filedBug.getPlatformId());
        assertEquals("chromium", filedBug.getProjectName());
        assertEquals("libfuzzer_chrome_asan", filedBug.getJobType());
        assertEquals("github", filedBug.getIssueTrackerType());
        assertEquals("https://github.com/chromium/chromium/issues/123", filedBug.getExternalIssueUrl());
        assertEquals("open", filedBug.getIssueStatus());
        assertEquals("high", filedBug.getIssuePriority());
        assertEquals(now, filedBug.getTimestamp());
        
        assertTrue(filedBug.isFiled());
        assertTrue(filedBug.isGrouped());
        assertTrue(filedBug.hasExternalTracking());
        assertTrue(filedBug.isSecurityBug());
        assertTrue(filedBug.isOpen());
    }

    @Test
    @DisplayName("Should generate correct toString")
    void shouldGenerateCorrectToString() {
        filedBug.setId(1L);
        filedBug.setTestcaseId(123L);
        filedBug.setBugInformation(456);
        filedBug.setCrashType("Heap-buffer-overflow");
        filedBug.setSecurityFlag(true);
        filedBug.setProjectName("chromium");
        
        String toString = filedBug.toString();
        
        assertTrue(toString.contains("FiledBug{"));
        assertTrue(toString.contains("id=1"));
        assertTrue(toString.contains("testcaseId=123"));
        assertTrue(toString.contains("bugInformation=456"));
        assertTrue(toString.contains("crashType='Heap-buffer-overflow'"));
        assertTrue(toString.contains("securityFlag=true"));
        assertTrue(toString.contains("projectName='chromium'"));
    }

    @Test
    @DisplayName("Should handle equals and hashCode correctly")
    void shouldHandleEqualsAndHashCode() {
        FiledBug bug1 = new FiledBug();
        FiledBug bug2 = new FiledBug();
        
        // Without IDs, should not be equal
        assertNotEquals(bug1, bug2);
        
        // With same ID, should be equal
        bug1.setId(1L);
        bug2.setId(1L);
        assertEquals(bug1, bug2);
        assertEquals(bug1.hashCode(), bug2.hashCode());
        
        // With different IDs, should not be equal
        bug2.setId(2L);
        assertNotEquals(bug1, bug2);
        
        // Same object should be equal
        assertEquals(bug1, bug1);
        
        // Null comparison
        assertNotEquals(bug1, null);
        
        // Different class comparison
        assertNotEquals(bug1, "string");
    }

    @Test
    @DisplayName("Should handle edge cases for status detection")
    void shouldHandleEdgeCasesForStatusDetection() {
        // Test case-insensitive status detection
        filedBug.setIssueStatus("CLOSED");
        assertTrue(filedBug.isClosed());
        
        filedBug.setIssueStatus("Resolved");
        assertTrue(filedBug.isClosed());
        
        filedBug.setIssueStatus("FIXED");
        assertTrue(filedBug.isClosed());
        
        filedBug.setIssueStatus("Done");
        assertTrue(filedBug.isClosed());
        
        // Test partial matches
        filedBug.setIssueStatus("auto-closed");
        assertTrue(filedBug.isClosed());
        
        filedBug.setIssueStatus("marked-as-resolved");
        assertTrue(filedBug.isClosed());
    }

    @Test
    @DisplayName("Should handle constructor variations")
    void shouldHandleConstructorVariations() {
        // Test basic constructor
        FiledBug bug1 = new FiledBug(123L, 456);
        assertEquals(123L, bug1.getTestcaseId());
        assertEquals(456, bug1.getBugInformation());
        assertNull(bug1.getCrashType());
        assertNull(bug1.getCrashState());
        
        // Test extended constructor
        FiledBug bug2 = new FiledBug(789L, 101112, "Stack-overflow", "WRITE 4");
        assertEquals(789L, bug2.getTestcaseId());
        assertEquals(101112, bug2.getBugInformation());
        assertEquals("Stack-overflow", bug2.getCrashType());
        assertEquals("WRITE 4", bug2.getCrashState());
    }
}