package com.google.clusterfuzz.core.service;

import com.google.clusterfuzz.core.entity.FuzzingResult;
import com.google.clusterfuzz.core.entity.FuzzingTask;
import com.google.clusterfuzz.core.fuzzing.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Service for managing fuzzing engines and executing fuzzing tasks.
 */
@Service
@Slf4j
public class FuzzingEngineService {

    private final Map<String, FuzzingEngine> engines = new HashMap<>();
    private FuzzingEngineConfig defaultConfig;

    @Autowired
    private LibFuzzerEngine libFuzzerEngine;

    @Autowired
    private AFLEngine aflEngine;

    @PostConstruct
    public void initialize() {
        // Initialize default configuration
        defaultConfig = FuzzingEngineConfig.builder()
                .workDirectory("/tmp/clusterfuzz/fuzzing")
                .maxMemoryMb(2048)
                .defaultTimeoutSeconds(3600)
                .parallelProcesses(1)
                .debugLogging(false)
                .maxCrashesPerSession(100)
                .enableCoverage(true)
                .corpusStoragePath("/tmp/clusterfuzz/corpus")
                .build();

        // Register available engines
        registerEngine(libFuzzerEngine);
        registerEngine(aflEngine);

        log.info("Fuzzing engine service initialized with {} engines", engines.size());
    }

    private void registerEngine(FuzzingEngine engine) {
        if (engine.isAvailable()) {
            engine.initialize(defaultConfig);
            engines.put(engine.getName().toLowerCase(), engine);
            log.info("Registered fuzzing engine: {} (version: {})", 
                    engine.getName(), engine.getVersion());
        } else {
            log.warn("Fuzzing engine {} is not available", engine.getName());
        }
    }

    /**
     * Get all available fuzzing engines.
     */
    public Map<String, FuzzingEngine> getAvailableEngines() {
        return new HashMap<>(engines);
    }

    /**
     * Get a specific fuzzing engine by name.
     */
    public FuzzingEngine getEngine(String engineName) {
        FuzzingEngine engine = engines.get(engineName.toLowerCase());
        if (engine == null) {
            throw new IllegalArgumentException("Fuzzing engine not found: " + engineName);
        }
        return engine;
    }

    /**
     * Check if a fuzzing engine is available.
     */
    public boolean isEngineAvailable(String engineName) {
        return engines.containsKey(engineName.toLowerCase());
    }

    /**
     * Start a fuzzing task with the specified engine.
     */
    public CompletableFuture<FuzzingResult> startFuzzing(FuzzingTask task) {
        String engineName = task.getEngineName();
        if (engineName == null || engineName.isEmpty()) {
            engineName = selectDefaultEngine(task);
        }

        FuzzingEngine engine = getEngine(engineName);
        log.info("Starting fuzzing task {} with engine {}", task.getTaskId(), engineName);

        return engine.startFuzzing(task)
                .whenComplete((result, throwable) -> {
                    if (throwable != null) {
                        log.error("Fuzzing task {} failed", task.getTaskId(), throwable);
                    } else {
                        log.info("Fuzzing task {} completed with {} crashes", 
                                task.getTaskId(), result.getCrashCount());
                    }
                });
    }

    /**
     * Stop a fuzzing session.
     */
    public void stopFuzzing(String engineName, String sessionId) {
        FuzzingEngine engine = getEngine(engineName);
        engine.stopFuzzing(sessionId);
        log.info("Stopped fuzzing session {} on engine {}", sessionId, engineName);
    }

    /**
     * Get the status of a fuzzing session.
     */
    public FuzzingStatus getFuzzingStatus(String engineName, String sessionId) {
        FuzzingEngine engine = getEngine(engineName);
        return engine.getFuzzingStatus(sessionId);
    }

    /**
     * Minimize a test case using the specified engine.
     */
    public CompletableFuture<Path> minimizeTestCase(String engineName, Path testCase, 
                                                   Path target, List<String> args) {
        FuzzingEngine engine = getEngine(engineName);
        log.info("Minimizing test case {} with engine {}", testCase.getFileName(), engineName);
        
        return engine.minimizeTestCase(testCase, target, args)
                .whenComplete((result, throwable) -> {
                    if (throwable != null) {
                        log.error("Test case minimization failed", throwable);
                    } else {
                        log.info("Test case minimized successfully: {}", result);
                    }
                });
    }

    /**
     * Reproduce a crash using the specified engine.
     */
    public CompletableFuture<ReproductionResult> reproduceCrash(String engineName, Path testCase,
                                                               Path target, List<String> args) {
        FuzzingEngine engine = getEngine(engineName);
        log.info("Reproducing crash with test case {} using engine {}", 
                testCase.getFileName(), engineName);
        
        return engine.reproduceCrash(testCase, target, args)
                .whenComplete((result, throwable) -> {
                    if (throwable != null) {
                        log.error("Crash reproduction failed", throwable);
                    } else {
                        log.info("Crash reproduction completed: reproduced={}", result.isReproduced());
                    }
                });
    }

    /**
     * Generate coverage information using the specified engine.
     */
    public CompletableFuture<CoverageInfo> generateCoverage(String engineName, Path testCase,
                                                           Path target, List<String> args) {
        FuzzingEngine engine = getEngine(engineName);
        log.info("Generating coverage for test case {} using engine {}", 
                testCase.getFileName(), engineName);
        
        return engine.generateCoverage(testCase, target, args)
                .whenComplete((result, throwable) -> {
                    if (throwable != null) {
                        log.error("Coverage generation failed", throwable);
                    } else {
                        log.info("Coverage generated: {}% line coverage", result.getCoveragePercentage());
                    }
                });
    }

    /**
     * Select the best engine for a given task.
     */
    private String selectDefaultEngine(FuzzingTask task) {
        // Simple selection logic - can be enhanced with more sophisticated rules
        
        // Prefer libFuzzer for in-process fuzzing
        if (engines.containsKey("libfuzzer")) {
            return "libfuzzer";
        }
        
        // Fall back to AFL for file-based fuzzing
        if (engines.containsKey("afl")) {
            return "afl";
        }
        
        // Return first available engine
        return engines.keySet().iterator().next();
    }

    /**
     * Get engine recommendations for a target.
     */
    public List<String> getEngineRecommendations(Path targetPath, List<String> args) {
        // Analyze target and arguments to recommend best engines
        // This is a simplified implementation
        
        if (args != null && args.contains("@@")) {
            // File-based input suggests AFL
            return List.of("afl", "libfuzzer");
        } else {
            // In-process suggests libFuzzer
            return List.of("libfuzzer", "afl");
        }
    }

    /**
     * Update engine configuration.
     */
    public void updateEngineConfig(FuzzingEngineConfig config) {
        this.defaultConfig = config;
        
        // Re-initialize all engines with new config
        engines.values().forEach(engine -> engine.initialize(config));
        
        log.info("Updated fuzzing engine configuration");
    }

    /**
     * Get current engine configuration.
     */
    public FuzzingEngineConfig getEngineConfig() {
        return defaultConfig;
    }

    @PreDestroy
    public void cleanup() {
        log.info("Cleaning up fuzzing engines");
        engines.values().forEach(FuzzingEngine::cleanup);
        engines.clear();
    }
}