package com.google.clusterfuzz.core.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Metadata associated with a user uploaded test case.
 * Tracks upload status, processing information, and associated testcase details.
 */
@Entity
@Table(name = "testcase_upload_metadata", indexes = {
    @Index(name = "idx_upload_metadata_timestamp", columnList = "timestamp"),
    @Index(name = "idx_upload_metadata_status", columnList = "status"),
    @Index(name = "idx_upload_metadata_uploader", columnList = "uploaderEmail"),
    @Index(name = "idx_upload_metadata_testcase_id", columnList = "testcaseId"),
    @Index(name = "idx_upload_metadata_bot_name", columnList = "botName"),
    @Index(name = "idx_upload_metadata_duplicate_of", columnList = "duplicateOf")
})
@EntityListeners(AuditingEntityListener.class)
public class TestcaseUploadMetadata {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Upload timestamp.
     */
    @CreatedDate
    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    /**
     * Original filename of the uploaded testcase.
     */
    @Size(max = 500)
    @Column(name = "filename", length = 500)
    private String filename;

    /**
     * Current processing status of the testcase.
     * Values: PENDING, PROCESSING, PROCESSED, FAILED, DUPLICATE
     */
    @NotNull
    @Size(max = 50)
    @Column(name = "status", nullable = false, length = 50)
    private String status = "PENDING";

    /**
     * Email address of the uploader.
     */
    @Email
    @Size(max = 255)
    @Column(name = "uploader_email", length = 255)
    private String uploaderEmail;

    /**
     * Name of the bot that processed this testcase.
     */
    @Size(max = 255)
    @Column(name = "bot_name", length = 255)
    private String botName;

    /**
     * ID of the associated processed testcase.
     */
    @Column(name = "testcase_id")
    private Long testcaseId;

    /**
     * ID of the testcase that this is marked as a duplicate of.
     */
    @Column(name = "duplicate_of")
    private Long duplicateOf;

    /**
     * Storage key for the uploaded testcase file.
     */
    @Size(max = 500)
    @Column(name = "blobstore_key", length = 500)
    private String blobstoreKey;

    /**
     * Testcase timeout in seconds.
     */
    @Column(name = "timeout")
    private Integer timeout;

    /**
     * Job type for processing this testcase.
     */
    @Size(max = 255)
    @Column(name = "job_type", length = 255)
    private String jobType;

    /**
     * Platform for processing this testcase.
     */
    @Size(max = 100)
    @Column(name = "platform", length = 100)
    private String platform;

    /**
     * Additional metadata as JSON.
     */
    @Lob
    @Column(name = "additional_metadata", columnDefinition = "TEXT")
    private String additionalMetadata;

    /**
     * Error message if processing failed.
     */
    @Lob
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    /**
     * Size of the uploaded file in bytes.
     */
    @Column(name = "file_size")
    private Long fileSize;

    /**
     * MIME type of the uploaded file.
     */
    @Size(max = 100)
    @Column(name = "content_type", length = 100)
    private String contentType;

    /**
     * Last modification timestamp.
     */
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Constructors
    public TestcaseUploadMetadata() {}

    public TestcaseUploadMetadata(String filename, String uploaderEmail) {
        this.filename = filename;
        this.uploaderEmail = uploaderEmail;
        this.status = "PENDING";
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUploaderEmail() {
        return uploaderEmail;
    }

    public void setUploaderEmail(String uploaderEmail) {
        this.uploaderEmail = uploaderEmail;
    }

    public String getBotName() {
        return botName;
    }

    public void setBotName(String botName) {
        this.botName = botName;
    }

    public Long getTestcaseId() {
        return testcaseId;
    }

    public void setTestcaseId(Long testcaseId) {
        this.testcaseId = testcaseId;
    }

    public Long getDuplicateOf() {
        return duplicateOf;
    }

    public void setDuplicateOf(Long duplicateOf) {
        this.duplicateOf = duplicateOf;
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

    public String getJobType() {
        return jobType;
    }

    public void setJobType(String jobType) {
        this.jobType = jobType;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getAdditionalMetadata() {
        return additionalMetadata;
    }

    public void setAdditionalMetadata(String additionalMetadata) {
        this.additionalMetadata = additionalMetadata;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Status management methods
    public void markAsPending() {
        this.status = "PENDING";
    }

    public void markAsProcessing() {
        this.status = "PROCESSING";
    }

    public void markAsProcessed(Long testcaseId) {
        this.status = "PROCESSED";
        this.testcaseId = testcaseId;
        this.errorMessage = null;
    }

    public void markAsFailed(String errorMessage) {
        this.status = "FAILED";
        this.errorMessage = errorMessage;
    }

    public void markAsDuplicate(Long duplicateOfId) {
        this.status = "DUPLICATE";
        this.duplicateOf = duplicateOfId;
    }

    // Status check methods
    public boolean isPending() {
        return "PENDING".equals(status);
    }

    public boolean isProcessing() {
        return "PROCESSING".equals(status);
    }

    public boolean isProcessed() {
        return "PROCESSED".equals(status);
    }

    public boolean isFailed() {
        return "FAILED".equals(status);
    }

    public boolean isDuplicate() {
        return "DUPLICATE".equals(status);
    }

    public boolean isCompleted() {
        return isProcessed() || isDuplicate();
    }

    // Utility methods
    public boolean hasTestcase() {
        return testcaseId != null;
    }

    public boolean hasError() {
        return errorMessage != null && !errorMessage.trim().isEmpty();
    }

    public boolean hasBlobstoreKey() {
        return blobstoreKey != null && !blobstoreKey.trim().isEmpty();
    }

    public String getFileSizeFormatted() {
        if (fileSize == null) return "Unknown";
        return formatBytes(fileSize);
    }

    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " bytes";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024));
        return String.format("%.1f GB", bytes / (1024.0 * 1024 * 1024));
    }

    public String getStatusDisplayName() {
        switch (status) {
            case "PENDING": return "Pending Analysis";
            case "PROCESSING": return "Processing";
            case "PROCESSED": return "Processed";
            case "FAILED": return "Failed";
            case "DUPLICATE": return "Duplicate";
            default: return status;
        }
    }

    @Override
    public String toString() {
        return "TestcaseUploadMetadata{" +
                "id=" + id +
                ", filename='" + filename + '\'' +
                ", status='" + status + '\'' +
                ", uploaderEmail='" + uploaderEmail + '\'' +
                ", testcaseId=" + testcaseId +
                ", timestamp=" + timestamp +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TestcaseUploadMetadata)) return false;
        TestcaseUploadMetadata that = (TestcaseUploadMetadata) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}