package com.google.clusterfuzz.core.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Metadata information for issues that were filed automatically.
 * Tracks bug filing information and associated testcase details.
 */
@Entity
@Table(name = "filed_bug", indexes = {
    @Index(name = "idx_filed_bug_timestamp", columnList = "timestamp"),
    @Index(name = "idx_filed_bug_testcase_id", columnList = "testcaseId"),
    @Index(name = "idx_filed_bug_group_id", columnList = "groupId"),
    @Index(name = "idx_filed_bug_crash_type", columnList = "crashType"),
    @Index(name = "idx_filed_bug_security_flag", columnList = "securityFlag"),
    @Index(name = "idx_filed_bug_platform_id", columnList = "platformId"),
    @Index(name = "idx_filed_bug_project_name", columnList = "projectName"),
    @Index(name = "idx_filed_bug_job_type", columnList = "jobType"),
    @Index(name = "idx_filed_bug_bug_information", columnList = "bugInformation")
})
@EntityListeners(AuditingEntityListener.class)
public class FiledBug {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Timestamp when the issue was filed.
     */
    @CreatedDate
    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    /**
     * ID of the test case that is associated with the filed issue.
     */
    @NotNull
    @Column(name = "testcase_id", nullable = false)
    private Long testcaseId;

    /**
     * Tracking issue tracker bug ID for this testcase.
     */
    @Column(name = "bug_information", nullable = false)
    private Integer bugInformation = 0;

    /**
     * Group ID associated with this issue.
     */
    @Column(name = "group_id")
    private Long groupId;

    /**
     * Crash type for easy reference.
     */
    @Size(max = 255)
    @Column(name = "crash_type", length = 255)
    private String crashType;

    /**
     * Crash state for easy reference.
     */
    @Size(max = 500)
    @Column(name = "crash_state", length = 500)
    private String crashState;

    /**
     * Is it a security bug?
     */
    @Column(name = "security_flag")
    private Boolean securityFlag;

    /**
     * Platform identifier.
     */
    @Size(max = 100)
    @Column(name = "platform_id", length = 100)
    private String platformId;

    /**
     * Project name that is associated with the filed issue.
     */
    @Size(max = 255)
    @Column(name = "project_name", length = 255)
    private String projectName;

    /**
     * Job type that is associated with the filed issue.
     */
    @Size(max = 255)
    @Column(name = "job_type", length = 255)
    private String jobType;

    /**
     * Issue tracker type (e.g., "jira", "github", "monorail").
     */
    @Size(max = 50)
    @Column(name = "issue_tracker_type", length = 50)
    private String issueTrackerType;

    /**
     * External issue URL or identifier.
     */
    @Size(max = 1000)
    @Column(name = "external_issue_url", length = 1000)
    private String externalIssueUrl;

    /**
     * Issue status in the external tracker.
     */
    @Size(max = 50)
    @Column(name = "issue_status", length = 50)
    private String issueStatus;

    /**
     * Issue priority in the external tracker.
     */
    @Size(max = 50)
    @Column(name = "issue_priority", length = 50)
    private String issuePriority;

    /**
     * Last modification timestamp.
     */
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Constructors
    public FiledBug() {}

    public FiledBug(Long testcaseId, Integer bugInformation) {
        this.testcaseId = testcaseId;
        this.bugInformation = bugInformation;
    }

    public FiledBug(Long testcaseId, Integer bugInformation, String crashType, String crashState) {
        this(testcaseId, bugInformation);
        this.crashType = crashType;
        this.crashState = crashState;
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

    public Long getTestcaseId() {
        return testcaseId;
    }

    public void setTestcaseId(Long testcaseId) {
        this.testcaseId = testcaseId;
    }

    public Integer getBugInformation() {
        return bugInformation;
    }

    public void setBugInformation(Integer bugInformation) {
        this.bugInformation = bugInformation;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public String getCrashType() {
        return crashType;
    }

    public void setCrashType(String crashType) {
        this.crashType = crashType;
    }

    public String getCrashState() {
        return crashState;
    }

    public void setCrashState(String crashState) {
        this.crashState = crashState;
    }

    public Boolean getSecurityFlag() {
        return securityFlag;
    }

    public void setSecurityFlag(Boolean securityFlag) {
        this.securityFlag = securityFlag;
    }

    public String getPlatformId() {
        return platformId;
    }

    public void setPlatformId(String platformId) {
        this.platformId = platformId;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getJobType() {
        return jobType;
    }

    public void setJobType(String jobType) {
        this.jobType = jobType;
    }

    public String getIssueTrackerType() {
        return issueTrackerType;
    }

    public void setIssueTrackerType(String issueTrackerType) {
        this.issueTrackerType = issueTrackerType;
    }

    public String getExternalIssueUrl() {
        return externalIssueUrl;
    }

    public void setExternalIssueUrl(String externalIssueUrl) {
        this.externalIssueUrl = externalIssueUrl;
    }

    public String getIssueStatus() {
        return issueStatus;
    }

    public void setIssueStatus(String issueStatus) {
        this.issueStatus = issueStatus;
    }

    public String getIssuePriority() {
        return issuePriority;
    }

    public void setIssuePriority(String issuePriority) {
        this.issuePriority = issuePriority;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Business logic methods
    /**
     * Check if this is a security bug.
     */
    public boolean isSecurityBug() {
        return Boolean.TRUE.equals(securityFlag);
    }

    /**
     * Check if this bug has been filed (has bug information).
     */
    public boolean isFiled() {
        return bugInformation != null && bugInformation > 0;
    }

    /**
     * Check if this bug is part of a group.
     */
    public boolean isGrouped() {
        return groupId != null;
    }

    /**
     * Check if this bug has external tracking information.
     */
    public boolean hasExternalTracking() {
        return externalIssueUrl != null && !externalIssueUrl.trim().isEmpty();
    }

    /**
     * Get the bug display identifier.
     */
    public String getBugDisplayId() {
        if (bugInformation != null && bugInformation > 0) {
            return String.valueOf(bugInformation);
        }
        return "Not Filed";
    }

    /**
     * Get the security classification display.
     */
    public String getSecurityClassification() {
        return isSecurityBug() ? "Security" : "Functional";
    }

    /**
     * Check if the issue is in an open state.
     */
    public boolean isOpen() {
        if (issueStatus == null) return true;
        String status = issueStatus.toLowerCase();
        return !status.contains("closed") && 
               !status.contains("resolved") && 
               !status.contains("fixed") &&
               !status.contains("done");
    }

    /**
     * Check if the issue is closed.
     */
    public boolean isClosed() {
        return !isOpen();
    }

    /**
     * Get a summary of the filed bug.
     */
    public String getBugSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("Bug ").append(getBugDisplayId());
        
        if (crashType != null) {
            summary.append(" - ").append(crashType);
        }
        
        if (isSecurityBug()) {
            summary.append(" [SECURITY]");
        }
        
        if (projectName != null) {
            summary.append(" (").append(projectName).append(")");
        }
        
        return summary.toString();
    }

    /**
     * Update issue status from external tracker.
     */
    public void updateIssueStatus(String status, String priority) {
        this.issueStatus = status;
        this.issuePriority = priority;
    }

    /**
     * Mark as security bug.
     */
    public void markAsSecurityBug() {
        this.securityFlag = true;
    }

    /**
     * Mark as functional bug.
     */
    public void markAsFunctionalBug() {
        this.securityFlag = false;
    }

    @Override
    public String toString() {
        return "FiledBug{" +
                "id=" + id +
                ", testcaseId=" + testcaseId +
                ", bugInformation=" + bugInformation +
                ", crashType='" + crashType + '\'' +
                ", securityFlag=" + securityFlag +
                ", projectName='" + projectName + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FiledBug)) return false;
        FiledBug filedBug = (FiledBug) o;
        return id != null && id.equals(filedBug.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}