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
import java.util.Map;

/**
 * Entity representing a ClusterFuzz bot instance.
 * Bots are worker processes that execute fuzzing tasks and other operations.
 */
@Entity
@Table(name = "bots", indexes = {
    @Index(name = "idx_bot_name", columnList = "name"),
    @Index(name = "idx_bot_platform", columnList = "platform"),
    @Index(name = "idx_bot_last_beat_time", columnList = "lastBeatTime"),
    @Index(name = "idx_bot_task_name", columnList = "taskName")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Bot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Unique name/identifier for this bot instance.
     */
    @Column(name = "name", unique = true, nullable = false, length = 100)
    @NotBlank(message = "Bot name is required")
    @Size(max = 100, message = "Bot name must not exceed 100 characters")
    private String name;

    /**
     * Platform this bot is running on (linux, windows, macos, etc.).
     */
    @Column(name = "platform", nullable = false, length = 50)
    @NotBlank(message = "Platform is required")
    @Size(max = 50, message = "Platform must not exceed 50 characters")
    private String platform;

    /**
     * Version of the bot software.
     */
    @Column(name = "version", length = 50)
    @Size(max = 50, message = "Version must not exceed 50 characters")
    private String version;

    /**
     * Last time this bot sent a heartbeat.
     */
    @Column(name = "last_beat_time")
    private LocalDateTime lastBeatTime;

    /**
     * Current task payload (JSON) being executed by this bot.
     */
    @Column(name = "task_payload", columnDefinition = "TEXT")
    private String taskPayload;

    /**
     * Name of the current task being executed.
     */
    @Column(name = "task_name", length = 200)
    @Size(max = 200, message = "Task name must not exceed 200 characters")
    private String taskName;

    /**
     * Expected end time for the current task.
     */
    @Column(name = "task_end_time")
    private LocalDateTime taskEndTime;

    /**
     * Source code version/revision this bot is running.
     */
    @Column(name = "source_version", length = 100)
    @Size(max = 100, message = "Source version must not exceed 100 characters")
    private String sourceVersion;

    /**
     * Current status of the bot.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @NotNull(message = "Bot status is required")
    @Builder.Default
    private BotStatus status = BotStatus.IDLE;

    /**
     * IP address of the bot instance.
     */
    @Column(name = "ip_address", length = 45) // IPv6 support
    @Size(max = 45, message = "IP address must not exceed 45 characters")
    private String ipAddress;

    /**
     * Port number for bot communication.
     */
    @Column(name = "port")
    private Integer port;

    /**
     * CPU architecture (x86_64, arm64, etc.).
     */
    @Column(name = "cpu_architecture", length = 50)
    @Size(max = 50, message = "CPU architecture must not exceed 50 characters")
    private String cpuArchitecture;

    /**
     * Number of CPU cores available.
     */
    @Column(name = "cpu_cores")
    private Integer cpuCores;

    /**
     * Total memory in MB.
     */
    @Column(name = "memory_mb")
    private Integer memoryMb;

    /**
     * Available disk space in MB.
     */
    @Column(name = "disk_space_mb")
    private Integer diskSpaceMb;

    /**
     * Bot configuration as JSON.
     */
    @Column(name = "configuration", columnDefinition = "TEXT")
    private String configuration;

    /**
     * Environment variables for this bot.
     */
    @ElementCollection
    @CollectionTable(name = "bot_environment", 
                    joinColumns = @JoinColumn(name = "bot_id"))
    @MapKeyColumn(name = "env_key")
    @Column(name = "env_value")
    private Map<String, String> environment;

    /**
     * Supported fuzzing engines on this bot.
     */
    @ElementCollection
    @CollectionTable(name = "bot_fuzzing_engines",
                    joinColumns = @JoinColumn(name = "bot_id"))
    @Column(name = "engine_name")
    private java.util.Set<String> supportedEngines;

    /**
     * Whether this bot is currently available for new tasks.
     */
    @Column(name = "available", nullable = false)
    @Builder.Default
    private Boolean available = true;

    /**
     * Number of tasks completed by this bot.
     */
    @Column(name = "tasks_completed")
    @Builder.Default
    private Long tasksCompleted = 0L;

    /**
     * Number of crashes found by this bot.
     */
    @Column(name = "crashes_found")
    @Builder.Default
    private Long crashesFound = 0L;

    /**
     * Last error message from this bot.
     */
    @Column(name = "last_error", columnDefinition = "TEXT")
    private String lastError;

    /**
     * Time when this bot was last restarted.
     */
    @Column(name = "last_restart_time")
    private LocalDateTime lastRestartTime;

    /**
     * Uptime in seconds since last restart.
     */
    @Column(name = "uptime_seconds")
    private Long uptimeSeconds;

    /**
     * Performance metrics as JSON.
     */
    @Column(name = "performance_metrics", columnDefinition = "TEXT")
    private String performanceMetrics;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Bot status enumeration.
     */
    public enum BotStatus {
        /**
         * Bot is idle and available for tasks.
         */
        IDLE,
        
        /**
         * Bot is currently executing a task.
         */
        BUSY,
        
        /**
         * Bot is offline or unreachable.
         */
        OFFLINE,
        
        /**
         * Bot encountered an error.
         */
        ERROR,
        
        /**
         * Bot is being updated or maintained.
         */
        MAINTENANCE,
        
        /**
         * Bot is shutting down.
         */
        SHUTDOWN
    }

    /**
     * Check if the bot is considered alive based on last heartbeat.
     */
    public boolean isAlive() {
        if (lastBeatTime == null) {
            return false;
        }
        return lastBeatTime.isAfter(LocalDateTime.now().minusMinutes(5));
    }

    /**
     * Check if the bot is available for new tasks.
     */
    public boolean isAvailableForTasks() {
        return available && status == BotStatus.IDLE && isAlive();
    }

    /**
     * Update the heartbeat timestamp.
     */
    public void updateHeartbeat() {
        this.lastBeatTime = LocalDateTime.now();
    }

    /**
     * Mark the bot as busy with a specific task.
     */
    public void assignTask(String taskName, String taskPayload, LocalDateTime taskEndTime) {
        this.taskName = taskName;
        this.taskPayload = taskPayload;
        this.taskEndTime = taskEndTime;
        this.status = BotStatus.BUSY;
        this.available = false;
    }

    /**
     * Mark the bot as idle and clear current task.
     */
    public void completeTask() {
        this.taskName = null;
        this.taskPayload = null;
        this.taskEndTime = null;
        this.status = BotStatus.IDLE;
        this.available = true;
        this.tasksCompleted++;
    }

    /**
     * Record a crash found by this bot.
     */
    public void recordCrashFound() {
        this.crashesFound++;
    }

    /**
     * Set error status with message.
     */
    public void setError(String errorMessage) {
        this.status = BotStatus.ERROR;
        this.lastError = errorMessage;
        this.available = false;
    }

    /**
     * Clear error status and make bot available.
     */
    public void clearError() {
        this.status = BotStatus.IDLE;
        this.lastError = null;
        this.available = true;
    }
}