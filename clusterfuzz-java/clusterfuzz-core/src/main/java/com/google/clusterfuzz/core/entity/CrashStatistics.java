package com.google.clusterfuzz.core.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity for tracking crash statistics and trends.
 * Aggregates crash data for reporting and analysis.
 */
@Entity
@Table(name = "crash_statistics", indexes = {
    @Index(name = "idx_crash_stats_date", columnList = "date"),
    @Index(name = "idx_crash_stats_job", columnList = "jobName"),
    @Index(name = "idx_crash_stats_fuzzer", columnList = "fuzzerName"),
    @Index(name = "idx_crash_stats_platform", columnList = "platform")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class CrashStatistics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Date for these statistics.
     */
    @Column(name = "date", nullable = false)
    @NotNull(message = "Date is required")
    private LocalDate date;

    /**
     * Job name these statistics are for.
     */
    @Column(name = "job_name", nullable = false, length = 100)
    @NotBlank(message = "Job name is required")
    @Size(max = 100, message = "Job name must not exceed 100 characters")
    private String jobName;

    /**
     * Fuzzer name these statistics are for.
     */
    @Column(name = "fuzzer_name", length = 100)
    @Size(max = 100, message = "Fuzzer name must not exceed 100 characters")
    private String fuzzerName;

    /**
     * Platform these statistics are for.
     */
    @Column(name = "platform", length = 50)
    @Size(max = 50, message = "Platform must not exceed 50 characters")
    private String platform;

    /**
     * Total number of crashes found.
     */
    @Column(name = "total_crashes", nullable = false)
    @Builder.Default
    private Long totalCrashes = 0L;

    /**
     * Number of unique crashes (deduplicated).
     */
    @Column(name = "unique_crashes", nullable = false)
    @Builder.Default
    private Long uniqueCrashes = 0L;

    /**
     * Number of security-related crashes.
     */
    @Column(name = "security_crashes", nullable = false)
    @Builder.Default
    private Long securityCrashes = 0L;

    /**
     * Number of crashes with high severity.
     */
    @Column(name = "high_severity_crashes", nullable = false)
    @Builder.Default
    private Long highSeverityCrashes = 0L;

    /**
     * Number of crashes with medium severity.
     */
    @Column(name = "medium_severity_crashes", nullable = false)
    @Builder.Default
    private Long mediumSeverityCrashes = 0L;

    /**
     * Number of crashes with low severity.
     */
    @Column(name = "low_severity_crashes", nullable = false)
    @Builder.Default
    private Long lowSeverityCrashes = 0L;

    /**
     * Number of heap buffer overflow crashes.
     */
    @Column(name = "heap_buffer_overflow_crashes", nullable = false)
    @Builder.Default
    private Long heapBufferOverflowCrashes = 0L;

    /**
     * Number of stack buffer overflow crashes.
     */
    @Column(name = "stack_buffer_overflow_crashes", nullable = false)
    @Builder.Default
    private Long stackBufferOverflowCrashes = 0L;

    /**
     * Number of use-after-free crashes.
     */
    @Column(name = "use_after_free_crashes", nullable = false)
    @Builder.Default
    private Long useAfterFreeCrashes = 0L;

    /**
     * Number of null pointer dereference crashes.
     */
    @Column(name = "null_pointer_crashes", nullable = false)
    @Builder.Default
    private Long nullPointerCrashes = 0L;

    /**
     * Number of assertion failures.
     */
    @Column(name = "assertion_failures", nullable = false)
    @Builder.Default
    private Long assertionFailures = 0L;

    /**
     * Number of timeout crashes.
     */
    @Column(name = "timeout_crashes", nullable = false)
    @Builder.Default
    private Long timeoutCrashes = 0L;

    /**
     * Number of out-of-memory crashes.
     */
    @Column(name = "out_of_memory_crashes", nullable = false)
    @Builder.Default
    private Long outOfMemoryCrashes = 0L;

    /**
     * Total fuzzing executions performed.
     */
    @Column(name = "total_executions", nullable = false)
    @Builder.Default
    private Long totalExecutions = 0L;

    /**
     * Total fuzzing time in seconds.
     */
    @Column(name = "total_fuzzing_time_seconds", nullable = false)
    @Builder.Default
    private Long totalFuzzingTimeSeconds = 0L;

    /**
     * Average executions per second.
     */
    @Column(name = "avg_executions_per_second")
    private Double avgExecutionsPerSecond;

    /**
     * Total coverage achieved.
     */
    @Column(name = "total_coverage")
    private Long totalCoverage;

    /**
     * New coverage discovered.
     */
    @Column(name = "new_coverage")
    private Long newCoverage;

    /**
     * Number of corpus files generated.
     */
    @Column(name = "corpus_files_generated", nullable = false)
    @Builder.Default
    private Long corpusFilesGenerated = 0L;

    /**
     * Total size of corpus in bytes.
     */
    @Column(name = "corpus_size_bytes")
    private Long corpusSizeBytes;

    /**
     * Number of crashes that resulted in filed bugs.
     */
    @Column(name = "bugs_filed", nullable = false)
    @Builder.Default
    private Long bugsFiled = 0L;

    /**
     * Number of crashes that were duplicates.
     */
    @Column(name = "duplicate_crashes", nullable = false)
    @Builder.Default
    private Long duplicateCrashes = 0L;

    /**
     * Number of crashes that were unreproducible.
     */
    @Column(name = "unreproducible_crashes", nullable = false)
    @Builder.Default
    private Long unreproducibleCrashes = 0L;

    /**
     * Number of crashes that were minimized.
     */
    @Column(name = "minimized_crashes", nullable = false)
    @Builder.Default
    private Long minimizedCrashes = 0L;

    /**
     * Average crash minimization ratio.
     */
    @Column(name = "avg_minimization_ratio")
    private Double avgMinimizationRatio;

    /**
     * Number of active bots for this job.
     */
    @Column(name = "active_bots")
    private Integer activeBots;

    /**
     * CPU hours consumed.
     */
    @Column(name = "cpu_hours")
    private Double cpuHours;

    /**
     * Memory usage in GB-hours.
     */
    @Column(name = "memory_gb_hours")
    private Double memoryGbHours;

    /**
     * Additional metrics as JSON.
     */
    @Column(name = "additional_metrics", columnDefinition = "TEXT")
    private String additionalMetrics;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Calculate crash rate (crashes per execution).
     */
    public double getCrashRate() {
        if (totalExecutions == 0) {
            return 0.0;
        }
        return (double) totalCrashes / totalExecutions;
    }

    /**
     * Calculate unique crash rate.
     */
    public double getUniqueCrashRate() {
        if (totalExecutions == 0) {
            return 0.0;
        }
        return (double) uniqueCrashes / totalExecutions;
    }

    /**
     * Calculate security crash percentage.
     */
    public double getSecurityCrashPercentage() {
        if (totalCrashes == 0) {
            return 0.0;
        }
        return (double) securityCrashes / totalCrashes * 100.0;
    }

    /**
     * Calculate bug filing rate.
     */
    public double getBugFilingRate() {
        if (uniqueCrashes == 0) {
            return 0.0;
        }
        return (double) bugsFiled / uniqueCrashes;
    }

    /**
     * Calculate duplicate rate.
     */
    public double getDuplicateRate() {
        if (totalCrashes == 0) {
            return 0.0;
        }
        return (double) duplicateCrashes / totalCrashes;
    }

    /**
     * Calculate efficiency score (unique crashes per CPU hour).
     */
    public double getEfficiencyScore() {
        if (cpuHours == null || cpuHours == 0) {
            return 0.0;
        }
        return (double) uniqueCrashes / cpuHours;
    }

    /**
     * Get the most common crash type.
     */
    public String getMostCommonCrashType() {
        long maxCrashes = 0;
        String mostCommonType = "unknown";

        if (heapBufferOverflowCrashes > maxCrashes) {
            maxCrashes = heapBufferOverflowCrashes;
            mostCommonType = "heap-buffer-overflow";
        }
        if (stackBufferOverflowCrashes > maxCrashes) {
            maxCrashes = stackBufferOverflowCrashes;
            mostCommonType = "stack-buffer-overflow";
        }
        if (useAfterFreeCrashes > maxCrashes) {
            maxCrashes = useAfterFreeCrashes;
            mostCommonType = "use-after-free";
        }
        if (nullPointerCrashes > maxCrashes) {
            maxCrashes = nullPointerCrashes;
            mostCommonType = "null-pointer-dereference";
        }
        if (assertionFailures > maxCrashes) {
            maxCrashes = assertionFailures;
            mostCommonType = "assertion-failure";
        }
        if (timeoutCrashes > maxCrashes) {
            maxCrashes = timeoutCrashes;
            mostCommonType = "timeout";
        }
        if (outOfMemoryCrashes > maxCrashes) {
            maxCrashes = outOfMemoryCrashes;
            mostCommonType = "out-of-memory";
        }

        return mostCommonType;
    }

    /**
     * Add crash statistics from another instance.
     */
    public void addStatistics(CrashStatistics other) {
        this.totalCrashes += other.totalCrashes;
        this.uniqueCrashes += other.uniqueCrashes;
        this.securityCrashes += other.securityCrashes;
        this.highSeverityCrashes += other.highSeverityCrashes;
        this.mediumSeverityCrashes += other.mediumSeverityCrashes;
        this.lowSeverityCrashes += other.lowSeverityCrashes;
        this.heapBufferOverflowCrashes += other.heapBufferOverflowCrashes;
        this.stackBufferOverflowCrashes += other.stackBufferOverflowCrashes;
        this.useAfterFreeCrashes += other.useAfterFreeCrashes;
        this.nullPointerCrashes += other.nullPointerCrashes;
        this.assertionFailures += other.assertionFailures;
        this.timeoutCrashes += other.timeoutCrashes;
        this.outOfMemoryCrashes += other.outOfMemoryCrashes;
        this.totalExecutions += other.totalExecutions;
        this.totalFuzzingTimeSeconds += other.totalFuzzingTimeSeconds;
        this.corpusFilesGenerated += other.corpusFilesGenerated;
        this.bugsFiled += other.bugsFiled;
        this.duplicateCrashes += other.duplicateCrashes;
        this.unreproducibleCrashes += other.unreproducibleCrashes;
        this.minimizedCrashes += other.minimizedCrashes;

        // Recalculate derived fields
        if (totalFuzzingTimeSeconds > 0) {
            this.avgExecutionsPerSecond = (double) totalExecutions / totalFuzzingTimeSeconds;
        }
    }
}