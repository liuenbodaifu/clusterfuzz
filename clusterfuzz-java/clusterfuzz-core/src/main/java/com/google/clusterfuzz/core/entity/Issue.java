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
import java.time.LocalDateTime;
import java.util.Set;

/**
 * Entity representing an issue/bug in external issue tracking systems.
 * Links ClusterFuzz testcases to external bug trackers like Monorail, GitHub Issues, etc.
 */
@Entity
@Table(name = "issues", indexes = {
    @Index(name = "idx_issue_external_id", columnList = "externalId"),
    @Index(name = "idx_issue_tracker", columnList = "tracker"),
    @Index(name = "idx_issue_status", columnList = "status"),
    @Index(name = "idx_issue_priority", columnList = "priority"),
    @Index(name = "idx_issue_created_at", columnList = "createdAt")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Issue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * External issue ID in the bug tracking system.
     */
    @Column(name = "external_id", nullable = false, length = 100)
    @NotBlank(message = "External issue ID is required")
    @Size(max = 100, message = "External ID must not exceed 100 characters")
    private String externalId;

    /**
     * Issue tracking system (monorail, github, jira, etc.).
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "tracker", nullable = false)
    @NotNull(message = "Issue tracker is required")
    private IssueTracker tracker;

    /**
     * Project identifier in the tracking system.
     */
    @Column(name = "project", nullable = false, length = 100)
    @NotBlank(message = "Project is required")
    @Size(max = 100, message = "Project must not exceed 100 characters")
    private String project;

    /**
     * Issue title/summary.
     */
    @Column(name = "title", nullable = false, length = 500)
    @NotBlank(message = "Issue title is required")
    @Size(max = 500, message = "Title must not exceed 500 characters")
    private String title;

    /**
     * Issue description/body.
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * Current status of the issue.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @NotNull(message = "Issue status is required")
    @Builder.Default
    private IssueStatus status = IssueStatus.NEW;

    /**
     * Priority level of the issue.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "priority")
    private IssuePriority priority;

    /**
     * Severity level of the issue.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "severity")
    private IssueSeverity severity;

    /**
     * User who reported the issue.
     */
    @Column(name = "reporter", length = 100)
    @Size(max = 100, message = "Reporter must not exceed 100 characters")
    private String reporter;

    /**
     * User assigned to the issue.
     */
    @Column(name = "assignee", length = 100)
    @Size(max = 100, message = "Assignee must not exceed 100 characters")
    private String assignee;

    /**
     * Labels/tags associated with the issue.
     */
    @ElementCollection
    @CollectionTable(name = "issue_labels",
                    joinColumns = @JoinColumn(name = "issue_id"))
    @Column(name = "label")
    private Set<String> labels;

    /**
     * Components affected by this issue.
     */
    @ElementCollection
    @CollectionTable(name = "issue_components",
                    joinColumns = @JoinColumn(name = "issue_id"))
    @Column(name = "component")
    private Set<String> components;

    /**
     * URL to the issue in the external tracker.
     */
    @Column(name = "url", length = 500)
    @Size(max = 500, message = "URL must not exceed 500 characters")
    private String url;

    /**
     * Whether this is a security-related issue.
     */
    @Column(name = "is_security", nullable = false)
    @Builder.Default
    private Boolean isSecurity = false;

    /**
     * Whether this issue is publicly visible.
     */
    @Column(name = "is_public", nullable = false)
    @Builder.Default
    private Boolean isPublic = true;

    /**
     * Number of comments on the issue.
     */
    @Column(name = "comment_count")
    @Builder.Default
    private Integer commentCount = 0;

    /**
     * Last time the issue was updated in the external tracker.
     */
    @Column(name = "last_updated_external")
    private LocalDateTime lastUpdatedExternal;

    /**
     * Time when the issue was closed (if applicable).
     */
    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    /**
     * Reason for closing the issue.
     */
    @Column(name = "close_reason", length = 100)
    @Size(max = 100, message = "Close reason must not exceed 100 characters")
    private String closeReason;

    /**
     * Milestone or version this issue is targeted for.
     */
    @Column(name = "milestone", length = 100)
    @Size(max = 100, message = "Milestone must not exceed 100 characters")
    private String milestone;

    /**
     * Additional metadata as JSON.
     */
    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Testcases associated with this issue.
     */
    @OneToMany(mappedBy = "issue", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Testcase> testcases;

    /**
     * Issue tracking systems.
     */
    public enum IssueTracker {
        MONORAIL,
        GITHUB,
        JIRA,
        BUGZILLA,
        GITLAB,
        CUSTOM
    }

    /**
     * Issue status values.
     */
    public enum IssueStatus {
        NEW,
        ASSIGNED,
        ACCEPTED,
        STARTED,
        FIXED,
        VERIFIED,
        INVALID,
        WONTFIX,
        DUPLICATE,
        CLOSED
    }

    /**
     * Issue priority levels.
     */
    public enum IssuePriority {
        CRITICAL,
        HIGH,
        MEDIUM,
        LOW
    }

    /**
     * Issue severity levels.
     */
    public enum IssueSeverity {
        BLOCKER,
        CRITICAL,
        MAJOR,
        MINOR,
        TRIVIAL
    }

    /**
     * Check if the issue is open.
     */
    public boolean isOpen() {
        return status != IssueStatus.FIXED && 
               status != IssueStatus.VERIFIED && 
               status != IssueStatus.INVALID && 
               status != IssueStatus.WONTFIX && 
               status != IssueStatus.DUPLICATE && 
               status != IssueStatus.CLOSED;
    }

    /**
     * Check if the issue is closed.
     */
    public boolean isClosed() {
        return !isOpen();
    }

    /**
     * Mark the issue as fixed.
     */
    public void markFixed() {
        this.status = IssueStatus.FIXED;
        this.closedAt = LocalDateTime.now();
    }

    /**
     * Mark the issue as verified.
     */
    public void markVerified() {
        this.status = IssueStatus.VERIFIED;
        if (this.closedAt == null) {
            this.closedAt = LocalDateTime.now();
        }
    }

    /**
     * Close the issue with a reason.
     */
    public void close(IssueStatus closeStatus, String reason) {
        this.status = closeStatus;
        this.closeReason = reason;
        this.closedAt = LocalDateTime.now();
    }

    /**
     * Reopen the issue.
     */
    public void reopen() {
        this.status = IssueStatus.NEW;
        this.closedAt = null;
        this.closeReason = null;
    }

    /**
     * Add a label to the issue.
     */
    public void addLabel(String label) {
        if (this.labels == null) {
            this.labels = new java.util.HashSet<>();
        }
        this.labels.add(label);
    }

    /**
     * Remove a label from the issue.
     */
    public void removeLabel(String label) {
        if (this.labels != null) {
            this.labels.remove(label);
        }
    }

    /**
     * Add a component to the issue.
     */
    public void addComponent(String component) {
        if (this.components == null) {
            this.components = new java.util.HashSet<>();
        }
        this.components.add(component);
    }

    /**
     * Remove a component from the issue.
     */
    public void removeComponent(String component) {
        if (this.components != null) {
            this.components.remove(component);
        }
    }
}