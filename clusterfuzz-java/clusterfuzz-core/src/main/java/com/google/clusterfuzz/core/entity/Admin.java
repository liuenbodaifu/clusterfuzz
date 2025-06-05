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
 * Records an admin user.
 * Manages administrative access and permissions for the ClusterFuzz system.
 */
@Entity
@Table(name = "admin", indexes = {
    @Index(name = "idx_admin_email", columnList = "email"),
    @Index(name = "idx_admin_active", columnList = "active"),
    @Index(name = "idx_admin_role", columnList = "role"),
    @Index(name = "idx_admin_created_at", columnList = "createdAt"),
    @Index(name = "idx_admin_last_login", columnList = "lastLoginAt")
})
@EntityListeners(AuditingEntityListener.class)
public class Admin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Email address of the admin user.
     */
    @NotNull
    @Email
    @Size(max = 255)
    @Column(name = "email", nullable = false, length = 255, unique = true)
    private String email;

    /**
     * Admin role/level.
     * Values: SUPER_ADMIN, ADMIN, MODERATOR
     */
    @Size(max = 50)
    @Column(name = "role", length = 50)
    private String role = "ADMIN";

    /**
     * Whether this admin account is active.
     */
    @Column(name = "active", nullable = false)
    private Boolean active = true;

    /**
     * Display name for the admin.
     */
    @Size(max = 255)
    @Column(name = "display_name", length = 255)
    private String displayName;

    /**
     * Admin permissions as JSON.
     */
    @Lob
    @Column(name = "permissions", columnDefinition = "TEXT")
    private String permissions;

    /**
     * Last login timestamp.
     */
    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    /**
     * Login count.
     */
    @Column(name = "login_count", nullable = false)
    private Long loginCount = 0L;

    /**
     * Last IP address used for login.
     */
    @Size(max = 45)
    @Column(name = "last_login_ip", length = 45)
    private String lastLoginIp;

    /**
     * Notes about this admin account.
     */
    @Lob
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    /**
     * Who created this admin account.
     */
    @Size(max = 255)
    @Column(name = "created_by", length = 255)
    private String createdBy;

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
     * Account expiration date (optional).
     */
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    // Role constants
    public static final String ROLE_SUPER_ADMIN = "SUPER_ADMIN";
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_MODERATOR = "MODERATOR";

    // Constructors
    public Admin() {}

    public Admin(String email) {
        this.email = email;
        this.role = ROLE_ADMIN;
        this.active = true;
        this.loginCount = 0L;
    }

    public Admin(String email, String role) {
        this(email);
        this.role = role;
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

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getPermissions() {
        return permissions;
    }

    public void setPermissions(String permissions) {
        this.permissions = permissions;
    }

    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(LocalDateTime lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

    public Long getLoginCount() {
        return loginCount;
    }

    public void setLoginCount(Long loginCount) {
        this.loginCount = loginCount;
    }

    public String getLastLoginIp() {
        return lastLoginIp;
    }

    public void setLastLoginIp(String lastLoginIp) {
        this.lastLoginIp = lastLoginIp;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
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

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    // Business logic methods
    /**
     * Record a login for this admin.
     */
    public void recordLogin(String ipAddress) {
        this.lastLoginAt = LocalDateTime.now();
        this.lastLoginIp = ipAddress;
        this.loginCount++;
    }

    /**
     * Check if this admin is a super admin.
     */
    public boolean isSuperAdmin() {
        return ROLE_SUPER_ADMIN.equals(role);
    }

    /**
     * Check if this admin is a regular admin.
     */
    public boolean isAdmin() {
        return ROLE_ADMIN.equals(role);
    }

    /**
     * Check if this admin is a moderator.
     */
    public boolean isModerator() {
        return ROLE_MODERATOR.equals(role);
    }

    /**
     * Check if the account is expired.
     */
    public boolean isExpired() {
        return expiresAt != null && expiresAt.isBefore(LocalDateTime.now());
    }

    /**
     * Check if the admin account is valid (active and not expired).
     */
    public boolean isValid() {
        return Boolean.TRUE.equals(active) && !isExpired();
    }

    /**
     * Activate the admin account.
     */
    public void activate() {
        this.active = true;
    }

    /**
     * Deactivate the admin account.
     */
    public void deactivate() {
        this.active = false;
    }

    /**
     * Set expiration date.
     */
    public void setExpiration(LocalDateTime expirationDate) {
        this.expiresAt = expirationDate;
    }

    /**
     * Remove expiration (make permanent).
     */
    public void removePermanent() {
        this.expiresAt = null;
    }

    /**
     * Get role display name.
     */
    public String getRoleDisplayName() {
        switch (role) {
            case ROLE_SUPER_ADMIN: return "Super Administrator";
            case ROLE_ADMIN: return "Administrator";
            case ROLE_MODERATOR: return "Moderator";
            default: return role;
        }
    }

    /**
     * Get display name or email if display name is not set.
     */
    public String getDisplayNameOrEmail() {
        return displayName != null && !displayName.trim().isEmpty() ? displayName : email;
    }

    /**
     * Check if admin has been active recently (logged in within last 30 days).
     */
    public boolean isRecentlyActive() {
        return lastLoginAt != null && 
               lastLoginAt.isAfter(LocalDateTime.now().minusDays(30));
    }

    /**
     * Get days since last login.
     */
    public Long getDaysSinceLastLogin() {
        if (lastLoginAt == null) return null;
        return java.time.Duration.between(lastLoginAt, LocalDateTime.now()).toDays();
    }

    /**
     * Check if this is a new admin (never logged in).
     */
    public boolean isNewAdmin() {
        return loginCount == 0 || lastLoginAt == null;
    }

    /**
     * Promote to super admin.
     */
    public void promoteToSuperAdmin() {
        this.role = ROLE_SUPER_ADMIN;
    }

    /**
     * Demote to regular admin.
     */
    public void demoteToAdmin() {
        this.role = ROLE_ADMIN;
    }

    /**
     * Demote to moderator.
     */
    public void demoteToModerator() {
        this.role = ROLE_MODERATOR;
    }

    /**
     * Get account status summary.
     */
    public String getAccountStatus() {
        if (!Boolean.TRUE.equals(active)) {
            return "Inactive";
        }
        if (isExpired()) {
            return "Expired";
        }
        if (isNewAdmin()) {
            return "New (Never Logged In)";
        }
        if (isRecentlyActive()) {
            return "Active";
        }
        return "Dormant";
    }

    @Override
    public String toString() {
        return "Admin{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", role='" + role + '\'' +
                ", active=" + active +
                ", displayName='" + displayName + '\'' +
                ", loginCount=" + loginCount +
                ", lastLoginAt=" + lastLoginAt +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Admin)) return false;
        Admin admin = (Admin) o;
        return id != null && id.equals(admin.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}