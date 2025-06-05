package com.google.clusterfuzz.core.fuzzing;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

/**
 * Coverage information for a test case or fuzzing session.
 */
@Data
@Builder
public class CoverageInfo {
    
    /**
     * Total number of lines in the target.
     */
    private int totalLines;
    
    /**
     * Number of lines covered.
     */
    private int coveredLines;
    
    /**
     * Coverage percentage (0.0 to 100.0).
     */
    private double coveragePercentage;
    
    /**
     * Total number of functions in the target.
     */
    private int totalFunctions;
    
    /**
     * Number of functions covered.
     */
    private int coveredFunctions;
    
    /**
     * Function coverage percentage.
     */
    private double functionCoveragePercentage;
    
    /**
     * Total number of branches.
     */
    private int totalBranches;
    
    /**
     * Number of branches covered.
     */
    private int coveredBranches;
    
    /**
     * Branch coverage percentage.
     */
    private double branchCoveragePercentage;
    
    /**
     * Coverage data per file.
     */
    private Map<String, FileCoverageInfo> fileCoverage;
    
    /**
     * Raw coverage data (format depends on engine).
     */
    private String rawCoverageData;
    
    @Data
    @Builder
    public static class FileCoverageInfo {
        private String filename;
        private int totalLines;
        private int coveredLines;
        private double coveragePercentage;
    }
}