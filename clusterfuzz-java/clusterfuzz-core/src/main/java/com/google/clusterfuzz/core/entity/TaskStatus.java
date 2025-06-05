package com.google.clusterfuzz.core.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Information about task status.
 * Tracks the current status and progress of tasks running on bots.
 */
@Entity
@Table(name = "task_status", indexes = {
    @Index(name = "idx_task_status_bot_name", columnList = "botName"),
    @Index(name = "idx_task_status_status", columnList = "status"),
    @Index(name = "idx_task_status_time", columnList = "time"),
    @Index(name = "idx_task_status_task_name", columnList = "taskName"),
    @Index(name = "idx_task_status_bot_status", columnList = "botName, status"),
    @Index(name = "idx_task_status_active", columnList = "active")
})
@EntityListeners(AuditingEntityListener.class)
public class TaskStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Bot name executing the task.
     */
    @NotNull
    @Size(max = 255)
    @Column(name = "bot_name", nullable = false, length = 255)
    private String botName;

    /**
     * Current status of the task.
     * Values: PENDING, RUNNING, COMPLETED, FAILED, CANCELLED, TIMEOUT
     */
    @NotNull
    @Size(max = 50)
    @Column(name = "status", nullable = false, length = 50)
    private String status;

    /**
     * Time of creation or last update time.
     */
    @NotNull
    @Column(name = "time", nullable = false)
    private LocalDateTime time;

    /**
     * Name of the task being executed.
     */
    @Size(max = 255)
    @Column(name = "task_name", length = 255)
    private String taskName;

    /**
     * Task arguments or parameters.
     */
    @Lob
    @Column(name = "task_arguments", columnDefinition = "TEXT")
    private String taskArguments;

    /**
     * Progress percentage (0-100).
     */
    @Column(name = "progress_percentage")
    private Integer progressPercentage = 0;

    /**
     * Current step or phase of the task.
     */
    @Size(max = 255)
    @Column(name = "current_step", length = 255)
    private String currentStep;

    /**
     * Estimated completion time.
     */
    @Column(name = "estimated_completion")
    private LocalDateTime estimatedCompletion;

    /**
     * Task start time.
     */
    @Column(name = "started_at")
    private LocalDateTime startedAt;

    /**
     * Task completion time.
     */
    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    /**
     * Error message if task failed.
     */
    @Lob
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    /**
     * Task output or logs.
     */
    @Lob
    @Column(name = "task_output", columnDefinition = "TEXT")
    private String taskOutput;

    /**
     * Whether this task status is currently active.
     */
    @Column(name = "active", nullable = false)
    private Boolean active = true;

    /**
     * Priority of the task.
     */
    @Column(name = "priority")
    private Integer priority = 0;

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

    // Status constants
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_RUNNING = "RUNNING";
    public static final String STATUS_COMPLETED = "COMPLETED";
    public static final String STATUS_FAILED = "FAILED";
    public static final String STATUS_CANCELLED = "CANCELLED";
    public static final String STATUS_TIMEOUT = "TIMEOUT";

    // Constructors
    public TaskStatus() {
        this.time = LocalDateTime.now();
    }

    public TaskStatus(String botName, String status) {
        this();
        this.botName = botName;
        this.status = status;
    }

    public TaskStatus(String botName, String status, String taskName) {
        this(botName, status);
        this.taskName = taskName;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBotName() {
        return botName;
    }

    public void setBotName(String botName) {
        this.botName = botName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
        this.time = LocalDateTime.now();
    }

    public LocalDateTime getTime() {
        return time;
    }

    public void setTime(LocalDateTime time) {
        this.time = time;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getTaskArguments() {
        return taskArguments;
    }

    public void setTaskArguments(String taskArguments) {
        this.taskArguments = taskArguments;
    }

    public Integer getProgressPercentage() {
        return progressPercentage;
    }

    public void setProgressPercentage(Integer progressPercentage) {
        this.progressPercentage = progressPercentage;
    }

    public String getCurrentStep() {
        return currentStep;
    }

    public void setCurrentStep(String currentStep) {
        this.currentStep = currentStep;
    }

    public LocalDateTime getEstimatedCompletion() {
        return estimatedCompletion;
    }

    public void setEstimatedCompletion(LocalDateTime estimatedCompletion) {
        this.estimatedCompletion = estimatedCompletion;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getTaskOutput() {
        return taskOutput;
    }

    public void setTaskOutput(String taskOutput) {
        this.taskOutput = taskOutput;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
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

    // Business logic methods
    /**
     * Start the task.
     */
    public void startTask() {
        this.status = STATUS_RUNNING;
        this.startedAt = LocalDateTime.now();
        this.time = this.startedAt;
        this.progressPercentage = 0;
    }

    /**
     * Complete the task successfully.
     */
    public void completeTask() {
        this.status = STATUS_COMPLETED;
        this.completedAt = LocalDateTime.now();
        this.time = this.completedAt;
        this.progressPercentage = 100;
        this.errorMessage = null;
    }

    /**
     * Fail the task with error message.
     */
    public void failTask(String errorMessage) {
        this.status = STATUS_FAILED;
        this.completedAt = LocalDateTime.now();
        this.time = this.completedAt;
        this.errorMessage = errorMessage;
    }

    /**
     * Cancel the task.
     */
    public void cancelTask() {
        this.status = STATUS_CANCELLED;
        this.completedAt = LocalDateTime.now();
        this.time = this.completedAt;
    }

    /**
     * Mark task as timed out.
     */
    public void timeoutTask() {
        this.status = STATUS_TIMEOUT;
        this.completedAt = LocalDateTime.now();
        this.time = this.completedAt;
        this.errorMessage = "Task execution timed out";
    }

    /**
     * Update task progress.
     */
    public void updateProgress(Integer percentage, String currentStep) {
        this.progressPercentage = Math.max(0, Math.min(100, percentage));
        this.currentStep = currentStep;
        this.time = LocalDateTime.now();
    }

    /**
     * Check if task is running.
     */
    public boolean isRunning() {
        return STATUS_RUNNING.equals(status);
    }

    /**
     * Check if task is completed.
     */
    public boolean isCompleted() {
        return STATUS_COMPLETED.equals(status);
    }

    /**
     * Check if task has failed.
     */
    public boolean isFailed() {
        return STATUS_FAILED.equals(status);
    }

    /**
     * Check if task is cancelled.
     */
    public boolean isCancelled() {
        return STATUS_CANCELLED.equals(status);
    }

    /**
     * Check if task has timed out.
     */
    public boolean isTimedOut() {
        return STATUS_TIMEOUT.equals(status);
    }

    /**
     * Check if task is in a terminal state.
     */
    public boolean isTerminal() {
        return isCompleted() || isFailed() || isCancelled() || isTimedOut();
    }

    /**
     * Check if task is pending.
     */
    public boolean isPending() {
        return STATUS_PENDING.equals(status);
    }

    /**
     * Get task duration in seconds.
     */
    public Long getDurationSeconds() {
        if (startedAt == null) return null;
        
        LocalDateTime endTime = completedAt != null ? completedAt : LocalDateTime.now();
        return java.time.Duration.between(startedAt, endTime).getSeconds();
    }

    /**
     * Get status display name.
     */
    public String getStatusDisplayName() {
        switch (status) {
            case STATUS_PENDING: return "Pending";
            case STATUS_RUNNING: return "Running";
            case STATUS_COMPLETED: return "Completed";
            case STATUS_FAILED: return "Failed";
            case STATUS_CANCELLED: return "Cancelled";
            case STATUS_TIMEOUT: return "Timed Out";
            default: return status;
        }
    }

    /**
     * Get progress display string.
     */
    public String getProgressDisplay() {
        StringBuilder progress = new StringBuilder();
        progress.append(progressPercentage != null ? progressPercentage : 0).append("%");
        
        if (currentStep != null && !currentStep.trim().isEmpty()) {
            progress.append(" - ").append(currentStep);
        }
        
        return progress.toString();
    }

    /**
     * Deactivate this task status.
     */
    public void deactivate() {
        this.active = false;
    }

    /**
     * Activate this task status.
     */
    public void activate() {
        this.active = true;
    }

    @Override
    public String toString() {
        return "TaskStatus{" +
                "id=" + id +
                ", botName='" + botName + '\'' +
                ", status='" + status + '\'' +
                ", taskName='" + taskName + '\'' +
                ", progressPercentage=" + progressPercentage +
                ", time=" + time +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TaskStatus)) return false;
        TaskStatus that = (TaskStatus) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}