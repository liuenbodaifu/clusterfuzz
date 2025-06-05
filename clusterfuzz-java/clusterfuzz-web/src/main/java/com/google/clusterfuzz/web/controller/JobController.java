package com.google.clusterfuzz.web.controller;

import com.google.clusterfuzz.core.entity.Job;
import com.google.clusterfuzz.core.service.JobService;
import com.google.clusterfuzz.web.dto.JobDto;
import com.google.clusterfuzz.web.dto.JobListResponse;
import com.google.clusterfuzz.web.dto.JobSearchRequest;
import com.google.clusterfuzz.web.dto.JobStatsDto;
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

import java.util.Optional;

/**
 * REST API controller for Job management.
 * Provides endpoints for managing fuzzing jobs, their configuration, and execution.
 */
@RestController
@RequestMapping("/api/v1/jobs")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Job Management", description = "APIs for managing fuzzing jobs")
public class JobController {

    private final JobService jobService;

    /**
     * Get paginated list of jobs with optional filtering.
     */
    @GetMapping
    @Operation(summary = "List jobs", description = "Get paginated list of fuzzing jobs with optional filtering")
    public ResponseEntity<JobListResponse> getJobs(
            @Parameter(description = "Search criteria") @Valid JobSearchRequest searchRequest,
            @PageableDefault(size = 10) Pageable pageable) {
        
        log.debug("Getting jobs with search: {} and pageable: {}", searchRequest, pageable);
        
        Page<Job> jobPage = jobService.searchJobs(searchRequest, pageable);
        JobListResponse response = JobListResponse.builder()
                .jobs(jobPage.getContent().stream()
                        .map(JobDto::fromEntity)
                        .toList())
                .totalElements(jobPage.getTotalElements())
                .totalPages(jobPage.getTotalPages())
                .currentPage(jobPage.getNumber())
                .pageSize(jobPage.getSize())
                .hasNext(jobPage.hasNext())
                .hasPrevious(jobPage.hasPrevious())
                .build();
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get specific job by name.
     */
    @GetMapping("/{name}")
    @Operation(summary = "Get job", description = "Get specific job by name")
    public ResponseEntity<JobDto> getJob(
            @Parameter(description = "Job name") @PathVariable String name) {
        
        log.debug("Getting job with name: {}", name);
        
        Optional<Job> job = jobService.findByName(name);
        return job.map(j -> ResponseEntity.ok(JobDto.fromEntity(j)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Create new job.
     */
    @PostMapping
    @Operation(summary = "Create job", description = "Create a new fuzzing job")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<JobDto> createJob(
            @Parameter(description = "Job data") @Valid @RequestBody JobDto jobDto) {
        
        log.info("Creating new job: {}", jobDto.getName());
        
        // Check if job already exists
        if (jobService.existsByName(jobDto.getName())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        
        Job job = jobDto.toEntity();
        Job savedJob = jobService.save(job);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(JobDto.fromEntity(savedJob));
    }

    /**
     * Update existing job.
     */
    @PutMapping("/{name}")
    @Operation(summary = "Update job", description = "Update an existing job configuration")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<JobDto> updateJob(
            @Parameter(description = "Job name") @PathVariable String name,
            @Parameter(description = "Updated job data") @Valid @RequestBody JobDto jobDto) {
        
        log.info("Updating job with name: {}", name);
        
        Optional<Job> existingJob = jobService.findByName(name);
        if (existingJob.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Job job = jobDto.toEntity();
        job.setName(name); // Ensure name consistency
        Job updatedJob = jobService.save(job);
        
        return ResponseEntity.ok(JobDto.fromEntity(updatedJob));
    }

    /**
     * Delete job.
     */
    @DeleteMapping("/{name}")
    @Operation(summary = "Delete job", description = "Delete a fuzzing job")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteJob(
            @Parameter(description = "Job name") @PathVariable String name) {
        
        log.info("Deleting job with name: {}", name);
        
        if (!jobService.existsByName(name)) {
            return ResponseEntity.notFound().build();
        }
        
        jobService.deleteByName(name);
        return ResponseEntity.noContent().build();
    }

    /**
     * Start job execution.
     */
    @PostMapping("/{name}/start")
    @Operation(summary = "Start job", description = "Start job execution")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<JobDto> startJob(
            @Parameter(description = "Job name") @PathVariable String name) {
        
        log.info("Starting job: {}", name);
        
        try {
            Job job = jobService.startJob(name);
            return ResponseEntity.ok(JobDto.fromEntity(job));
        } catch (IllegalStateException e) {
            log.warn("Cannot start job {}: {}", name, e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (Exception e) {
            log.error("Error starting job: " + name, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Stop job execution.
     */
    @PostMapping("/{name}/stop")
    @Operation(summary = "Stop job", description = "Stop job execution")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<JobDto> stopJob(
            @Parameter(description = "Job name") @PathVariable String name) {
        
        log.info("Stopping job: {}", name);
        
        try {
            Job job = jobService.stopJob(name);
            return ResponseEntity.ok(JobDto.fromEntity(job));
        } catch (IllegalStateException e) {
            log.warn("Cannot stop job {}: {}", name, e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (Exception e) {
            log.error("Error stopping job: " + name, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get job statistics.
     */
    @GetMapping("/{name}/stats")
    @Operation(summary = "Get job stats", description = "Get job execution statistics")
    public ResponseEntity<JobStatsDto> getJobStats(
            @Parameter(description = "Job name") @PathVariable String name,
            @Parameter(description = "Time range in days") @RequestParam(value = "days", defaultValue = "7") int days) {
        
        log.debug("Getting stats for job: {} for {} days", name, days);
        
        try {
            JobStatsDto stats = jobService.getJobStats(name, days);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error getting job stats for: " + name, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Enable/disable job.
     */
    @PostMapping("/{name}/toggle")
    @Operation(summary = "Toggle job", description = "Enable or disable job")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<JobDto> toggleJob(
            @Parameter(description = "Job name") @PathVariable String name,
            @Parameter(description = "Enable flag") @RequestParam("enabled") boolean enabled) {
        
        log.info("Toggling job {} to enabled: {}", name, enabled);
        
        try {
            Job job = jobService.toggleJob(name, enabled);
            return ResponseEntity.ok(JobDto.fromEntity(job));
        } catch (Exception e) {
            log.error("Error toggling job: " + name, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Update job environment variables.
     */
    @PutMapping("/{name}/environment")
    @Operation(summary = "Update environment", description = "Update job environment variables")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<JobDto> updateJobEnvironment(
            @Parameter(description = "Job name") @PathVariable String name,
            @Parameter(description = "Environment variables") @RequestBody String environment) {
        
        log.info("Updating environment for job: {}", name);
        
        try {
            Job job = jobService.updateEnvironment(name, environment);
            return ResponseEntity.ok(JobDto.fromEntity(job));
        } catch (Exception e) {
            log.error("Error updating job environment: " + name, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get job templates.
     */
    @GetMapping("/{name}/templates")
    @Operation(summary = "Get templates", description = "Get job templates")
    public ResponseEntity<String> getJobTemplates(
            @Parameter(description = "Job name") @PathVariable String name) {
        
        log.debug("Getting templates for job: {}", name);
        
        try {
            String templates = jobService.getJobTemplates(name);
            return ResponseEntity.ok(templates);
        } catch (Exception e) {
            log.error("Error getting job templates: " + name, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Update job templates.
     */
    @PutMapping("/{name}/templates")
    @Operation(summary = "Update templates", description = "Update job templates")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<JobDto> updateJobTemplates(
            @Parameter(description = "Job name") @PathVariable String name,
            @Parameter(description = "Job templates") @RequestBody String templates) {
        
        log.info("Updating templates for job: {}", name);
        
        try {
            Job job = jobService.updateTemplates(name, templates);
            return ResponseEntity.ok(JobDto.fromEntity(job));
        } catch (Exception e) {
            log.error("Error updating job templates: " + name, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get job queue information.
     */
    @GetMapping("/{name}/queue")
    @Operation(summary = "Get queue info", description = "Get job queue information")
    public ResponseEntity<Object> getJobQueue(
            @Parameter(description = "Job name") @PathVariable String name) {
        
        log.debug("Getting queue info for job: {}", name);
        
        try {
            Object queueInfo = jobService.getJobQueueInfo(name);
            return ResponseEntity.ok(queueInfo);
        } catch (Exception e) {
            log.error("Error getting job queue info: " + name, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Reset job statistics.
     */
    @PostMapping("/{name}/reset-stats")
    @Operation(summary = "Reset stats", description = "Reset job statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> resetJobStats(
            @Parameter(description = "Job name") @PathVariable String name) {
        
        log.info("Resetting stats for job: {}", name);
        
        try {
            jobService.resetJobStats(name);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error resetting job stats: " + name, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}