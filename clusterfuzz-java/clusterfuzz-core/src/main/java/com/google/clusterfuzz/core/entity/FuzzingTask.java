package com.google.clusterfuzz.core.entity;

import lombok.Builder;
import lombok.Data;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * Configuration for a fuzzing task.
 */
@Data
@Builder
public class FuzzingTask {
    
    /**
     * Unique identifier for this task.
     */
    private String taskId;
    
    /**
     * Name of the target being fuzzed.
     */
    private String targetName;
    
    /**
     * Path to the target binary.
     */
    private Path targetPath;
    
    /**
     * Command line arguments for the target.
     */
    private List<String> arguments;
    
    /**
     * Path to initial corpus directory or file.
     */
    private Path corpusPath;
    
    /**
     * Maximum time to run fuzzing (in seconds).
     */
    private int timeoutSeconds;
    
    /**
     * Memory limit in MB.
     */
    private int memoryLimitMb;
    
    /**
     * Fuzzing engine to use.
     */
    private String engineName;
    
    /**
     * Engine-specific options.
     */
    private Map<String, String> engineOptions;
    
    /**
     * Job ID this task belongs to.
     */
    private Long jobId;
    
    /**
     * Bot ID executing this task.
     */
    private String botId;
    
    /**
     * Priority of this task (higher = more important).
     */
    private int priority;
    
    /**
     * Environment variables for the target.
     */
    private Map<String, String> environment;
    
    /**
     * Working directory for the task.
     */
    private Path workingDirectory;
    
    /**
     * Whether to enable coverage collection.
     */
    private boolean enableCoverage;
    
    /**
     * Whether to enable crash minimization.
     */
    private boolean enableMinimization;
    
    /**
     * Maximum number of crashes to collect.
     */
    private int maxCrashes;
}