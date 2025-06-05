package com.google.clusterfuzz.core.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Metadata needed for multiple test cases uploaded in an archive.
 * Tracks batch upload processing and configuration.
 */
@Entity
@Table(name = "bundled_archive_metadata", indexes = {
    @Index(name = "idx_archive_metadata_blobstore_key", columnList = "blobstoreKey"),
    @Index(name = "idx_archive_metadata_job_type", columnList = "jobType"),
    @Index(name = "idx_archive_metadata_job_queue", columnList = "jobQueue"),
    @Index(name = "idx_archive_metadata_uploader", columnList = "uploaderEmail"),
    @Index(name = "idx_archive_metadata_status", columnList = "processingStatus"),
    @Index(name = "idx_archive_metadata_created_at", columnList = "createdAt"),
    @Index(name = "idx_archive_metadata_http_flag", columnList = "httpFlag")
})
@EntityListeners(AuditingEntityListener.class)
public class BundledArchiveMetadata {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Blobstore key of the archive.
     */
    @NotNull
    @Size(max = 500)
    @Column(name = "blobstore_key", nullable = false, length = 500)
    private String blobstoreKey;

    /**
     * Timeout in seconds for each testcase in the bundle.
     */
    @Min(1)
    @Column(name = "timeout")
    private Integer timeout;

    /**
     * Job queue for the analyze tasks created for this bundle.
     */
    @Size(max = 255)
    @Column(name = "job_queue", length = 255)
    private String jobQueue;

    /**
     * Job type that should be used for all testcases in this bundle.
     */
    @Size(max = 255)
    @Column(name = "job_type", length = 255)
    private String jobType;

    /**
     * Flag indicating whether or not these testcases need HTTP.
     */
    @Column(name = "http_flag")
    private Boolean httpFlag = false;

    /**
     * File name of the uploaded archive.
     */
    @Size(max = 500)
    @Column(name = "archive_filename", length = 500)
    private String archiveFilename;

    /**
     * Email address of the uploader of the archive.
     */
    @Email
    @Size(max = 255)
    @Column(name = "uploader_email", length = 255)
    private String uploaderEmail;

    /**
     * Processing status of the archive.
     * Values: PENDING, EXTRACTING, PROCESSING, COMPLETED, FAILED
     */
    @Size(max = 50)
    @Column(name = "processing_status", length = 50)
    private String processingStatus = "PENDING";

    /**
     * Number of testcases extracted from the archive.
     */
    @Column(name = "extracted_count")
    private Integer extractedCount = 0;

    /**
     * Number of testcases successfully processed.
     */
    @Column(name = "processed_count")
    private Integer processedCount = 0;

    /**
     * Number of testcases that failed processing.
     */
    @Column(name = "failed_count")
    private Integer failedCount = 0;

    /**
     * Size of the archive in bytes.
     */
    @Column(name = "archive_size")
    private Long archiveSize;

    /**
     * Archive format/type (zip, tar, etc.).
     */
    @Size(max = 50)
    @Column(name = "archive_format", length = 50)
    private String archiveFormat;

    /**
     * Error message if processing failed.
     */
    @Lob
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    /**
     * Processing logs.
     */
    @Lob
    @Column(name = "processing_logs", columnDefinition = "TEXT")
    private String processingLogs;

    /**
     * Platform for processing testcases.
     */
    @Size(max = 100)
    @Column(name = "platform", length = 100)
    private String platform;

    /**
     * Additional configuration as JSON.
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

    /**
     * Processing start time.
     */
    @Column(name = "processing_started_at")
    private LocalDateTime processingStartedAt;

    /**
     * Processing completion time.
     */
    @Column(name = "processing_completed_at")
    private LocalDateTime processingCompletedAt;

    // Status constants
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_EXTRACTING = "EXTRACTING";
    public static final String STATUS_PROCESSING = "PROCESSING";
    public static final String STATUS_COMPLETED = "COMPLETED";
    public static final String STATUS_FAILED = "FAILED";

    // Constructors
    public BundledArchiveMetadata() {}

    public BundledArchiveMetadata(String blobstoreKey, String archiveFilename, String uploaderEmail) {
        this.blobstoreKey = blobstoreKey;
        this.archiveFilename = archiveFilename;
        this.uploaderEmail = uploaderEmail;
        this.processingStatus = STATUS_PENDING;
        this.httpFlag = false;
        this.extractedCount = 0;
        this.processedCount = 0;
        this.failedCount = 0;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBlobstoreKey() {
        return blobstoreKey;
    }

    public void setBlobstoreKey(String blobstoreKey) {
        this.blobstoreKey = blobstoreKey;
    }

    public Integer getTimeout() {
        return timeout;
    }

    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }

    public String getJobQueue() {
        return jobQueue;
    }

    public void setJobQueue(String jobQueue) {
        this.jobQueue = jobQueue;
    }

    public String getJobType() {
        return jobType;
    }

    public void setJobType(String jobType) {
        this.jobType = jobType;
    }

    public Boolean getHttpFlag() {
        return httpFlag;
    }

    public void setHttpFlag(Boolean httpFlag) {
        this.httpFlag = httpFlag;
    }

    public String getArchiveFilename() {
        return archiveFilename;
    }

    public void setArchiveFilename(String archiveFilename) {
        this.archiveFilename = archiveFilename;
    }

    public String getUploaderEmail() {
        return uploaderEmail;
    }

    public void setUploaderEmail(String uploaderEmail) {
        this.uploaderEmail = uploaderEmail;
    }

    public String getProcessingStatus() {
        return processingStatus;
    }

    public void setProcessingStatus(String processingStatus) {
        this.processingStatus = processingStatus;
    }

    public Integer getExtractedCount() {
        return extractedCount;
    }

    public void setExtractedCount(Integer extractedCount) {
        this.extractedCount = extractedCount;
    }

    public Integer getProcessedCount() {
        return processedCount;
    }

    public void setProcessedCount(Integer processedCount) {
        this.processedCount = processedCount;
    }

    public Integer getFailedCount() {
        return failedCount;
    }

    public void setFailedCount(Integer failedCount) {
        this.failedCount = failedCount;
    }

    public Long getArchiveSize() {
        return archiveSize;
    }

    public void setArchiveSize(Long archiveSize) {
        this.archiveSize = archiveSize;
    }

    public String getArchiveFormat() {
        return archiveFormat;
    }

    public void setArchiveFormat(String archiveFormat) {
        this.archiveFormat = archiveFormat;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getProcessingLogs() {
        return processingLogs;
    }

    public void setProcessingLogs(String processingLogs) {
        this.processingLogs = processingLogs;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
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

    public LocalDateTime getProcessingStartedAt() {
        return processingStartedAt;
    }

    public void setProcessingStartedAt(LocalDateTime processingStartedAt) {
        this.processingStartedAt = processingStartedAt;
    }

    public LocalDateTime getProcessingCompletedAt() {
        return processingCompletedAt;
    }

    public void setProcessingCompletedAt(LocalDateTime processingCompletedAt) {
        this.processingCompletedAt = processingCompletedAt;
    }

    // Business logic methods
    /**
     * Start processing the archive.
     */
    public void startProcessing() {
        this.processingStatus = STATUS_EXTRACTING;
        this.processingStartedAt = LocalDateTime.now();
        this.errorMessage = null;
    }

    /**
     * Mark as extracting testcases.
     */
    public void startExtracting() {
        this.processingStatus = STATUS_EXTRACTING;
    }

    /**
     * Mark as processing testcases.
     */
    public void startProcessingTestcases() {
        this.processingStatus = STATUS_PROCESSING;
    }

    /**
     * Complete processing successfully.
     */
    public void completeProcessing() {
        this.processingStatus = STATUS_COMPLETED;
        this.processingCompletedAt = LocalDateTime.now();
        this.errorMessage = null;
    }

    /**
     * Fail processing with error.
     */
    public void failProcessing(String errorMessage) {
        this.processingStatus = STATUS_FAILED;
        this.processingCompletedAt = LocalDateTime.now();
        this.errorMessage = errorMessage;
    }

    /**
     * Increment extracted count.
     */
    public void incrementExtractedCount() {
        this.extractedCount++;
    }

    /**
     * Increment processed count.
     */
    public void incrementProcessedCount() {
        this.processedCount++;
    }

    /**
     * Increment failed count.
     */
    public void incrementFailedCount() {
        this.failedCount++;
    }

    /**
     * Check if processing is pending.
     */
    public boolean isPending() {
        return STATUS_PENDING.equals(processingStatus);
    }

    /**
     * Check if currently extracting.
     */
    public boolean isExtracting() {
        return STATUS_EXTRACTING.equals(processingStatus);
    }

    /**
     * Check if currently processing.
     */
    public boolean isProcessing() {
        return STATUS_PROCESSING.equals(processingStatus);
    }

    /**
     * Check if processing is completed.
     */
    public boolean isCompleted() {
        return STATUS_COMPLETED.equals(processingStatus);
    }

    /**
     * Check if processing failed.
     */
    public boolean isFailed() {
        return STATUS_FAILED.equals(processingStatus);
    }

    /**
     * Check if processing is in progress.
     */
    public boolean isInProgress() {
        return isExtracting() || isProcessing();
    }

    /**
     * Check if processing is finished (completed or failed).
     */
    public boolean isFinished() {
        return isCompleted() || isFailed();
    }

    /**
     * Get processing progress percentage.
     */
    public double getProgressPercentage() {
        if (extractedCount == null || extractedCount == 0) {
            return 0.0;
        }
        
        int totalProcessed = (processedCount != null ? processedCount : 0) + 
                           (failedCount != null ? failedCount : 0);
        
        return (totalProcessed * 100.0) / extractedCount;
    }

    /**
     * Get success rate percentage.
     */
    public double getSuccessRate() {
        int totalProcessed = (processedCount != null ? processedCount : 0) + 
                           (failedCount != null ? failedCount : 0);
        
        if (totalProcessed == 0) {
            return 0.0;
        }
        
        return ((processedCount != null ? processedCount : 0) * 100.0) / totalProcessed;
    }

    /**
     * Get archive size formatted.
     */
    public String getArchiveSizeFormatted() {
        if (archiveSize == null) return "Unknown";
        return formatBytes(archiveSize);
    }

    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " bytes";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024));
        return String.format("%.1f GB", bytes / (1024.0 * 1024 * 1024));
    }

    /**
     * Get processing duration in seconds.
     */
    public Long getProcessingDurationSeconds() {
        if (processingStartedAt == null) return null;
        
        LocalDateTime endTime = processingCompletedAt != null ? 
                               processingCompletedAt : LocalDateTime.now();
        
        return java.time.Duration.between(processingStartedAt, endTime).getSeconds();
    }

    /**
     * Get status display name.
     */
    public String getStatusDisplayName() {
        switch (processingStatus) {
            case STATUS_PENDING: return "Pending";
            case STATUS_EXTRACTING: return "Extracting";
            case STATUS_PROCESSING: return "Processing";
            case STATUS_COMPLETED: return "Completed";
            case STATUS_FAILED: return "Failed";
            default: return processingStatus;
        }
    }

    /**
     * Add to processing logs.
     */
    public void addToLogs(String logEntry) {
        if (processingLogs == null) {
            processingLogs = "";
        }
        processingLogs += LocalDateTime.now() + ": " + logEntry + "\n";
    }

    @Override
    public String toString() {
        return "BundledArchiveMetadata{" +
                "id=" + id +
                ", archiveFilename='" + archiveFilename + '\'' +
                ", processingStatus='" + processingStatus + '\'' +
                ", extractedCount=" + extractedCount +
                ", processedCount=" + processedCount +
                ", failedCount=" + failedCount +
                ", uploaderEmail='" + uploaderEmail + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BundledArchiveMetadata)) return false;
        BundledArchiveMetadata that = (BundledArchiveMetadata) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}