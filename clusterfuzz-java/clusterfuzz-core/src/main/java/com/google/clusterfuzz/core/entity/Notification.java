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
 * Tracks whether or not an email has been sent to a user for a test case.
 * Prevents duplicate notifications and tracks notification history.
 */
@Entity
@Table(name = "notification", 
    indexes = {
        @Index(name = "idx_notification_testcase_id", columnList = "testcaseId"),
        @Index(name = "idx_notification_user_email", columnList = "userEmail"),
        @Index(name = "idx_notification_testcase_user", columnList = "testcaseId, userEmail"),
        @Index(name = "idx_notification_type", columnList = "notificationType"),
        @Index(name = "idx_notification_sent", columnList = "sent"),
        @Index(name = "idx_notification_created_at", columnList = "createdAt")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_notification_testcase_user_type", 
                         columnNames = {"testcaseId", "userEmail", "notificationType"})
    }
)
@EntityListeners(AuditingEntityListener.class)
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Testcase ID associated with this notification.
     */
    @NotNull
    @Column(name = "testcase_id", nullable = false)
    private Long testcaseId;

    /**
     * User email that this notification was sent to.
     */
    @NotNull
    @Email
    @Size(max = 255)
    @Column(name = "user_email", nullable = false, length = 255)
    private String userEmail;

    /**
     * Type of notification sent.
     * Values: NEW_CRASH, FIXED, DUPLICATE, SECURITY_ISSUE, etc.
     */
    @NotNull
    @Size(max = 50)
    @Column(name = "notification_type", nullable = false, length = 50)
    private String notificationType;

    /**
     * Whether the notification was successfully sent.
     */
    @Column(name = "sent", nullable = false)
    private Boolean sent = false;

    /**
     * Number of attempts to send this notification.
     */
    @Column(name = "send_attempts", nullable = false)
    private Integer sendAttempts = 0;

    /**
     * Last error message if sending failed.
     */
    @Lob
    @Column(name = "last_error", columnDefinition = "TEXT")
    private String lastError;

    /**
     * Timestamp when notification was sent (if successful).
     */
    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    /**
     * Subject of the notification email.
     */
    @Size(max = 500)
    @Column(name = "subject", length = 500)
    private String subject;

    /**
     * Body content of the notification.
     */
    @Lob
    @Column(name = "body", columnDefinition = "TEXT")
    private String body;

    /**
     * Priority of the notification.
     * Values: LOW, NORMAL, HIGH, URGENT
     */
    @Size(max = 20)
    @Column(name = "priority", length = 20)
    private String priority = "NORMAL";

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
     * Next retry timestamp for failed notifications.
     */
    @Column(name = "next_retry_at")
    private LocalDateTime nextRetryAt;

    // Notification type constants
    public static final String TYPE_NEW_CRASH = "NEW_CRASH";
    public static final String TYPE_FIXED = "FIXED";
    public static final String TYPE_DUPLICATE = "DUPLICATE";
    public static final String TYPE_SECURITY_ISSUE = "SECURITY_ISSUE";
    public static final String TYPE_REGRESSION = "REGRESSION";
    public static final String TYPE_STATUS_UPDATE = "STATUS_UPDATE";
    public static final String TYPE_COMMENT_ADDED = "COMMENT_ADDED";

    // Priority constants
    public static final String PRIORITY_LOW = "LOW";
    public static final String PRIORITY_NORMAL = "NORMAL";
    public static final String PRIORITY_HIGH = "HIGH";
    public static final String PRIORITY_URGENT = "URGENT";

    // Constructors
    public Notification() {}

    public Notification(Long testcaseId, String userEmail, String notificationType) {
        this.testcaseId = testcaseId;
        this.userEmail = userEmail;
        this.notificationType = notificationType;
        this.sent = false;
        this.sendAttempts = 0;
        this.priority = PRIORITY_NORMAL;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTestcaseId() {
        return testcaseId;
    }

    public void setTestcaseId(Long testcaseId) {
        this.testcaseId = testcaseId;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(String notificationType) {
        this.notificationType = notificationType;
    }

    public Boolean getSent() {
        return sent;
    }

    public void setSent(Boolean sent) {
        this.sent = sent;
    }

    public Integer getSendAttempts() {
        return sendAttempts;
    }

    public void setSendAttempts(Integer sendAttempts) {
        this.sendAttempts = sendAttempts;
    }

    public String getLastError() {
        return lastError;
    }

    public void setLastError(String lastError) {
        this.lastError = lastError;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
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

    public LocalDateTime getNextRetryAt() {
        return nextRetryAt;
    }

    public void setNextRetryAt(LocalDateTime nextRetryAt) {
        this.nextRetryAt = nextRetryAt;
    }

    // Business logic methods
    /**
     * Mark notification as successfully sent.
     */
    public void markAsSent() {
        this.sent = true;
        this.sentAt = LocalDateTime.now();
        this.lastError = null;
        this.nextRetryAt = null;
    }

    /**
     * Mark notification as failed with error.
     */
    public void markAsFailed(String errorMessage) {
        this.sent = false;
        this.sendAttempts++;
        this.lastError = errorMessage;
        
        // Calculate next retry time (exponential backoff)
        long delayMinutes = Math.min(60, (long) Math.pow(2, sendAttempts));
        this.nextRetryAt = LocalDateTime.now().plusMinutes(delayMinutes);
    }

    /**
     * Check if notification is pending (not sent and ready for retry).
     */
    public boolean isPending() {
        return !Boolean.TRUE.equals(sent) && 
               (nextRetryAt == null || nextRetryAt.isBefore(LocalDateTime.now()));
    }

    /**
     * Check if notification has failed too many times.
     */
    public boolean hasExceededMaxRetries() {
        return sendAttempts >= 5; // Max 5 retry attempts
    }

    /**
     * Check if this is a high priority notification.
     */
    public boolean isHighPriority() {
        return PRIORITY_HIGH.equals(priority) || PRIORITY_URGENT.equals(priority);
    }

    /**
     * Check if this is a security-related notification.
     */
    public boolean isSecurityNotification() {
        return TYPE_SECURITY_ISSUE.equals(notificationType);
    }

    /**
     * Get notification type display name.
     */
    public String getNotificationTypeDisplayName() {
        switch (notificationType) {
            case TYPE_NEW_CRASH: return "New Crash";
            case TYPE_FIXED: return "Fixed";
            case TYPE_DUPLICATE: return "Duplicate";
            case TYPE_SECURITY_ISSUE: return "Security Issue";
            case TYPE_REGRESSION: return "Regression";
            case TYPE_STATUS_UPDATE: return "Status Update";
            case TYPE_COMMENT_ADDED: return "Comment Added";
            default: return notificationType;
        }
    }

    /**
     * Get priority display name.
     */
    public String getPriorityDisplayName() {
        switch (priority) {
            case PRIORITY_LOW: return "Low";
            case PRIORITY_NORMAL: return "Normal";
            case PRIORITY_HIGH: return "High";
            case PRIORITY_URGENT: return "Urgent";
            default: return priority;
        }
    }

    /**
     * Set priority to high.
     */
    public void setHighPriority() {
        this.priority = PRIORITY_HIGH;
    }

    /**
     * Set priority to urgent.
     */
    public void setUrgentPriority() {
        this.priority = PRIORITY_URGENT;
    }

    /**
     * Reset retry counter for manual retry.
     */
    public void resetForRetry() {
        this.sendAttempts = 0;
        this.lastError = null;
        this.nextRetryAt = null;
        this.sent = false;
    }

    /**
     * Check if notification is ready for retry.
     */
    public boolean isReadyForRetry() {
        return !Boolean.TRUE.equals(sent) && 
               !hasExceededMaxRetries() &&
               (nextRetryAt == null || nextRetryAt.isBefore(LocalDateTime.now()));
    }

    @Override
    public String toString() {
        return "Notification{" +
                "id=" + id +
                ", testcaseId=" + testcaseId +
                ", userEmail='" + userEmail + '\'' +
                ", notificationType='" + notificationType + '\'' +
                ", sent=" + sent +
                ", sendAttempts=" + sendAttempts +
                ", priority='" + priority + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Notification)) return false;
        Notification that = (Notification) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}