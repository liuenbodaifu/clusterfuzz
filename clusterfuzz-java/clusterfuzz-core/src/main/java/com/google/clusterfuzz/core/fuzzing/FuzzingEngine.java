package com.google.clusterfuzz.core.fuzzing;

import com.google.clusterfuzz.core.entity.FuzzingResult;
import com.google.clusterfuzz.core.entity.FuzzingTask;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Abstract interface for all fuzzing engines.
 * Provides a unified API for different fuzzing technologies (libFuzzer, AFL, etc.).
 */
public interface FuzzingEngine {

    /**
     * Get the name of this fuzzing engine.
     */
    String getName();

    /**
     * Get the version of this fuzzing engine.
     */
    String getVersion();

    /**
     * Check if this engine is available and properly configured.
     */
    boolean isAvailable();

    /**
     * Initialize the fuzzing engine with the given configuration.
     */
    void initialize(FuzzingEngineConfig config);

    /**
     * Start a fuzzing session asynchronously.
     * 
     * @param task The fuzzing task configuration
     * @return CompletableFuture that completes when fuzzing finishes
     */
    CompletableFuture<FuzzingResult> startFuzzing(FuzzingTask task);

    /**
     * Stop an active fuzzing session.
     * 
     * @param sessionId The ID of the session to stop
     */
    void stopFuzzing(String sessionId);

    /**
     * Get the current status of a fuzzing session.
     * 
     * @param sessionId The ID of the session
     * @return Current fuzzing status
     */
    FuzzingStatus getFuzzingStatus(String sessionId);

    /**
     * Minimize a test case to its smallest crashing form.
     * 
     * @param testCase Path to the test case file
     * @param target Path to the target binary
     * @param args Command line arguments for the target
     * @return Path to the minimized test case
     */
    CompletableFuture<Path> minimizeTestCase(Path testCase, Path target, List<String> args);

    /**
     * Reproduce a crash with the given test case.
     * 
     * @param testCase Path to the test case file
     * @param target Path to the target binary
     * @param args Command line arguments for the target
     * @return Reproduction result with crash details
     */
    CompletableFuture<ReproductionResult> reproduceCrash(Path testCase, Path target, List<String> args);

    /**
     * Generate coverage information for the given test case.
     * 
     * @param testCase Path to the test case file
     * @param target Path to the target binary
     * @param args Command line arguments for the target
     * @return Coverage information
     */
    CompletableFuture<CoverageInfo> generateCoverage(Path testCase, Path target, List<String> args);

    /**
     * Get supported platforms for this engine.
     */
    List<String> getSupportedPlatforms();

    /**
     * Get supported file formats for this engine.
     */
    List<String> getSupportedFormats();

    /**
     * Clean up resources used by this engine.
     */
    void cleanup();
}