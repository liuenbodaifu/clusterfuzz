package com.google.clusterfuzz.core.entity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the Testcase entity.
 */
class TestcaseTest {

    private Testcase testcase;

    @BeforeEach
    void setUp() {
        testcase = new Testcase();
    }

    @Test
    void testDefaultConstructor() {
        assertNotNull(testcase);
        assertNotNull(testcase.getTimestamp());
        assertEquals("Processed", testcase.getStatus());
        assertEquals(false, testcase.getSecurityFlag());
        assertEquals(true, testcase.getOpen());
        assertEquals(false, testcase.getIsLeader());
        assertEquals(128, testcase.getRedzone());
        assertEquals(1.0, testcase.getTimeoutMultiplier());
    }

    @Test
    void testParameterizedConstructor() {
        String crashType = "Heap-buffer-overflow";
        String crashState = "main\ntest_function\n";
        String fuzzerName = "libfuzzer";
        String jobType = "libfuzzer_asan_test";

        Testcase testcase = new Testcase(crashType, crashState, fuzzerName, jobType);

        assertEquals(crashType, testcase.getCrashType());
        assertEquals(crashState, testcase.getCrashState());
        assertEquals(fuzzerName, testcase.getFuzzerName());
        assertEquals(jobType, testcase.getJobType());
        assertNotNull(testcase.getTimestamp());
    }

    @Test
    void testSecurityBugMethods() {
        assertFalse(testcase.isSecurityBug());

        testcase.setSecurityFlag(true);
        assertTrue(testcase.isSecurityBug());

        testcase.setSecurityFlag(false);
        assertFalse(testcase.isSecurityBug());
    }

    @Test
    void testOpenMethods() {
        assertTrue(testcase.isOpen());

        testcase.setOpen(false);
        assertFalse(testcase.isOpen());

        testcase.setOpen(true);
        assertTrue(testcase.isOpen());
    }

    @Test
    void testLeaderMethods() {
        assertFalse(testcase.isLeader());

        testcase.setIsLeader(true);
        assertTrue(testcase.isLeader());

        testcase.setIsLeader(false);
        assertFalse(testcase.isLeader());
    }

    @Test
    void testBugMethods() {
        assertFalse(testcase.hasBug());

        testcase.setHasBugFlag(true);
        assertTrue(testcase.hasBug());

        testcase.setHasBugFlag(false);
        assertFalse(testcase.hasBug());
    }

    @Test
    void testDuplicateMethods() {
        assertFalse(testcase.isDuplicate());

        testcase.setIsADuplicateFlag(true);
        assertTrue(testcase.isDuplicate());

        testcase.setIsADuplicateFlag(false);
        assertFalse(testcase.isDuplicate());
    }

    @Test
    void testGesturesHandling() {
        assertNull(testcase.getGestures());

        testcase.setGestures(Arrays.asList("click", "scroll", "type"));
        assertEquals(3, testcase.getGestures().size());
        assertTrue(testcase.getGestures().contains("click"));
    }

    @Test
    void testKeywordsHandling() {
        assertNull(testcase.getKeywords());

        testcase.setKeywords(Arrays.asList("security", "heap", "overflow"));
        assertEquals(3, testcase.getKeywords().size());
        assertTrue(testcase.getKeywords().contains("security"));
    }

    @Test
    void testEqualsAndHashCode() {
        Testcase testcase1 = new Testcase();
        Testcase testcase2 = new Testcase();

        // Without IDs, they should not be equal
        assertNotEquals(testcase1, testcase2);

        // Set same ID
        testcase1.setId(1L);
        testcase2.setId(1L);
        assertEquals(testcase1, testcase2);
        assertEquals(testcase1.hashCode(), testcase2.hashCode());

        // Different IDs
        testcase2.setId(2L);
        assertNotEquals(testcase1, testcase2);
    }

    @Test
    void testToString() {
        testcase.setId(1L);
        testcase.setCrashType("Heap-buffer-overflow");
        testcase.setFuzzerName("libfuzzer");
        testcase.setJobType("test_job");
        testcase.setProjectName("test_project");
        testcase.setPlatform("linux");
        testcase.setSecurityFlag(true);

        String toString = testcase.toString();
        assertTrue(toString.contains("id=1"));
        assertTrue(toString.contains("crashType='Heap-buffer-overflow'"));
        assertTrue(toString.contains("fuzzerName='libfuzzer'"));
        assertTrue(toString.contains("securityFlag=true"));
    }

    @Test
    void testTimestampHandling() {
        LocalDateTime now = LocalDateTime.now();
        testcase.setTimestamp(now);
        assertEquals(now, testcase.getTimestamp());
    }

    @Test
    void testGroupHandling() {
        testcase.setGroupId(100L);
        testcase.setGroupBugInformation(200L);

        assertEquals(100L, testcase.getGroupId());
        assertEquals(200L, testcase.getGroupBugInformation());
    }

    @Test
    void testCrashInformation() {
        String crashType = "Use-after-free";
        String crashState = "main\nfree\nmalloc";
        String crashAddress = "0x7fff12345678";
        String stacktrace = "Stack trace line 1\nStack trace line 2";

        testcase.setCrashType(crashType);
        testcase.setCrashState(crashState);
        testcase.setCrashAddress(crashAddress);
        testcase.setCrashStacktrace(stacktrace);

        assertEquals(crashType, testcase.getCrashType());
        assertEquals(crashState, testcase.getCrashState());
        assertEquals(crashAddress, testcase.getCrashAddress());
        assertEquals(stacktrace, testcase.getCrashStacktrace());
    }

    @Test
    void testPlatformInformation() {
        testcase.setPlatform("linux");
        testcase.setPlatformId("linux:ubuntu:20.04");

        assertEquals("linux", testcase.getPlatform());
        assertEquals("linux:ubuntu:20.04", testcase.getPlatformId());
    }

    @Test
    void testSecuritySeverity() {
        testcase.setSecurityFlag(true);
        testcase.setSecuritySeverity(5);

        assertTrue(testcase.isSecurityBug());
        assertEquals(5, testcase.getSecuritySeverity());
    }
}