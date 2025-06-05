package com.google.clusterfuzz.core.fuzzing;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

/**
 * Configuration for fuzzing engines.
 */
@Data
@Builder
public class FuzzingEngineConfig {
    
    /**
     * Working directory for fuzzing operations.
     */
    private String workDirectory;
    
    /**
     * Maximum memory limit in MB.
     */
    private int maxMemoryMb;
    
    /**
     * Default timeout for fuzzing sessions in seconds.
     */
    private int defaultTimeoutSeconds;
    
    /**
     * Number of parallel fuzzing processes.
     */
    private int parallelProcesses;
    
    /**
     * Engine-specific configuration options.
     */
    private Map<String, String> engineOptions;
    
    /**
     * Path to engine binaries (if not in PATH).
     */
    private String engineBinaryPath;
    
    /**
     * Enable debug logging for fuzzing operations.
     */
    private boolean debugLogging;
    
    /**
     * Maximum number of crashes to collect per session.
     */
    private int maxCrashesPerSession;
    
    /**
     * Enable coverage collection.
     */
    private boolean enableCoverage;
    
    /**
     * Corpus storage configuration.
     */
    private String corpusStoragePath;
}