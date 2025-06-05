package com.google.clusterfuzz.core.fuzzing;

import lombok.Builder;
import lombok.Data;

/**
 * Result of crash reproduction attempt.
 */
@Data
@Builder
public class ReproductionResult {
    
    /**
     * Whether the crash was successfully reproduced.
     */
    private boolean reproduced;
    
    /**
     * Exit code of the reproduction attempt.
     */
    private int exitCode;
    
    /**
     * Output from the reproduction attempt.
     */
    private String output;
    
    /**
     * Type of crash detected (if any).
     */
    private String crashType;
    
    /**
     * Stack trace from the crash (if available).
     */
    private String stackTrace;
    
    /**
     * Signal that caused the crash (Unix systems).
     */
    private Integer signal;
    
    /**
     * Address where the crash occurred (if available).
     */
    private String crashAddress;
    
    /**
     * Additional crash details.
     */
    private String crashDetails;
}