package com.google.clusterfuzz.core.entity;

import lombok.Builder;
import lombok.Data;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Result of a fuzzing session.
 */
@Data
@Builder
public class FuzzingResult {
    
    /**
     * Session ID for this fuzzing run.
     */
    private String sessionId;
    
    /**
     * Name of the fuzzing engine used.
     */
    private String engineName;
    
    /**
     * Start time of the fuzzing session.
     */
    private LocalDateTime startTime;
    
    /**
     * End time of the fuzzing session.
     */
    private LocalDateTime endTime;
    
    /**
     * Total number of executions performed.
     */
    private int executions;
    
    /**
     * Coverage achieved (engine-specific metric).
     */
    private int coverage;
    
    /**
     * Number of crashes found.
     */
    private int crashCount;
    
    /**
     * List of crash descriptions.
     */
    private List<String> crashes;
    
    /**
     * Paths to crash files.
     */
    private List<Path> crashFiles;
    
    /**
     * Exit code of the fuzzing process.
     */
    private int exitCode;
    
    /**
     * Whether the session completed successfully.
     */
    private boolean successful;
    
    /**
     * Error message if the session failed.
     */
    private String errorMessage;
    
    /**
     * Statistics from the fuzzing session.
     */
    private FuzzingStatistics statistics;
    
    /**
     * Path to the final corpus.
     */
    private Path finalCorpusPath;
    
    /**
     * Size of the final corpus.
     */
    private int finalCorpusSize;
    
    @Data
    @Builder
    public static class FuzzingStatistics {
        private int totalExecs;
        private int crashesFound;
        private int timeoutsFound;
        private int ooms; // Out of memory
        private double execsPerSecond;
        private int peakMemoryMb;
        private int corpusSize;
        private int featuresFound;
    }
}