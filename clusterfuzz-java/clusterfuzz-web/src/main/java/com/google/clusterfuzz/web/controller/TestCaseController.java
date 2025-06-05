package com.google.clusterfuzz.web.controller;

import com.google.clusterfuzz.core.entity.TestCase;
import com.google.clusterfuzz.core.service.TestCaseService;
import com.google.clusterfuzz.web.dto.TestCaseDto;
import com.google.clusterfuzz.web.dto.TestCaseListResponse;
import com.google.clusterfuzz.web.dto.TestCaseSearchRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

/**
 * REST API controller for TestCase management.
 * Provides endpoints for CRUD operations, search, and testcase-specific actions.
 */
@RestController
@RequestMapping("/api/v1/testcases")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "TestCase Management", description = "APIs for managing test cases")
public class TestCaseController {

    private final TestCaseService testCaseService;

    /**
     * Get paginated list of testcases with optional filtering.
     */
    @GetMapping
    @Operation(summary = "List testcases", description = "Get paginated list of testcases with optional filtering")
    public ResponseEntity<TestCaseListResponse> getTestCases(
            @Parameter(description = "Search criteria") @Valid TestCaseSearchRequest searchRequest,
            @PageableDefault(size = 20) Pageable pageable) {
        
        log.debug("Getting testcases with search: {} and pageable: {}", searchRequest, pageable);
        
        Page<TestCase> testCasePage = testCaseService.searchTestCases(searchRequest, pageable);
        TestCaseListResponse response = TestCaseListResponse.builder()
                .testCases(testCasePage.getContent().stream()
                        .map(TestCaseDto::fromEntity)
                        .toList())
                .totalElements(testCasePage.getTotalElements())
                .totalPages(testCasePage.getTotalPages())
                .currentPage(testCasePage.getNumber())
                .pageSize(testCasePage.getSize())
                .hasNext(testCasePage.hasNext())
                .hasPrevious(testCasePage.hasPrevious())
                .build();
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get specific testcase by ID.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get testcase", description = "Get specific testcase by ID")
    public ResponseEntity<TestCaseDto> getTestCase(
            @Parameter(description = "TestCase ID") @PathVariable Long id) {
        
        log.debug("Getting testcase with id: {}", id);
        
        Optional<TestCase> testCase = testCaseService.findById(id);
        return testCase.map(tc -> ResponseEntity.ok(TestCaseDto.fromEntity(tc)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Create new testcase.
     */
    @PostMapping
    @Operation(summary = "Create testcase", description = "Create a new testcase")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<TestCaseDto> createTestCase(
            @Parameter(description = "TestCase data") @Valid @RequestBody TestCaseDto testCaseDto) {
        
        log.info("Creating new testcase: {}", testCaseDto.getCrashType());
        
        TestCase testCase = testCaseDto.toEntity();
        TestCase savedTestCase = testCaseService.save(testCase);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(TestCaseDto.fromEntity(savedTestCase));
    }

    /**
     * Update existing testcase.
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update testcase", description = "Update an existing testcase")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<TestCaseDto> updateTestCase(
            @Parameter(description = "TestCase ID") @PathVariable Long id,
            @Parameter(description = "Updated TestCase data") @Valid @RequestBody TestCaseDto testCaseDto) {
        
        log.info("Updating testcase with id: {}", id);
        
        Optional<TestCase> existingTestCase = testCaseService.findById(id);
        if (existingTestCase.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        TestCase testCase = testCaseDto.toEntity();
        testCase.setId(id);
        TestCase updatedTestCase = testCaseService.save(testCase);
        
        return ResponseEntity.ok(TestCaseDto.fromEntity(updatedTestCase));
    }

    /**
     * Delete testcase.
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete testcase", description = "Delete a testcase")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteTestCase(
            @Parameter(description = "TestCase ID") @PathVariable Long id) {
        
        log.info("Deleting testcase with id: {}", id);
        
        if (!testCaseService.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        
        testCaseService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Upload testcase file.
     */
    @PostMapping("/upload")
    @Operation(summary = "Upload testcase", description = "Upload a testcase file")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<TestCaseDto> uploadTestCase(
            @Parameter(description = "Testcase file") @RequestParam("file") MultipartFile file,
            @Parameter(description = "Job type") @RequestParam("jobType") String jobType,
            @Parameter(description = "Fuzzer name") @RequestParam(value = "fuzzerName", required = false) String fuzzerName,
            @Parameter(description = "Platform") @RequestParam(value = "platform", required = false) String platform) {
        
        log.info("Uploading testcase file: {} for job: {}", file.getOriginalFilename(), jobType);
        
        try {
            TestCase testCase = testCaseService.uploadTestCase(file, jobType, fuzzerName, platform);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(TestCaseDto.fromEntity(testCase));
        } catch (Exception e) {
            log.error("Error uploading testcase", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Download testcase file.
     */
    @GetMapping("/{id}/download")
    @Operation(summary = "Download testcase", description = "Download testcase file")
    public ResponseEntity<byte[]> downloadTestCase(
            @Parameter(description = "TestCase ID") @PathVariable Long id) {
        
        log.debug("Downloading testcase with id: {}", id);
        
        try {
            byte[] fileData = testCaseService.downloadTestCase(id);
            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=testcase_" + id + ".txt")
                    .body(fileData);
        } catch (Exception e) {
            log.error("Error downloading testcase", e);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Minimize testcase.
     */
    @PostMapping("/{id}/minimize")
    @Operation(summary = "Minimize testcase", description = "Start testcase minimization process")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<Void> minimizeTestCase(
            @Parameter(description = "TestCase ID") @PathVariable Long id) {
        
        log.info("Starting minimization for testcase: {}", id);
        
        try {
            testCaseService.startMinimization(id);
            return ResponseEntity.accepted().build();
        } catch (Exception e) {
            log.error("Error starting minimization", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Reproduce testcase crash.
     */
    @PostMapping("/{id}/reproduce")
    @Operation(summary = "Reproduce crash", description = "Start crash reproduction process")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<Void> reproduceTestCase(
            @Parameter(description = "TestCase ID") @PathVariable Long id) {
        
        log.info("Starting reproduction for testcase: {}", id);
        
        try {
            testCaseService.startReproduction(id);
            return ResponseEntity.accepted().build();
        } catch (Exception e) {
            log.error("Error starting reproduction", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Create issue tracker entry for testcase.
     */
    @PostMapping("/{id}/create-issue")
    @Operation(summary = "Create issue", description = "Create issue tracker entry for testcase")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<Void> createIssue(
            @Parameter(description = "TestCase ID") @PathVariable Long id,
            @Parameter(description = "Issue tracker type") @RequestParam("tracker") String tracker) {
        
        log.info("Creating issue for testcase: {} in tracker: {}", id, tracker);
        
        try {
            testCaseService.createIssue(id, tracker);
            return ResponseEntity.accepted().build();
        } catch (Exception e) {
            log.error("Error creating issue", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Mark testcase as fixed.
     */
    @PostMapping("/{id}/mark-fixed")
    @Operation(summary = "Mark fixed", description = "Mark testcase as fixed")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<TestCaseDto> markFixed(
            @Parameter(description = "TestCase ID") @PathVariable Long id,
            @Parameter(description = "Fixed revision") @RequestParam(value = "revision", required = false) String revision) {
        
        log.info("Marking testcase {} as fixed at revision: {}", id, revision);
        
        try {
            TestCase updatedTestCase = testCaseService.markAsFixed(id, revision);
            return ResponseEntity.ok(TestCaseDto.fromEntity(updatedTestCase));
        } catch (Exception e) {
            log.error("Error marking testcase as fixed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Mark testcase as security issue.
     */
    @PostMapping("/{id}/mark-security")
    @Operation(summary = "Mark security", description = "Mark testcase as security issue")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TestCaseDto> markSecurity(
            @Parameter(description = "TestCase ID") @PathVariable Long id,
            @Parameter(description = "Security flag") @RequestParam("securityFlag") Boolean securityFlag) {
        
        log.info("Marking testcase {} as security: {}", id, securityFlag);
        
        try {
            TestCase updatedTestCase = testCaseService.markAsSecurity(id, securityFlag);
            return ResponseEntity.ok(TestCaseDto.fromEntity(updatedTestCase));
        } catch (Exception e) {
            log.error("Error marking testcase as security", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get testcase variants.
     */
    @GetMapping("/{id}/variants")
    @Operation(summary = "Get variants", description = "Get testcase variants")
    public ResponseEntity<List<TestCaseDto>> getTestCaseVariants(
            @Parameter(description = "TestCase ID") @PathVariable Long id) {
        
        log.debug("Getting variants for testcase: {}", id);
        
        try {
            List<TestCase> variants = testCaseService.getVariants(id);
            List<TestCaseDto> variantDtos = variants.stream()
                    .map(TestCaseDto::fromEntity)
                    .toList();
            return ResponseEntity.ok(variantDtos);
        } catch (Exception e) {
            log.error("Error getting testcase variants", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Redo testcase analysis.
     */
    @PostMapping("/{id}/redo")
    @Operation(summary = "Redo analysis", description = "Redo testcase analysis")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<Void> redoAnalysis(
            @Parameter(description = "TestCase ID") @PathVariable Long id) {
        
        log.info("Redoing analysis for testcase: {}", id);
        
        try {
            testCaseService.redoAnalysis(id);
            return ResponseEntity.accepted().build();
        } catch (Exception e) {
            log.error("Error redoing analysis", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}