package com.google.clusterfuzz.core;

import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

/**
 * Test suite for ClusterFuzz Core module.
 * Runs all unit tests, integration tests, and performance benchmarks.
 */
@Suite
@SuiteDisplayName("ClusterFuzz Core Test Suite")
@SelectPackages({
    "com.google.clusterfuzz.core.entity",
    "com.google.clusterfuzz.core.repository", 
    "com.google.clusterfuzz.core.service",
    "com.google.clusterfuzz.core.performance"
})
public class TestSuite {
    // Test suite configuration class
    // All tests are discovered and run automatically based on package selection
}