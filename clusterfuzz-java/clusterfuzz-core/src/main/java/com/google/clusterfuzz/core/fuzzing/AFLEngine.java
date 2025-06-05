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
 * AFL (American Fuzzy Lop) engine implementation.
 * Integrates with AFL++ for coverage-guided fuzzing.
 */
@Component
@Slf4j
public class AFLEngine implements FuzzingEngine {

    private static final String ENGINE_NAME = "AFL";
    private static final Pattern STATS_PATTERN = Pattern.compile("(\\w+)\\s*:\\s*(.+)");
    
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
            ProcessBuilder pb = new ProcessBuilder("afl-fuzz", "-h");
            Process process = pb.start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                String line = reader.readLine();
                if (line != null && line.contains("afl-fuzz")) {
                    // Extract version from first line
                    String[] parts = line.split(" ");
                    for (int i = 0; i < parts.length; i++) {
                        if (parts[i].matches("\\d+\\.\\d+.*")) {
                            return parts[i];
                        }
                    }
                }
            }
            process.waitFor(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("Could not determine AFL version", e);
        }
        return "unknown";
    }

    @Override
    public boolean isAvailable() {
        try {
            ProcessBuilder pb = new ProcessBuilder("afl-fuzz", "-h");
            Process process = pb.start();
            boolean finished = process.waitFor(5, TimeUnit.SECONDS);
            return finished && process.exitValue() == 0;
        } catch (Exception e) {
            log.debug("AFL not available", e);
            return false;
        }
    }

    @Override
    public void initialize(FuzzingEngineConfig config) {
        this.config = config;
        log.info("Initialized AFL engine with config: {}", config);
    }

    @Override
    public CompletableFuture<FuzzingResult> startFuzzing(FuzzingTask task) {
        String sessionId = UUID.randomUUID().toString();
        log.info("Starting AFL session {} for task: {}", sessionId, task.getTargetName());

        return CompletableFuture.supplyAsync(() -> {
            try {
                return executeFuzzing(sessionId, task);
            } catch (Exception e) {
                log.error("Error during AFL fuzzing session " + sessionId, e);
                throw new RuntimeException("AFL fuzzing failed", e);
            }
        });
    }

    private FuzzingResult executeFuzzing(String sessionId, FuzzingTask task) throws IOException, InterruptedException {
        // Prepare AFL environment
        Path workDir = Paths.get(config.getWorkDirectory(), sessionId);
        Files.createDirectories(workDir);
        
        Path inputDir = workDir.resolve("input");
        Path outputDir = workDir.resolve("output");
        Files.createDirectories(inputDir);
        Files.createDirectories(outputDir);

        // Copy initial corpus
        if (task.getCorpusPath() != null) {
            copyCorpus(task.getCorpusPath(), inputDir);
        } else {
            // Create minimal seed if no corpus provided
            createMinimalSeed(inputDir);
        }

        // Build AFL command
        List<String> command = buildAFLCommand(task, inputDir, outputDir);
        
        log.info("Executing AFL command: {}", String.join(" ", command));

        // Start AFL process
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.directory(workDir.toFile());
        pb.environment().put("AFL_I_DONT_CARE_ABOUT_MISSING_CRASHES", "1");
        pb.environment().put("AFL_SKIP_CPUFREQ", "1");
        
        Process process = pb.start();
        activeSessions.put(sessionId, process);
        sessionStatus.put(sessionId, FuzzingStatus.RUNNING);

        // Monitor AFL process
        FuzzingResult result = monitorAFLProcess(sessionId, process, outputDir, task.getTimeoutSeconds());
        
        // Cleanup
        activeSessions.remove(sessionId);
        sessionStatus.put(sessionId, FuzzingStatus.COMPLETED);
        
        return result;
    }

    private List<String> buildAFLCommand(FuzzingTask task, Path inputDir, Path outputDir) {
        List<String> command = new ArrayList<>();
        
        // AFL binary
        command.add("afl-fuzz");
        
        // Input directory
        command.add("-i");
        command.add(inputDir.toString());
        
        // Output directory
        command.add("-o");
        command.add(outputDir.toString());
        
        // Memory limit
        if (task.getMemoryLimitMb() > 0) {
            command.add("-m");
            command.add(String.valueOf(task.getMemoryLimitMb()));
        }
        
        // Timeout
        if (task.getTimeoutSeconds() > 0) {
            command.add("-t");
            command.add(String.valueOf(task.getTimeoutSeconds() * 1000)); // AFL expects milliseconds
        }
        
        // Additional AFL options
        command.add("-d"); // Skip deterministic stage
        
        // Target binary and arguments
        command.add("--");
        command.add(task.getTargetPath().toString());
        
        if (task.getArguments() != null) {
            command.addAll(task.getArguments());
        }
        
        // Add @@ placeholder if not present
        if (!command.contains("@@")) {
            command.add("@@");
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

    private void createMinimalSeed(Path inputDir) throws IOException {
        // Create a minimal seed file
        Path seedFile = inputDir.resolve("seed");
        Files.write(seedFile, "A".getBytes());
    }

    private FuzzingResult monitorAFLProcess(String sessionId, Process process, Path outputDir, int timeoutSeconds) {
        FuzzingResult.Builder resultBuilder = FuzzingResult.builder()
                .sessionId(sessionId)
                .engineName(ENGINE_NAME)
                .startTime(LocalDateTime.now());

        // Wait for AFL to start and create stats file
        Path statsFile = outputDir.resolve("fuzzer_stats");
        long startTime = System.currentTimeMillis();
        long timeoutMs = timeoutSeconds * 1000L;
        
        try {
            // Wait for process to complete or timeout
            if (timeoutSeconds > 0) {
                boolean finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);
                if (!finished) {
                    process.destroyForcibly();
                    sessionStatus.put(sessionId, FuzzingStatus.TIMEOUT);
                }
            } else {
                process.waitFor();
            }
            
            // Parse final statistics
            AFLStats stats = parseAFLStats(statsFile);
            
            // Collect crashes
            List<Path> crashFiles = collectCrashFiles(outputDir);
            
            return resultBuilder
                    .endTime(LocalDateTime.now())
                    .executions(stats.totalExecs)
                    .coverage(stats.mapSize)
                    .crashCount(crashFiles.size())
                    .crashFiles(crashFiles)
                    .exitCode(process.exitValue())
                    .successful(process.exitValue() == 0)
                    .finalCorpusPath(outputDir.resolve("queue"))
                    .finalCorpusSize(stats.corpusCount)
                    .statistics(FuzzingResult.FuzzingStatistics.builder()
                            .totalExecs(stats.totalExecs)
                            .crashesFound(stats.uniqueCrashes)
                            .timeoutsFound(stats.uniqueTimeouts)
                            .execsPerSecond(stats.execsPerSec)
                            .corpusSize(stats.corpusCount)
                            .build())
                    .build();
            
        } catch (InterruptedException e) {
            log.error("AFL monitoring interrupted", e);
            process.destroyForcibly();
            return resultBuilder
                    .endTime(LocalDateTime.now())
                    .successful(false)
                    .errorMessage("Monitoring interrupted")
                    .build();
        }
    }

    private AFLStats parseAFLStats(Path statsFile) {
        AFLStats stats = new AFLStats();
        
        if (!Files.exists(statsFile)) {
            log.warn("AFL stats file not found: {}", statsFile);
            return stats;
        }
        
        try {
            List<String> lines = Files.readAllLines(statsFile);
            for (String line : lines) {
                Matcher matcher = STATS_PATTERN.matcher(line);
                if (matcher.find()) {
                    String key = matcher.group(1);
                    String value = matcher.group(2);
                    
                    switch (key) {
                        case "execs_done":
                            stats.totalExecs = Integer.parseInt(value);
                            break;
                        case "execs_per_sec":
                            stats.execsPerSec = Double.parseDouble(value);
                            break;
                        case "corpus_count":
                            stats.corpusCount = Integer.parseInt(value);
                            break;
                        case "unique_crashes":
                            stats.uniqueCrashes = Integer.parseInt(value);
                            break;
                        case "unique_hangs":
                            stats.uniqueTimeouts = Integer.parseInt(value);
                            break;
                        case "map_size":
                            stats.mapSize = Integer.parseInt(value);
                            break;
                    }
                }
            }
        } catch (IOException | NumberFormatException e) {
            log.error("Error parsing AFL stats", e);
        }
        
        return stats;
    }

    private List<Path> collectCrashFiles(Path outputDir) {
        List<Path> crashFiles = new ArrayList<>();
        Path crashesDir = outputDir.resolve("crashes");
        
        try {
            if (Files.exists(crashesDir)) {
                Files.walk(crashesDir)
                        .filter(Files::isRegularFile)
                        .filter(path -> !path.getFileName().toString().equals("README.txt"))
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
            log.info("Stopping AFL session: {}", sessionId);
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
                return executeAFLTmin(testCase, target, args);
            } catch (Exception e) {
                log.error("Error minimizing test case with AFL", e);
                throw new RuntimeException("AFL minimization failed", e);
            }
        });
    }

    private Path executeAFLTmin(Path testCase, Path target, List<String> args) throws IOException, InterruptedException {
        Path workDir = Files.createTempDirectory("afl_tmin_");
        Path minimizedFile = workDir.resolve("minimized_" + testCase.getFileName());
        
        List<String> command = new ArrayList<>();
        command.add("afl-tmin");
        command.add("-i");
        command.add(testCase.toString());
        command.add("-o");
        command.add(minimizedFile.toString());
        command.add("--");
        command.add(target.toString());
        
        if (args != null) {
            command.addAll(args);
        }
        
        if (!command.contains("@@")) {
            command.add("@@");
        }
        
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.directory(workDir.toFile());
        Process process = pb.start();
        
        int exitCode = process.waitFor();
        if (exitCode == 0 && Files.exists(minimizedFile)) {
            return minimizedFile;
        } else {
            throw new RuntimeException("AFL minimization failed with exit code: " + exitCode);
        }
    }

    @Override
    public CompletableFuture<ReproductionResult> reproduceCrash(Path testCase, Path target, List<String> args) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return executeReproduction(testCase, target, args);
            } catch (Exception e) {
                log.error("Error reproducing crash with AFL", e);
                throw new RuntimeException("AFL reproduction failed", e);
            }
        });
    }

    private ReproductionResult executeReproduction(Path testCase, Path target, List<String> args) throws IOException, InterruptedException {
        List<String> command = new ArrayList<>();
        command.add(target.toString());
        
        if (args != null) {
            // Replace @@ with actual test case path
            for (String arg : args) {
                if ("@@".equals(arg)) {
                    command.add(testCase.toString());
                } else {
                    command.add(arg);
                }
            }
        } else {
            command.add(testCase.toString());
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
        if (output.contains("SIGSEGV")) return "SIGSEGV";
        if (output.contains("SIGABRT")) return "SIGABRT";
        if (output.contains("SIGFPE")) return "SIGFPE";
        if (output.contains("AddressSanitizer")) return "AddressSanitizer";
        return "unknown";
    }

    @Override
    public CompletableFuture<CoverageInfo> generateCoverage(Path testCase, Path target, List<String> args) {
        return CompletableFuture.supplyAsync(() -> {
            // AFL doesn't directly provide coverage info, would need gcov integration
            return CoverageInfo.builder()
                    .totalLines(0)
                    .coveredLines(0)
                    .coveragePercentage(0.0)
                    .build();
        });
    }

    @Override
    public List<String> getSupportedPlatforms() {
        return Arrays.asList("linux", "macos");
    }

    @Override
    public List<String> getSupportedFormats() {
        return Arrays.asList("binary", "file-based");
    }

    @Override
    public void cleanup() {
        activeSessions.values().forEach(process -> {
            if (process.isAlive()) {
                process.destroyForcibly();
            }
        });
        activeSessions.clear();
        sessionStatus.clear();
        log.info("AFL engine cleaned up");
    }

    private static class AFLStats {
        int totalExecs = 0;
        double execsPerSec = 0.0;
        int corpusCount = 0;
        int uniqueCrashes = 0;
        int uniqueTimeouts = 0;
        int mapSize = 0;
    }
}