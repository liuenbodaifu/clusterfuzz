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
 * Permissions for external users.
 * Controls access to specific entities and resources for non-admin users.
 */
@Entity
@Table(name = "external_user_permission", indexes = {
    @Index(name = "idx_external_permission_email", columnList = "email"),
    @Index(name = "idx_external_permission_entity_kind", columnList = "entityKind"),
    @Index(name = "idx_external_permission_entity_name", columnList = "entityName"),
    @Index(name = "idx_external_permission_email_entity", columnList = "email, entityKind, entityName"),
    @Index(name = "idx_external_permission_auto_cc", columnList = "autoCc")
})
@EntityListeners(AuditingEntityListener.class)
public class ExternalUserPermission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Email address of the user.
     */
    @NotNull
    @Email
    @Size(max = 255)
    @Column(name = "email", nullable = false, length = 255)
    private String email;

    /**
     * Type of entity that user is allowed to access.
     * Values: JOB(1), FUZZER(2), UPLOADER(3), etc.
     */
    @NotNull
    @Column(name = "entity_kind", nullable = false)
    private Integer entityKind;

    /**
     * Name of the specific entity that user is allowed to view.
     */
    @NotNull
    @Size(max = 255)
    @Column(name = "entity_name", nullable = false, length = 255)
    private String entityName;

    /**
     * Whether the entity_name is a prefix match.
     * If true, user can access all entities starting with entity_name.
     */
    @Column(name = "is_prefix", nullable = false)
    private Boolean isPrefix = false;

    /**
     * Auto CC type for notifications.
     * Values: NONE(0), ALL(1), SECURITY_ONLY(2), etc.
     */
    @Column(name = "auto_cc")
    private Integer autoCc = 0;

    /**
     * Whether this permission is currently active.
     */
    @Column(name = "enabled", nullable = false)
    private Boolean enabled = true;

    /**
     * Optional description of this permission.
     */
    @Size(max = 500)
    @Column(name = "description", length = 500)
    private String description;

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
     * User who created this permission.
     */
    @Size(max = 255)
    @Column(name = "created_by", length = 255)
    private String createdBy;

    // Entity kind constants
    public static final int ENTITY_KIND_JOB = 1;
    public static final int ENTITY_KIND_FUZZER = 2;
    public static final int ENTITY_KIND_UPLOADER = 3;
    public static final int ENTITY_KIND_PROJECT = 4;
    public static final int ENTITY_KIND_TESTCASE = 5;

    // Auto CC constants
    public static final int AUTO_CC_NONE = 0;
    public static final int AUTO_CC_ALL = 1;
    public static final int AUTO_CC_SECURITY_ONLY = 2;
    public static final int AUTO_CC_CRITICAL_ONLY = 3;

    // Constructors
    public ExternalUserPermission() {}

    public ExternalUserPermission(String email, Integer entityKind, String entityName) {
        this.email = email;
        this.entityKind = entityKind;
        this.entityName = entityName;
        this.isPrefix = false;
        this.autoCc = AUTO_CC_NONE;
        this.enabled = true;
    }

    public ExternalUserPermission(String email, Integer entityKind, String entityName, Boolean isPrefix) {
        this(email, entityKind, entityName);
        this.isPrefix = isPrefix;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getEntityKind() {
        return entityKind;
    }

    public void setEntityKind(Integer entityKind) {
        this.entityKind = entityKind;
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public Boolean getIsPrefix() {
        return isPrefix;
    }

    public void setIsPrefix(Boolean isPrefix) {
        this.isPrefix = isPrefix;
    }

    public Integer getAutoCc() {
        return autoCc;
    }

    public void setAutoCc(Integer autoCc) {
        this.autoCc = autoCc;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    // Business logic methods
    /**
     * Check if this permission allows access to the specified entity.
     */
    public boolean allowsAccessTo(String targetEntityName) {
        if (!Boolean.TRUE.equals(enabled)) {
            return false;
        }

        if (Boolean.TRUE.equals(isPrefix)) {
            return targetEntityName.startsWith(entityName);
        } else {
            return entityName.equals(targetEntityName);
        }
    }

    /**
     * Check if this permission applies to the specified entity kind.
     */
    public boolean appliesToEntityKind(Integer targetEntityKind) {
        return entityKind.equals(targetEntityKind);
    }

    /**
     * Check if user should be auto-CCed on notifications.
     */
    public boolean shouldAutoCc(boolean isSecurityIssue, boolean isCriticalIssue) {
        if (autoCc == null || autoCc == AUTO_CC_NONE) {
            return false;
        }

        switch (autoCc) {
            case AUTO_CC_ALL:
                return true;
            case AUTO_CC_SECURITY_ONLY:
                return isSecurityIssue;
            case AUTO_CC_CRITICAL_ONLY:
                return isCriticalIssue;
            default:
                return false;
        }
    }

    // Entity kind helper methods
    public boolean isJobPermission() {
        return ENTITY_KIND_JOB == entityKind;
    }

    public boolean isFuzzerPermission() {
        return ENTITY_KIND_FUZZER == entityKind;
    }

    public boolean isUploaderPermission() {
        return ENTITY_KIND_UPLOADER == entityKind;
    }

    public boolean isProjectPermission() {
        return ENTITY_KIND_PROJECT == entityKind;
    }

    public boolean isTestcasePermission() {
        return ENTITY_KIND_TESTCASE == entityKind;
    }

    // Display helper methods
    public String getEntityKindDisplayName() {
        switch (entityKind) {
            case ENTITY_KIND_JOB: return "Job";
            case ENTITY_KIND_FUZZER: return "Fuzzer";
            case ENTITY_KIND_UPLOADER: return "Uploader";
            case ENTITY_KIND_PROJECT: return "Project";
            case ENTITY_KIND_TESTCASE: return "Testcase";
            default: return "Unknown";
        }
    }

    public String getAutoCcDisplayName() {
        switch (autoCc) {
            case AUTO_CC_NONE: return "None";
            case AUTO_CC_ALL: return "All Issues";
            case AUTO_CC_SECURITY_ONLY: return "Security Issues Only";
            case AUTO_CC_CRITICAL_ONLY: return "Critical Issues Only";
            default: return "Unknown";
        }
    }

    public String getPermissionSummary() {
        String prefix = Boolean.TRUE.equals(isPrefix) ? " (prefix)" : "";
        return getEntityKindDisplayName() + ": " + entityName + prefix;
    }

    // Permission management
    public void enable() {
        this.enabled = true;
    }

    public void disable() {
        this.enabled = false;
    }

    public void setAutoCcAll() {
        this.autoCc = AUTO_CC_ALL;
    }

    public void setAutoCcSecurityOnly() {
        this.autoCc = AUTO_CC_SECURITY_ONLY;
    }

    public void setAutoCcCriticalOnly() {
        this.autoCc = AUTO_CC_CRITICAL_ONLY;
    }

    public void setAutoCcNone() {
        this.autoCc = AUTO_CC_NONE;
    }

    @Override
    public String toString() {
        return "ExternalUserPermission{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", entityKind=" + entityKind +
                ", entityName='" + entityName + '\'' +
                ", isPrefix=" + isPrefix +
                ", autoCc=" + autoCc +
                ", enabled=" + enabled +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ExternalUserPermission)) return false;
        ExternalUserPermission that = (ExternalUserPermission) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}