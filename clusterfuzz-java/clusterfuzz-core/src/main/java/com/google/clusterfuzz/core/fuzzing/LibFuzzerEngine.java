package com.google.clusterfuzz.core.fuzzing;

import com.google.clusterfuzz.core.entity.FuzzingResult;
import com.google.clusterfuzz.core.entity.FuzzingTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * libFuzzer engine implementation.
 * Integrates with LLVM's libFuzzer for coverage-guided fuzzing.
 */
@Component
@Slf4j
public class LibFuzzerEngine implements FuzzingEngine {

    private static final String ENGINE_NAME = "libFuzzer";
    private static final Pattern CRASH_PATTERN = Pattern.compile("SUMMARY: .*Sanitizer: (.*) (.*)");
    private static final Pattern COVERAGE_PATTERN = Pattern.compile("cov: (\\d+)");
    private static final Pattern EXEC_PATTERN = Pattern.compile("#(\\d+)");

    private FuzzingEngineConfig config;
    private final Map<String, Process> activeSessions = new ConcurrentHashMap<>();
    private final Map<String, FuzzingStatus> sessionStatus = new ConcurrentHashMap<>();

    @Override
    public String getName() {
        return ENGINE_NAME;
    }

    @Override
    public String getVersion() {
        try {
            ProcessBuilder pb = new ProcessBuilder("clang", "--version");
            Process process = pb.start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line = reader.readLine();
                if (line != null && line.contains("clang version")) {
                    return line.substring(line.indexOf("clang version") + 13).split(" ")[0];
                }
            }
            process.waitFor(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("Could not determine libFuzzer version", e);
        }
        return "unknown";
    }

    @Override
    public boolean isAvailable() {
        try {
            ProcessBuilder pb = new ProcessBuilder("clang", "--version");
            Process process = pb.start();
            boolean finished = process.waitFor(5, TimeUnit.SECONDS);
            return finished && process.exitValue() == 0;
        } catch (Exception e) {
            log.debug("libFuzzer not available", e);
            return false;
        }
    }

    @Override
    public void initialize(FuzzingEngineConfig config) {
        this.config = config;
        log.info("Initialized libFuzzer engine with config: {}", config);
    }

    @Override
    public CompletableFuture<FuzzingResult> startFuzzing(FuzzingTask task) {
        String sessionId = UUID.randomUUID().toString();
        log.info("Starting libFuzzer session {} for task: {}", sessionId, task.getTargetName());

        return CompletableFuture.supplyAsync(() -> {
            try {
                return executeFuzzing(sessionId, task);
            } catch (Exception e) {
                log.error("Error during fuzzing session " + sessionId, e);
                throw new RuntimeException("Fuzzing failed", e);
            }
        });
    }

    private FuzzingResult executeFuzzing(String sessionId, FuzzingTask task) throws IOException, InterruptedException {
        // Prepare fuzzing environment
        Path workDir = Paths.get(config.getWorkDirectory(), sessionId);
        Files.createDirectories(workDir);
        
        Path corpusDir = workDir.resolve("corpus");
        Path crashDir = workDir.resolve("crashes");
        Files.createDirectories(corpusDir);
        Files.createDirectories(crashDir);

        // Copy initial corpus if provided
        if (task.getCorpusPath() != null) {
            copyCorpus(task.getCorpusPath(), corpusDir);
        }

        // Build command line
        List<String> command = buildFuzzingCommand(task, corpusDir, crashDir);
        
        log.info("Executing libFuzzer command: {}", String.join(" ", command));

        // Start fuzzing process
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.directory(workDir.toFile());
        pb.redirectErrorStream(true);
        
        Process process = pb.start();
        activeSessions.put(sessionId, process);
        sessionStatus.put(sessionId, FuzzingStatus.RUNNING);

        // Monitor fuzzing output
        FuzzingResult result = monitorFuzzingProcess(sessionId, process, crashDir);
        
        // Cleanup
        activeSessions.remove(sessionId);
        sessionStatus.put(sessionId, FuzzingStatus.COMPLETED);
        
        return result;
    }

    private List<String> buildFuzzingCommand(FuzzingTask task, Path corpusDir, Path crashDir) {
        List<String> command = new ArrayList<>();
        
        // Target binary
        command.add(task.getTargetPath().toString());
        
        // Corpus directory
        command.add(corpusDir.toString());
        
        // Basic libFuzzer options
        command.add("-artifact_prefix=" + crashDir.toString() + "/");
        command.add("-max_total_time=" + task.getTimeoutSeconds());
        command.add("-print_final_stats=1");
        command.add("-print_corpus_stats=1");
        
        // Memory limit
        if (task.getMemoryLimitMb() > 0) {
            command.add("-rss_limit_mb=" + task.getMemoryLimitMb());
        }
        
        // Additional arguments from task
        if (task.getArguments() != null) {
            command.addAll(task.getArguments());
        }
        
        return command;
    }

    private void copyCorpus(Path sourcePath, Path targetDir) throws IOException {
        if (Files.isDirectory(sourcePath)) {
            Files.walk(sourcePath)
                    .filter(Files::isRegularFile)
                    .forEach(file -> {
                        try {
                            Path target = targetDir.resolve(file.getFileName());
                            Files.copy(file, target);
                        } catch (IOException e) {
                            log.warn("Failed to copy corpus file: " + file, e);
                        }
                    });
        } else if (Files.isRegularFile(sourcePath)) {
            Files.copy(sourcePath, targetDir.resolve(sourcePath.getFileName()));
        }
    }

    private FuzzingResult monitorFuzzingProcess(String sessionId, Process process, Path crashDir) {
        FuzzingResult.Builder resultBuilder = FuzzingResult.builder()
                .sessionId(sessionId)
                .engineName(ENGINE_NAME)
                .startTime(LocalDateTime.now());

        List<String> crashes = new ArrayList<>();
        int executions = 0;
        int coverage = 0;
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null && process.isAlive()) {
                log.debug("libFuzzer output: {}", line);
                
                // Parse crash information
                Matcher crashMatcher = CRASH_PATTERN.matcher(line);
                if (crashMatcher.find()) {
                    String crashType = crashMatcher.group(1);
                    String crashLocation = crashMatcher.group(2);
                    crashes.add(crashType + " at " + crashLocation);
                    log.info("Crash detected in session {}: {} at {}", sessionId, crashType, crashLocation);
                }
                
                // Parse coverage information
                Matcher coverageMatcher = COVERAGE_PATTERN.matcher(line);
                if (coverageMatcher.find()) {
                    coverage = Integer.parseInt(coverageMatcher.group(1));
                }
                
                // Parse execution count
                Matcher execMatcher = EXEC_PATTERN.matcher(line);
                if (execMatcher.find()) {
                    executions = Integer.parseInt(execMatcher.group(1));
                }
            }
            
            // Wait for process completion
            process.waitFor();
            
        } catch (IOException | InterruptedException e) {
            log.error("Error monitoring fuzzing process", e);
        }

        // Collect crash files
        List<Path> crashFiles = collectCrashFiles(crashDir);
        
        return resultBuilder
                .endTime(LocalDateTime.now())
                .executions(executions)
                .coverage(coverage)
                .crashCount(crashes.size())
                .crashes(crashes)
                .crashFiles(crashFiles)
                .exitCode(process.exitValue())
                .build();
    }

    private List<Path> collectCrashFiles(Path crashDir) {
        List<Path> crashFiles = new ArrayList<>();
        try {
            if (Files.exists(crashDir)) {
                Files.walk(crashDir)
                        .filter(Files::isRegularFile)
                        .filter(path -> path.getFileName().toString().startsWith("crash-"))
                        .forEach(crashFiles::add);
            }
        } catch (IOException e) {
            log.error("Error collecting crash files", e);
        }
        return crashFiles;
    }

    @Override
    public void stopFuzzing(String sessionId) {
        Process process = activeSessions.get(sessionId);
        if (process != null && process.isAlive()) {
            log.info("Stopping fuzzing session: {}", sessionId);
            process.destroyForcibly();
            sessionStatus.put(sessionId, FuzzingStatus.STOPPED);
        }
    }

    @Override
    public FuzzingStatus getFuzzingStatus(String sessionId) {
        return sessionStatus.getOrDefault(sessionId, FuzzingStatus.UNKNOWN);
    }

    @Override
    public CompletableFuture<Path> minimizeTestCase(Path testCase, Path target, List<String> args) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return executeMinimization(testCase, target, args);
            } catch (Exception e) {
                log.error("Error minimizing test case", e);
                throw new RuntimeException("Minimization failed", e);
            }
        });
    }

    private Path executeMinimization(Path testCase, Path target, List<String> args) throws IOException, InterruptedException {
        Path workDir = Files.createTempDirectory("minimize_");
        Path minimizedFile = workDir.resolve("minimized_" + testCase.getFileName());
        
        List<String> command = new ArrayList<>();
        command.add(target.toString());
        command.add("-minimize_crash=1");
        command.add("-exact_artifact_path=" + minimizedFile.toString());
        command.add(testCase.toString());
        
        if (args != null) {
            command.addAll(args);
        }
        
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.directory(workDir.toFile());
        Process process = pb.start();
        
        int exitCode = process.waitFor();
        if (exitCode == 0 && Files.exists(minimizedFile)) {
            return minimizedFile;
        } else {
            throw new RuntimeException("Minimization failed with exit code: " + exitCode);
        }
    }

    @Override
    public CompletableFuture<ReproductionResult> reproduceCrash(Path testCase, Path target, List<String> args) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return executeReproduction(testCase, target, args);
            } catch (Exception e) {
                log.error("Error reproducing crash", e);
                throw new RuntimeException("Reproduction failed", e);
            }
        });
    }

    private ReproductionResult executeReproduction(Path testCase, Path target, List<String> args) throws IOException, InterruptedException {
        List<String> command = new ArrayList<>();
        command.add(target.toString());
        command.add(testCase.toString());
        
        if (args != null) {
            command.addAll(args);
        }
        
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);
        Process process = pb.start();
        
        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        }
        
        int exitCode = process.waitFor();
        
        return ReproductionResult.builder()
                .reproduced(exitCode != 0)
                .exitCode(exitCode)
                .output(output.toString())
                .crashType(extractCrashType(output.toString()))
                .build();
    }

    private String extractCrashType(String output) {
        Matcher matcher = CRASH_PATTERN.matcher(output);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "unknown";
    }

    @Override
    public CompletableFuture<CoverageInfo> generateCoverage(Path testCase, Path target, List<String> args) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return executeCoverageGeneration(testCase, target, args);
            } catch (Exception e) {
                log.error("Error generating coverage", e);
                throw new RuntimeException("Coverage generation failed", e);
            }
        });
    }

    private CoverageInfo executeCoverageGeneration(Path testCase, Path target, List<String> args) throws IOException, InterruptedException {
        // This would require a coverage-instrumented binary
        // For now, return basic coverage info
        return CoverageInfo.builder()
                .totalLines(0)
                .coveredLines(0)
                .coveragePercentage(0.0)
                .build();
    }

    @Override
    public List<String> getSupportedPlatforms() {
        return Arrays.asList("linux", "macos", "windows");
    }

    @Override
    public List<String> getSupportedFormats() {
        return Arrays.asList("binary", "text", "structured");
    }

    @Override
    public void cleanup() {
        // Stop all active sessions
        activeSessions.values().forEach(process -> {
            if (process.isAlive()) {
                process.destroyForcibly();
            }
        });
        activeSessions.clear();
        sessionStatus.clear();
        log.info("libFuzzer engine cleaned up");
    }
}