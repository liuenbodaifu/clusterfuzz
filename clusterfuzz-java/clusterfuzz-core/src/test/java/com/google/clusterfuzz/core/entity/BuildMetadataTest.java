package com.google.clusterfuzz.core.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for BuildMetadata entity.
 */
@DisplayName("BuildMetadata Entity Tests")
class BuildMetadataTest {

    private BuildMetadata buildMetadata;

    @BeforeEach
    void setUp() {
        buildMetadata = new BuildMetadata();
    }

    @Test
    @DisplayName("Should create BuildMetadata with default values")
    void shouldCreateWithDefaults() {
        assertNotNull(buildMetadata);
        assertFalse(buildMetadata.getBadBuild());
        assertTrue(buildMetadata.isGoodBuild());
    }

    @Test
    @DisplayName("Should create BuildMetadata with job type and revision")
    void shouldCreateWithJobTypeAndRevision() {
        BuildMetadata metadata = new BuildMetadata("libfuzzer_chrome_asan", 12345);
        
        assertEquals("libfuzzer_chrome_asan", metadata.getJobType());
        assertEquals(12345, metadata.getRevision());
        assertFalse(metadata.getBadBuild());
    }

    @Test
    @DisplayName("Should handle bad build marking")
    void shouldHandleBadBuildMarking() {
        buildMetadata.markAsBadBuild();
        
        assertTrue(buildMetadata.getBadBuild());
        assertFalse(buildMetadata.isGoodBuild());
    }

    @Test
    @DisplayName("Should handle good build marking")
    void shouldHandleGoodBuildMarking() {
        buildMetadata.setBadBuild(true);
        buildMetadata.markAsGoodBuild();
        
        assertFalse(buildMetadata.getBadBuild());
        assertTrue(buildMetadata.isGoodBuild());
    }

    @Test
    @DisplayName("Should detect console output presence")
    void shouldDetectConsoleOutputPresence() {
        assertFalse(buildMetadata.hasConsoleOutput());
        
        buildMetadata.setConsoleOutput("Build output here");
        assertTrue(buildMetadata.hasConsoleOutput());
        
        buildMetadata.setConsoleOutput("   ");
        assertFalse(buildMetadata.hasConsoleOutput());
        
        buildMetadata.setConsoleOutput("");
        assertFalse(buildMetadata.hasConsoleOutput());
    }

    @Test
    @DisplayName("Should detect symbols presence")
    void shouldDetectSymbolsPresence() {
        assertFalse(buildMetadata.hasSymbols());
        
        buildMetadata.setSymbols("symbol_data.zip");
        assertTrue(buildMetadata.hasSymbols());
        
        buildMetadata.setSymbols("   ");
        assertFalse(buildMetadata.hasSymbols());
        
        buildMetadata.setSymbols("");
        assertFalse(buildMetadata.hasSymbols());
    }

    @Test
    @DisplayName("Should handle all properties correctly")
    void shouldHandleAllProperties() {
        LocalDateTime now = LocalDateTime.now();
        
        buildMetadata.setJobType("test_job");
        buildMetadata.setRevision(54321);
        buildMetadata.setBadBuild(true);
        buildMetadata.setConsoleOutput("Console output");
        buildMetadata.setBotName("test-bot-1");
        buildMetadata.setSymbols("symbols.zip");
        buildMetadata.setTimestamp(now);
        
        assertEquals("test_job", buildMetadata.getJobType());
        assertEquals(54321, buildMetadata.getRevision());
        assertTrue(buildMetadata.getBadBuild());
        assertEquals("Console output", buildMetadata.getConsoleOutput());
        assertEquals("test-bot-1", buildMetadata.getBotName());
        assertEquals("symbols.zip", buildMetadata.getSymbols());
        assertEquals(now, buildMetadata.getTimestamp());
    }

    @Test
    @DisplayName("Should handle null values gracefully")
    void shouldHandleNullValues() {
        buildMetadata.setJobType(null);
        buildMetadata.setRevision(null);
        buildMetadata.setBadBuild(null);
        buildMetadata.setConsoleOutput(null);
        buildMetadata.setBotName(null);
        buildMetadata.setSymbols(null);
        
        assertNull(buildMetadata.getJobType());
        assertNull(buildMetadata.getRevision());
        assertNull(buildMetadata.getBadBuild());
        assertNull(buildMetadata.getConsoleOutput());
        assertNull(buildMetadata.getBotName());
        assertNull(buildMetadata.getSymbols());
        
        assertFalse(buildMetadata.hasConsoleOutput());
        assertFalse(buildMetadata.hasSymbols());
    }

    @Test
    @DisplayName("Should generate correct toString")
    void shouldGenerateCorrectToString() {
        buildMetadata.setId(1L);
        buildMetadata.setJobType("test_job");
        buildMetadata.setRevision(12345);
        buildMetadata.setBadBuild(false);
        buildMetadata.setBotName("test-bot");
        
        String toString = buildMetadata.toString();
        
        assertTrue(toString.contains("BuildMetadata{"));
        assertTrue(toString.contains("id=1"));
        assertTrue(toString.contains("jobType='test_job'"));
        assertTrue(toString.contains("revision=12345"));
        assertTrue(toString.contains("badBuild=false"));
        assertTrue(toString.contains("botName='test-bot'"));
    }

    @Test
    @DisplayName("Should handle equals and hashCode correctly")
    void shouldHandleEqualsAndHashCode() {
        BuildMetadata metadata1 = new BuildMetadata();
        BuildMetadata metadata2 = new BuildMetadata();
        
        // Without IDs, should not be equal
        assertNotEquals(metadata1, metadata2);
        
        // With same ID, should be equal
        metadata1.setId(1L);
        metadata2.setId(1L);
        assertEquals(metadata1, metadata2);
        assertEquals(metadata1.hashCode(), metadata2.hashCode());
        
        // With different IDs, should not be equal
        metadata2.setId(2L);
        assertNotEquals(metadata1, metadata2);
        
        // Same object should be equal
        assertEquals(metadata1, metadata1);
        
        // Null comparison
        assertNotEquals(metadata1, null);
        
        // Different class comparison
        assertNotEquals(metadata1, "string");
    }

    @Test
    @DisplayName("Should handle edge cases for boolean operations")
    void shouldHandleEdgeCasesForBooleanOperations() {
        // Test with null badBuild
        buildMetadata.setBadBuild(null);
        assertTrue(buildMetadata.isGoodBuild()); // null is treated as good build
        
        // Test explicit false
        buildMetadata.setBadBuild(false);
        assertTrue(buildMetadata.isGoodBuild());
        
        // Test explicit true
        buildMetadata.setBadBuild(true);
        assertFalse(buildMetadata.isGoodBuild());
    }

    @Test
    @DisplayName("Should handle constructor with parameters")
    void shouldHandleConstructorWithParameters() {
        BuildMetadata metadata = new BuildMetadata("chrome_asan", 98765);
        
        assertEquals("chrome_asan", metadata.getJobType());
        assertEquals(98765, metadata.getRevision());
        assertFalse(metadata.getBadBuild());
        assertNull(metadata.getId());
        assertNull(metadata.getBotName());
        assertNull(metadata.getConsoleOutput());
        assertNull(metadata.getSymbols());
    }

    @Test
    @DisplayName("Should validate business logic methods")
    void shouldValidateBusinessLogicMethods() {
        // Test marking as bad build
        buildMetadata.markAsBadBuild();
        assertTrue(buildMetadata.getBadBuild());
        assertFalse(buildMetadata.isGoodBuild());
        
        // Test marking as good build
        buildMetadata.markAsGoodBuild();
        assertFalse(buildMetadata.getBadBuild());
        assertTrue(buildMetadata.isGoodBuild());
        
        // Test multiple toggles
        buildMetadata.markAsBadBuild();
        buildMetadata.markAsGoodBuild();
        buildMetadata.markAsBadBuild();
        assertTrue(buildMetadata.getBadBuild());
    }
}