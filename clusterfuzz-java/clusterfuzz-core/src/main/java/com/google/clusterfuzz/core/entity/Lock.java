package com.google.clusterfuzz.core.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Lock entity for distributed coordination.
 * Provides distributed locking mechanism for critical sections and resource coordination.
 */
@Entity
@Table(name = "lock_entity", indexes = {
    @Index(name = "idx_lock_name", columnList = "lockName"),
    @Index(name = "idx_lock_holder", columnList = "holder"),
    @Index(name = "idx_lock_expiration", columnList = "expirationTime"),
    @Index(name = "idx_lock_active", columnList = "active"),
    @Index(name = "idx_lock_created_at", columnList = "createdAt")
})
@EntityListeners(AuditingEntityListener.class)
public class Lock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Name/identifier of the lock.
     */
    @NotNull
    @Size(max = 255)
    @Column(name = "lock_name", nullable = false, length = 255, unique = true)
    private String lockName;

    /**
     * Expiration time for the lock.
     */
    @Column(name = "expiration_time")
    private LocalDateTime expirationTime;

    /**
     * The bot name denoting the holder of the lock.
     */
    @Size(max = 255)
    @Column(name = "holder", length = 255)
    private String holder;

    /**
     * Whether this lock is currently active.
     */
    @Column(name = "active", nullable = false)
    private Boolean active = true;

    /**
     * Lock timeout in seconds.
     */
    @Column(name = "timeout_seconds")
    private Integer timeoutSeconds;

    /**
     * Lock purpose or description.
     */
    @Size(max = 500)
    @Column(name = "purpose", length = 500)
    private String purpose;

    /**
     * Number of times this lock has been acquired.
     */
    @Column(name = "acquisition_count", nullable = false)
    private Long acquisitionCount = 0L;

    /**
     * Last acquisition timestamp.
     */
    @Column(name = "last_acquired_at")
    private LocalDateTime lastAcquiredAt;

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

    // Default lock timeout in seconds (5 minutes)
    public static final int DEFAULT_TIMEOUT_SECONDS = 300;

    // Constructors
    public Lock() {}

    public Lock(String lockName) {
        this.lockName = lockName;
        this.active = true;
        this.acquisitionCount = 0L;
    }

    public Lock(String lockName, String holder) {
        this(lockName);
        this.holder = holder;
    }

    public Lock(String lockName, String holder, int timeoutSeconds) {
        this(lockName, holder);
        this.timeoutSeconds = timeoutSeconds;
        this.expirationTime = LocalDateTime.now().plusSeconds(timeoutSeconds);
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLockName() {
        return lockName;
    }

    public void setLockName(String lockName) {
        this.lockName = lockName;
    }

    public LocalDateTime getExpirationTime() {
        return expirationTime;
    }

    public void setExpirationTime(LocalDateTime expirationTime) {
        this.expirationTime = expirationTime;
    }

    public String getHolder() {
        return holder;
    }

    public void setHolder(String holder) {
        this.holder = holder;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Integer getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public void setTimeoutSeconds(Integer timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public Long getAcquisitionCount() {
        return acquisitionCount;
    }

    public void setAcquisitionCount(Long acquisitionCount) {
        this.acquisitionCount = acquisitionCount;
    }

    public LocalDateTime getLastAcquiredAt() {
        return lastAcquiredAt;
    }

    public void setLastAcquiredAt(LocalDateTime lastAcquiredAt) {
        this.lastAcquiredAt = lastAcquiredAt;
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
     * Acquire the lock for the specified holder.
     */
    public boolean acquire(String holder, int timeoutSeconds) {
        if (isExpired() || !Boolean.TRUE.equals(active)) {
            this.holder = holder;
            this.timeoutSeconds = timeoutSeconds;
            this.expirationTime = LocalDateTime.now().plusSeconds(timeoutSeconds);
            this.lastAcquiredAt = LocalDateTime.now();
            this.acquisitionCount++;
            this.active = true;
            return true;
        }
        return false;
    }

    /**
     * Acquire the lock with default timeout.
     */
    public boolean acquire(String holder) {
        return acquire(holder, DEFAULT_TIMEOUT_SECONDS);
    }

    /**
     * Release the lock.
     */
    public boolean release(String holder) {
        if (this.holder != null && this.holder.equals(holder)) {
            this.holder = null;
            this.expirationTime = null;
            this.active = false;
            return true;
        }
        return false;
    }

    /**
     * Force release the lock (admin operation).
     */
    public void forceRelease() {
        this.holder = null;
        this.expirationTime = null;
        this.active = false;
    }

    /**
     * Extend the lock expiration time.
     */
    public boolean extend(String holder, int additionalSeconds) {
        if (this.holder != null && this.holder.equals(holder) && !isExpired()) {
            this.expirationTime = this.expirationTime.plusSeconds(additionalSeconds);
            return true;
        }
        return false;
    }

    /**
     * Check if the lock is expired.
     */
    public boolean isExpired() {
        return expirationTime != null && expirationTime.isBefore(LocalDateTime.now());
    }

    /**
     * Check if the lock is currently held.
     */
    public boolean isHeld() {
        return Boolean.TRUE.equals(active) && holder != null && !isExpired();
    }

    /**
     * Check if the lock is held by the specified holder.
     */
    public boolean isHeldBy(String holder) {
        return isHeld() && this.holder != null && this.holder.equals(holder);
    }

    /**
     * Check if the lock is available for acquisition.
     */
    public boolean isAvailable() {
        return !isHeld();
    }

    /**
     * Get remaining time in seconds before expiration.
     */
    public Long getRemainingTimeSeconds() {
        if (expirationTime == null || isExpired()) {
            return 0L;
        }
        return java.time.Duration.between(LocalDateTime.now(), expirationTime).getSeconds();
    }

    /**
     * Get lock duration in seconds since acquisition.
     */
    public Long getLockDurationSeconds() {
        if (lastAcquiredAt == null) {
            return 0L;
        }
        LocalDateTime endTime = isHeld() ? LocalDateTime.now() : 
                               (expirationTime != null ? expirationTime : LocalDateTime.now());
        return java.time.Duration.between(lastAcquiredAt, endTime).getSeconds();
    }

    /**
     * Get lock status description.
     */
    public String getStatusDescription() {
        if (!Boolean.TRUE.equals(active)) {
            return "Inactive";
        }
        if (isExpired()) {
            return "Expired";
        }
        if (isHeld()) {
            return "Held by " + holder + " (expires in " + getRemainingTimeSeconds() + "s)";
        }
        return "Available";
    }

    /**
     * Refresh the lock expiration time.
     */
    public boolean refresh(String holder) {
        if (isHeldBy(holder)) {
            int timeout = timeoutSeconds != null ? timeoutSeconds : DEFAULT_TIMEOUT_SECONDS;
            this.expirationTime = LocalDateTime.now().plusSeconds(timeout);
            return true;
        }
        return false;
    }

    /**
     * Check if lock needs renewal (less than 25% time remaining).
     */
    public boolean needsRenewal() {
        if (!isHeld() || timeoutSeconds == null) {
            return false;
        }
        long remaining = getRemainingTimeSeconds();
        return remaining < (timeoutSeconds * 0.25);
    }

    @Override
    public String toString() {
        return "Lock{" +
                "id=" + id +
                ", lockName='" + lockName + '\'' +
                ", holder='" + holder + '\'' +
                ", expirationTime=" + expirationTime +
                ", active=" + active +
                ", acquisitionCount=" + acquisitionCount +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Lock)) return false;
        Lock lock = (Lock) o;
        return id != null && id.equals(lock.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}