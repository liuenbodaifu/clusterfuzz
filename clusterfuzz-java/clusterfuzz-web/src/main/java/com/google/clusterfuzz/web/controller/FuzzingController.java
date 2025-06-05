package com.google.clusterfuzz.web.controller;

import com.google.clusterfuzz.core.entity.FuzzingResult;
import com.google.clusterfuzz.core.entity.FuzzingTask;
import com.google.clusterfuzz.core.fuzzing.CoverageInfo;
import com.google.clusterfuzz.core.fuzzing.FuzzingEngine;
import com.google.clusterfuzz.core.fuzzing.FuzzingStatus;
import com.google.clusterfuzz.core.fuzzing.ReproductionResult;
import com.google.clusterfuzz.core.service.FuzzingEngineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * REST controller for fuzzing engine operations.
 */
@RestController
@RequestMapping("/api/fuzzing")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Fuzzing", description = "Fuzzing engine management and execution")
public class FuzzingController {

    private final FuzzingEngineService fuzzingEngineService;

    @GetMapping("/engines")
    @Operation(summary = "Get available fuzzing engines")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<Map<String, EngineInfo>> getAvailableEngines() {
        Map<String, FuzzingEngine> engines = fuzzingEngineService.getAvailableEngines();
        
        Map<String, EngineInfo> engineInfo = engines.entrySet().stream()
                .collect(java.util.stream.Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> EngineInfo.builder()
                                .name(entry.getValue().getName())
                                .version(entry.getValue().getVersion())
                                .supportedPlatforms(entry.getValue().getSupportedPlatforms())
                                .supportedFormats(entry.getValue().getSupportedFormats())
                                .available(entry.getValue().isAvailable())
                                .build()
                ));
        
        return ResponseEntity.ok(engineInfo);
    }

    @PostMapping("/start")
    @Operation(summary = "Start a fuzzing task")
    @PreAuthorize("hasAnyRole('ADMIN', 'BOT')")
    public ResponseEntity<FuzzingTaskResponse> startFuzzing(
            @Valid @RequestBody FuzzingTaskRequest request) {
        
        FuzzingTask task = FuzzingTask.builder()
                .taskId(request.getTaskId())
                .targetName(request.getTargetName())
                .targetPath(Paths.get(request.getTargetPath()))
                .arguments(request.getArguments())
                .corpusPath(request.getCorpusPath() != null ? Paths.get(request.getCorpusPath()) : null)
                .timeoutSeconds(request.getTimeoutSeconds())
                .memoryLimitMb(request.getMemoryLimitMb())
                .engineName(request.getEngineName())
                .engineOptions(request.getEngineOptions())
                .jobId(request.getJobId())
                .botId(request.getBotId())
                .priority(request.getPriority())
                .environment(request.getEnvironment())
                .workingDirectory(request.getWorkingDirectory() != null ? 
                        Paths.get(request.getWorkingDirectory()) : null)
                .enableCoverage(request.isEnableCoverage())
                .enableMinimization(request.isEnableMinimization())
                .maxCrashes(request.getMaxCrashes())
                .build();

        CompletableFuture<FuzzingResult> future = fuzzingEngineService.startFuzzing(task);
        
        return ResponseEntity.ok(FuzzingTaskResponse.builder()
                .taskId(task.getTaskId())
                .status("STARTED")
                .message("Fuzzing task started successfully")
                .build());
    }

    @PostMapping("/stop/{engineName}/{sessionId}")
    @Operation(summary = "Stop a fuzzing session")
    @PreAuthorize("hasAnyRole('ADMIN', 'BOT')")
    public ResponseEntity<Void> stopFuzzing(
            @PathVariable String engineName,
            @PathVariable String sessionId) {
        
        fuzzingEngineService.stopFuzzing(engineName, sessionId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/status/{engineName}/{sessionId}")
    @Operation(summary = "Get fuzzing session status")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER', 'BOT')")
    public ResponseEntity<FuzzingStatusResponse> getFuzzingStatus(
            @PathVariable String engineName,
            @PathVariable String sessionId) {
        
        FuzzingStatus status = fuzzingEngineService.getFuzzingStatus(engineName, sessionId);
        
        return ResponseEntity.ok(FuzzingStatusResponse.builder()
                .sessionId(sessionId)
                .engineName(engineName)
                .status(status.name())
                .build());
    }

    @PostMapping("/minimize")
    @Operation(summary = "Minimize a test case")
    @PreAuthorize("hasAnyRole('ADMIN', 'BOT')")
    public ResponseEntity<MinimizationResponse> minimizeTestCase(
            @RequestParam String engineName,
            @RequestParam MultipartFile testCase,
            @RequestParam String targetPath,
            @RequestParam(required = false) List<String> arguments) throws IOException {
        
        // Save uploaded test case to temporary file
        Path tempTestCase = Files.createTempFile("testcase_", ".bin");
        testCase.transferTo(tempTestCase.toFile());
        
        CompletableFuture<Path> future = fuzzingEngineService.minimizeTestCase(
                engineName, tempTestCase, Paths.get(targetPath), arguments);
        
        return ResponseEntity.ok(MinimizationResponse.builder()
                .status("STARTED")
                .message("Test case minimization started")
                .build());
    }

    @PostMapping("/reproduce")
    @Operation(summary = "Reproduce a crash")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER', 'BOT')")
    public ResponseEntity<ReproductionResponse> reproduceCrash(
            @RequestParam String engineName,
            @RequestParam MultipartFile testCase,
            @RequestParam String targetPath,
            @RequestParam(required = false) List<String> arguments) throws IOException {
        
        // Save uploaded test case to temporary file
        Path tempTestCase = Files.createTempFile("reproduce_", ".bin");
        testCase.transferTo(tempTestCase.toFile());
        
        CompletableFuture<ReproductionResult> future = fuzzingEngineService.reproduceCrash(
                engineName, tempTestCase, Paths.get(targetPath), arguments);
        
        return ResponseEntity.ok(ReproductionResponse.builder()
                .status("STARTED")
                .message("Crash reproduction started")
                .build());
    }

    @PostMapping("/coverage")
    @Operation(summary = "Generate coverage information")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER', 'BOT')")
    public ResponseEntity<CoverageResponse> generateCoverage(
            @RequestParam String engineName,
            @RequestParam MultipartFile testCase,
            @RequestParam String targetPath,
            @RequestParam(required = false) List<String> arguments) throws IOException {
        
        // Save uploaded test case to temporary file
        Path tempTestCase = Files.createTempFile("coverage_", ".bin");
        testCase.transferTo(tempTestCase.toFile());
        
        CompletableFuture<CoverageInfo> future = fuzzingEngineService.generateCoverage(
                engineName, tempTestCase, Paths.get(targetPath), arguments);
        
        return ResponseEntity.ok(CoverageResponse.builder()
                .status("STARTED")
                .message("Coverage generation started")
                .build());
    }

    @GetMapping("/recommendations")
    @Operation(summary = "Get engine recommendations for a target")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<List<String>> getEngineRecommendations(
            @RequestParam String targetPath,
            @RequestParam(required = false) List<String> arguments) {
        
        List<String> recommendations = fuzzingEngineService.getEngineRecommendations(
                Paths.get(targetPath), arguments);
        
        return ResponseEntity.ok(recommendations);
    }

    // DTOs for API requests and responses

    @lombok.Data
    @lombok.Builder
    public static class EngineInfo {
        private String name;
        private String version;
        private List<String> supportedPlatforms;
        private List<String> supportedFormats;
        private boolean available;
    }

    @lombok.Data
    @lombok.Builder
    public static class FuzzingTaskRequest {
        private String taskId;
        private String targetName;
        private String targetPath;
        private List<String> arguments;
        private String corpusPath;
        private int timeoutSeconds;
        private int memoryLimitMb;
        private String engineName;
        private Map<String, String> engineOptions;
        private Long jobId;
        private String botId;
        private int priority;
        private Map<String, String> environment;
        private String workingDirectory;
        private boolean enableCoverage;
        private boolean enableMinimization;
        private int maxCrashes;
    }

    @lombok.Data
    @lombok.Builder
    public static class FuzzingTaskResponse {
        private String taskId;
        private String status;
        private String message;
    }

    @lombok.Data
    @lombok.Builder
    public static class FuzzingStatusResponse {
        private String sessionId;
        private String engineName;
        private String status;
    }

    @lombok.Data
    @lombok.Builder
    public static class MinimizationResponse {
        private String status;
        private String message;
    }

    @lombok.Data
    @lombok.Builder
    public static class ReproductionResponse {
        private String status;
        private String message;
    }

    @lombok.Data
    @lombok.Builder
    public static class CoverageResponse {
        private String status;
        private String message;
    }
}