package com.google.clusterfuzz.core.fuzzing;

/**
 * Status of a fuzzing session.
 */
public enum FuzzingStatus {
    /**
     * Fuzzing session is initializing.
     */
    INITIALIZING,
    
    /**
     * Fuzzing session is currently running.
     */
    RUNNING,
    
    /**
     * Fuzzing session has been paused.
     */
    PAUSED,
    
    /**
     * Fuzzing session was stopped by user.
     */
    STOPPED,
    
    /**
     * Fuzzing session completed successfully.
     */
    COMPLETED,
    
    /**
     * Fuzzing session failed with an error.
     */
    FAILED,
    
    /**
     * Fuzzing session timed out.
     */
    TIMEOUT,
    
    /**
     * Status is unknown or session not found.
     */
    UNKNOWN
}