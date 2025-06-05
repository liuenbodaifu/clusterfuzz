package com.google.clusterfuzz.core.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for CoverageInformation entity.
 */
@DisplayName("CoverageInformation Entity Tests")
class CoverageInformationTest {

    private CoverageInformation coverageInfo;

    @BeforeEach
    void setUp() {
        coverageInfo = new CoverageInformation();
    }

    @Test
    @DisplayName("Should create CoverageInformation with default values")
    void shouldCreateWithDefaults() {
        assertNotNull(coverageInfo);
        assertNull(coverageInfo.getFuzzer());
        assertNull(coverageInfo.getDate());
    }

    @Test
    @DisplayName("Should create CoverageInformation with fuzzer")
    void shouldCreateWithFuzzer() {
        CoverageInformation info = new CoverageInformation("libfuzzer");
        
        assertEquals("libfuzzer", info.getFuzzer());
        assertEquals(LocalDate.now(), info.getDate());
    }

    @Test
    @DisplayName("Should calculate function coverage percentage correctly")
    void shouldCalculateFunctionCoveragePercentage() {
        // Test with null values
        assertEquals(0.0, coverageInfo.getFunctionCoveragePercentage());
        
        // Test with zero total
        coverageInfo.setFunctionsTotal(0);
        coverageInfo.setFunctionsCovered(10);
        assertEquals(0.0, coverageInfo.getFunctionCoveragePercentage());
        
        // Test with normal values
        coverageInfo.setFunctionsTotal(100);
        coverageInfo.setFunctionsCovered(75);
        assertEquals(75.0, coverageInfo.getFunctionCoveragePercentage());
        
        // Test with null covered
        coverageInfo.setFunctionsCovered(null);
        assertEquals(0.0, coverageInfo.getFunctionCoveragePercentage());
        
        // Test with partial coverage
        coverageInfo.setFunctionsCovered(33);
        assertEquals(33.0, coverageInfo.getFunctionCoveragePercentage());
    }

    @Test
    @DisplayName("Should calculate edge coverage percentage correctly")
    void shouldCalculateEdgeCoveragePercentage() {
        // Test with null values
        assertEquals(0.0, coverageInfo.getEdgeCoveragePercentage());
        
        // Test with zero total
        coverageInfo.setEdgesTotal(0);
        coverageInfo.setEdgesCovered(10);
        assertEquals(0.0, coverageInfo.getEdgeCoveragePercentage());
        
        // Test with normal values
        coverageInfo.setEdgesTotal(1000);
        coverageInfo.setEdgesCovered(850);
        assertEquals(85.0, coverageInfo.getEdgeCoveragePercentage());
        
        // Test with null covered
        coverageInfo.setEdgesCovered(null);
        assertEquals(0.0, coverageInfo.getEdgeCoveragePercentage());
    }

    @Test
    @DisplayName("Should detect corpus presence")
    void shouldDetectCorpusPresence() {
        assertFalse(coverageInfo.hasCorpus());
        
        coverageInfo.setCorpusSizeUnits(0);
        assertFalse(coverageInfo.hasCorpus());
        
        coverageInfo.setCorpusSizeUnits(100);
        assertTrue(coverageInfo.hasCorpus());
        
        coverageInfo.setCorpusSizeUnits(null);
        assertFalse(coverageInfo.hasCorpus());
    }

    @Test
    @DisplayName("Should detect quarantine presence")
    void shouldDetectQuarantinePresence() {
        assertFalse(coverageInfo.hasQuarantine());
        
        coverageInfo.setQuarantineSizeUnits(0);
        assertFalse(coverageInfo.hasQuarantine());
        
        coverageInfo.setQuarantineSizeUnits(50);
        assertTrue(coverageInfo.hasQuarantine());
        
        coverageInfo.setQuarantineSizeUnits(null);
        assertFalse(coverageInfo.hasQuarantine());
    }

    @Test
    @DisplayName("Should detect HTML report presence")
    void shouldDetectHtmlReportPresence() {
        assertFalse(coverageInfo.hasHtmlReport());
        
        coverageInfo.setHtmlReportUrl("");
        assertFalse(coverageInfo.hasHtmlReport());
        
        coverageInfo.setHtmlReportUrl("   ");
        assertFalse(coverageInfo.hasHtmlReport());
        
        coverageInfo.setHtmlReportUrl("http://example.com/report.html");
        assertTrue(coverageInfo.hasHtmlReport());
    }

    @Test
    @DisplayName("Should format corpus size correctly")
    void shouldFormatCorpusSizeCorrectly() {
        assertEquals("0 bytes", coverageInfo.getCorpusSizeFormatted());
        
        coverageInfo.setCorpusSizeBytes(512);
        assertEquals("512 bytes", coverageInfo.getCorpusSizeFormatted());
        
        coverageInfo.setCorpusSizeBytes(1536); // 1.5 KB
        assertEquals("1.5 KB", coverageInfo.getCorpusSizeFormatted());
        
        coverageInfo.setCorpusSizeBytes(2097152); // 2 MB
        assertEquals("2.0 MB", coverageInfo.getCorpusSizeFormatted());
        
        coverageInfo.setCorpusSizeBytes(1073741824L); // 1 GB
        assertEquals("1.0 GB", coverageInfo.getCorpusSizeFormatted());
    }

    @Test
    @DisplayName("Should format quarantine size correctly")
    void shouldFormatQuarantineSizeCorrectly() {
        assertEquals("0 bytes", coverageInfo.getQuarantineSizeFormatted());
        
        coverageInfo.setQuarantineSizeBytes(1024);
        assertEquals("1.0 KB", coverageInfo.getQuarantineSizeFormatted());
        
        coverageInfo.setQuarantineSizeBytes(1048576); // 1 MB
        assertEquals("1.0 MB", coverageInfo.getQuarantineSizeFormatted());
    }

    @Test
    @DisplayName("Should handle all properties correctly")
    void shouldHandleAllProperties() {
        LocalDate testDate = LocalDate.of(2023, 12, 25);
        
        coverageInfo.setFuzzer("afl");
        coverageInfo.setDate(testDate);
        coverageInfo.setFunctionsCovered(500);
        coverageInfo.setFunctionsTotal(1000);
        coverageInfo.setEdgesCovered(7500);
        coverageInfo.setEdgesTotal(10000);
        coverageInfo.setCorpusSizeUnits(250);
        coverageInfo.setCorpusSizeBytes(1048576);
        coverageInfo.setCorpusLocation("gs://bucket/corpus");
        coverageInfo.setCorpusBackupLocation("gs://bucket/backup");
        coverageInfo.setQuarantineSizeUnits(10);
        coverageInfo.setQuarantineSizeBytes(10240);
        coverageInfo.setQuarantineLocation("gs://bucket/quarantine");
        coverageInfo.setHtmlReportUrl("http://example.com/report.html");
        
        assertEquals("afl", coverageInfo.getFuzzer());
        assertEquals(testDate, coverageInfo.getDate());
        assertEquals(500, coverageInfo.getFunctionsCovered());
        assertEquals(1000, coverageInfo.getFunctionsTotal());
        assertEquals(7500, coverageInfo.getEdgesCovered());
        assertEquals(10000, coverageInfo.getEdgesTotal());
        assertEquals(250, coverageInfo.getCorpusSizeUnits());
        assertEquals(1048576, coverageInfo.getCorpusSizeBytes());
        assertEquals("gs://bucket/corpus", coverageInfo.getCorpusLocation());
        assertEquals("gs://bucket/backup", coverageInfo.getCorpusBackupLocation());
        assertEquals(10, coverageInfo.getQuarantineSizeUnits());
        assertEquals(10240, coverageInfo.getQuarantineSizeBytes());
        assertEquals("gs://bucket/quarantine", coverageInfo.getQuarantineLocation());
        assertEquals("http://example.com/report.html", coverageInfo.getHtmlReportUrl());
        
        // Test calculated values
        assertEquals(50.0, coverageInfo.getFunctionCoveragePercentage());
        assertEquals(75.0, coverageInfo.getEdgeCoveragePercentage());
        assertTrue(coverageInfo.hasCorpus());
        assertTrue(coverageInfo.hasQuarantine());
        assertTrue(coverageInfo.hasHtmlReport());
    }

    @Test
    @DisplayName("Should generate correct toString")
    void shouldGenerateCorrectToString() {
        coverageInfo.setId(1L);
        coverageInfo.setFuzzer("libfuzzer");
        coverageInfo.setDate(LocalDate.of(2023, 12, 25));
        coverageInfo.setFunctionsCovered(100);
        coverageInfo.setFunctionsTotal(200);
        coverageInfo.setEdgesCovered(500);
        coverageInfo.setEdgesTotal(1000);
        coverageInfo.setCorpusSizeUnits(50);
        
        String toString = coverageInfo.toString();
        
        assertTrue(toString.contains("CoverageInformation{"));
        assertTrue(toString.contains("id=1"));
        assertTrue(toString.contains("fuzzer='libfuzzer'"));
        assertTrue(toString.contains("functionsCovered=100"));
        assertTrue(toString.contains("functionsTotal=200"));
        assertTrue(toString.contains("edgesCovered=500"));
        assertTrue(toString.contains("edgesTotal=1000"));
        assertTrue(toString.contains("corpusSizeUnits=50"));
    }

    @Test
    @DisplayName("Should handle equals and hashCode correctly")
    void shouldHandleEqualsAndHashCode() {
        CoverageInformation info1 = new CoverageInformation();
        CoverageInformation info2 = new CoverageInformation();
        
        // Without IDs, should not be equal
        assertNotEquals(info1, info2);
        
        // With same ID, should be equal
        info1.setId(1L);
        info2.setId(1L);
        assertEquals(info1, info2);
        assertEquals(info1.hashCode(), info2.hashCode());
        
        // With different IDs, should not be equal
        info2.setId(2L);
        assertNotEquals(info1, info2);
        
        // Same object should be equal
        assertEquals(info1, info1);
        
        // Null comparison
        assertNotEquals(info1, null);
        
        // Different class comparison
        assertNotEquals(info1, "string");
    }

    @Test
    @DisplayName("Should handle edge cases for percentage calculations")
    void shouldHandleEdgeCasesForPercentageCalculations() {
        // Test with very large numbers
        coverageInfo.setFunctionsTotal(Integer.MAX_VALUE);
        coverageInfo.setFunctionsCovered(Integer.MAX_VALUE / 2);
        assertTrue(coverageInfo.getFunctionCoveragePercentage() > 49.0);
        assertTrue(coverageInfo.getFunctionCoveragePercentage() < 51.0);
        
        // Test with very small numbers
        coverageInfo.setFunctionsTotal(1);
        coverageInfo.setFunctionsCovered(1);
        assertEquals(100.0, coverageInfo.getFunctionCoveragePercentage());
        
        // Test with zero covered
        coverageInfo.setFunctionsCovered(0);
        assertEquals(0.0, coverageInfo.getFunctionCoveragePercentage());
    }

    @Test
    @DisplayName("Should handle constructor with fuzzer parameter")
    void shouldHandleConstructorWithFuzzer() {
        CoverageInformation info = new CoverageInformation("honggfuzz");
        
        assertEquals("honggfuzz", info.getFuzzer());
        assertEquals(LocalDate.now(), info.getDate());
        assertNull(info.getId());
        assertNull(info.getFunctionsCovered());
        assertNull(info.getFunctionsTotal());
    }

    @Test
    @DisplayName("Should validate byte formatting edge cases")
    void shouldValidateByteFormattingEdgeCases() {
        // Test exact boundaries
        coverageInfo.setCorpusSizeBytes(1023);
        assertEquals("1023 bytes", coverageInfo.getCorpusSizeFormatted());
        
        coverageInfo.setCorpusSizeBytes(1024);
        assertEquals("1.0 KB", coverageInfo.getCorpusSizeFormatted());
        
        coverageInfo.setCorpusSizeBytes(1048575);
        assertEquals("1024.0 KB", coverageInfo.getCorpusSizeFormatted());
        
        coverageInfo.setCorpusSizeBytes(1048576);
        assertEquals("1.0 MB", coverageInfo.getCorpusSizeFormatted());
    }
}