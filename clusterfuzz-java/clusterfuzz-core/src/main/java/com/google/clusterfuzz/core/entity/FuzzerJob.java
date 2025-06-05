package com.google.clusterfuzz.core.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Mapping between a fuzzer and job with additional metadata for selection.
 * Controls fuzzer-job assignment weights and platform-specific configurations.
 */
@Entity
@Table(name = "fuzzer_job", 
    indexes = {
        @Index(name = "idx_fuzzer_job_fuzzer", columnList = "fuzzer"),
        @Index(name = "idx_fuzzer_job_job", columnList = "job"),
        @Index(name = "idx_fuzzer_job_platform", columnList = "platform"),
        @Index(name = "idx_fuzzer_job_fuzzer_job", columnList = "fuzzer, job"),
        @Index(name = "idx_fuzzer_job_weight", columnList = "weight"),
        @Index(name = "idx_fuzzer_job_actual_weight", columnList = "actualWeight")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_fuzzer_job_platform", columnNames = {"fuzzer", "job", "platform"})
    }
)
@EntityListeners(AuditingEntityListener.class)
public class FuzzerJob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Name of the fuzzer.
     */
    @NotNull
    @Column(name = "fuzzer", nullable = false, length = 255)
    private String fuzzer;

    /**
     * Name of the job.
     */
    @NotNull
    @Column(name = "job", nullable = false, length = 255)
    private String job;

    /**
     * Platform this mapping applies to.
     */
    @Column(name = "platform", length = 100)
    private String platform;

    /**
     * Base weight for fuzzer-job selection.
     */
    @DecimalMin(value = "0.0", inclusive = true)
    @Column(name = "weight", nullable = false, precision = 10, scale = 2)
    private Double weight = 1.0;

    /**
     * Multiplier applied to the base weight.
     */
    @DecimalMin(value = "0.0", inclusive = true)
    @Column(name = "multiplier", nullable = false, precision = 10, scale = 2)
    private Double multiplier = 1.0;

    /**
     * Computed actual weight (weight * multiplier).
     * Updated automatically when weight or multiplier changes.
     */
    @Column(name = "actual_weight", nullable = false, precision = 10, scale = 2)
    private Double actualWeight = 1.0;

    /**
     * Whether this fuzzer-job mapping is currently enabled.
     */
    @Column(name = "enabled", nullable = false)
    private Boolean enabled = true;

    /**
     * Additional configuration parameters as JSON.
     */
    @Lob
    @Column(name = "configuration", columnDefinition = "TEXT")
    private String configuration;

    /**
     * Creation timestamp.
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * Last modification timestamp.
     */
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Constructors
    public FuzzerJob() {}

    public FuzzerJob(String fuzzer, String job) {
        this.fuzzer = fuzzer;
        this.job = job;
        this.weight = 1.0;
        this.multiplier = 1.0;
        this.actualWeight = 1.0;
        this.enabled = true;
    }

    public FuzzerJob(String fuzzer, String job, String platform) {
        this(fuzzer, job);
        this.platform = platform;
    }

    public FuzzerJob(String fuzzer, String job, String platform, Double weight) {
        this(fuzzer, job, platform);
        this.weight = weight;
        updateActualWeight();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFuzzer() {
        return fuzzer;
    }

    public void setFuzzer(String fuzzer) {
        this.fuzzer = fuzzer;
    }

    public String getJob() {
        return job;
    }

    public void setJob(String job) {
        this.job = job;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
        updateActualWeight();
    }

    public Double getMultiplier() {
        return multiplier;
    }

    public void setMultiplier(Double multiplier) {
        this.multiplier = multiplier;
        updateActualWeight();
    }

    public Double getActualWeight() {
        return actualWeight;
    }

    public void setActualWeight(Double actualWeight) {
        this.actualWeight = actualWeight;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public String getConfiguration() {
        return configuration;
    }

    public void setConfiguration(String configuration) {
        this.configuration = configuration;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Business logic methods
    /**
     * Calculate and update the actual weight based on weight and multiplier.
     */
    public void updateActualWeight() {
        if (weight != null && multiplier != null) {
            this.actualWeight = weight * multiplier;
        } else {
            this.actualWeight = 0.0;
        }
    }

    /**
     * Get the actual weight for this fuzzer-job mapping.
     * This is the weight used for selection algorithms.
     */
    public Double calculateActualWeight() {
        if (!Boolean.TRUE.equals(enabled)) {
            return 0.0;
        }
        return (weight != null ? weight : 1.0) * (multiplier != null ? multiplier : 1.0);
    }

    /**
     * Check if this mapping is active and can be used for fuzzing.
     */
    public boolean isActive() {
        return Boolean.TRUE.equals(enabled) && actualWeight != null && actualWeight > 0;
    }

    /**
     * Enable this fuzzer-job mapping.
     */
    public void enable() {
        this.enabled = true;
    }

    /**
     * Disable this fuzzer-job mapping.
     */
    public void disable() {
        this.enabled = false;
    }

    /**
     * Check if this mapping applies to the specified platform.
     */
    public boolean appliesToPlatform(String targetPlatform) {
        return platform == null || platform.equals(targetPlatform);
    }

    /**
     * Get a unique key for this fuzzer-job-platform combination.
     */
    public String getUniqueKey() {
        return fuzzer + ":" + job + ":" + (platform != null ? platform : "default");
    }

    // JPA lifecycle callbacks
    @PrePersist
    @PreUpdate
    public void updateCalculatedFields() {
        updateActualWeight();
    }

    @Override
    public String toString() {
        return "FuzzerJob{" +
                "id=" + id +
                ", fuzzer='" + fuzzer + '\'' +
                ", job='" + job + '\'' +
                ", platform='" + platform + '\'' +
                ", weight=" + weight +
                ", multiplier=" + multiplier +
                ", actualWeight=" + actualWeight +
                ", enabled=" + enabled +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FuzzerJob)) return false;
        FuzzerJob fuzzerJob = (FuzzerJob) o;
        return id != null && id.equals(fuzzerJob.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}