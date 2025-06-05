package com.google.clusterfuzz.core.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Represents a fuzz target in the ClusterFuzz system.
 * This is the Java equivalent of the Python FuzzTarget model.
 */
@Entity
@Table(name = "fuzz_targets", indexes = {
    @Index(name = "idx_fuzz_target_engine", columnList = "engine"),
    @Index(name = "idx_fuzz_target_project", columnList = "project"),
    @Index(name = "idx_fuzz_target_binary", columnList = "binary"),
    @Index(name = "idx_fuzz_target_fqn", columnList = "fullyQualifiedName", unique = true)
})
@EntityListeners(AuditingEntityListener.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class FuzzTarget {

    // Pattern for normalizing special characters in names
    private static final Pattern SPECIAL_CHARS_PATTERN = Pattern.compile("[^a-zA-Z0-9_-]");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Engine is required")
    @Column(name = "engine", nullable = false)
    private String engine;

    @NotBlank(message = "Project is required")
    @Column(name = "project", nullable = false)
    private String project;

    @NotBlank(message = "Binary is required")
    @Column(name = "binary", nullable = false)
    private String binary;

    @Column(name = "fully_qualified_name", unique = true)
    private String fullyQualifiedName;

    @Column(name = "project_qualified_name")
    private String projectQualifiedName;

    @CreatedDate
    @Column(name = "created", updatable = false)
    private LocalDateTime created;

    @LastModifiedDate
    @Column(name = "updated")
    private LocalDateTime updated;

    // Constructors
    public FuzzTarget() {
    }

    public FuzzTarget(String engine, String project, String binary) {
        this.engine = engine;
        this.project = project;
        this.binary = binary;
        updateQualifiedNames();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEngine() {
        return engine;
    }

    public void setEngine(String engine) {
        this.engine = engine;
        updateQualifiedNames();
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
        updateQualifiedNames();
    }

    public String getBinary() {
        return binary;
    }

    public void setBinary(String binary) {
        this.binary = binary;
        updateQualifiedNames();
    }

    public String getFullyQualifiedName() {
        return fullyQualifiedName;
    }

    public void setFullyQualifiedName(String fullyQualifiedName) {
        this.fullyQualifiedName = fullyQualifiedName;
    }

    public String getProjectQualifiedName() {
        return projectQualifiedName;
    }

    public void setProjectQualifiedName(String projectQualifiedName) {
        this.projectQualifiedName = projectQualifiedName;
    }

    public LocalDateTime getCreated() {
        return created;
    }

    public void setCreated(LocalDateTime created) {
        this.created = created;
    }

    public LocalDateTime getUpdated() {
        return updated;
    }

    public void setUpdated(LocalDateTime updated) {
        this.updated = updated;
    }

    // Business logic methods
    private void updateQualifiedNames() {
        if (project != null && binary != null) {
            this.projectQualifiedName = generateProjectQualifiedName(project, binary);
        }
        if (engine != null && projectQualifiedName != null) {
            this.fullyQualifiedName = generateFullyQualifiedName(engine, project, binary);
        }
    }

    /**
     * Generate the fully qualified name for this fuzz target.
     * Format: engine_project_binary (with special chars normalized)
     */
    public static String generateFullyQualifiedName(String engine, String project, String binary) {
        if (engine == null || project == null || binary == null) {
            return null;
        }
        return engine + "_" + generateProjectQualifiedName(project, binary);
    }

    /**
     * Generate the project qualified name for this fuzz target.
     * Format: project_binary (with special chars normalized)
     */
    public static String generateProjectQualifiedName(String project, String binary) {
        if (project == null || binary == null) {
            return null;
        }
        String normalizedProject = normalizeName(project);
        String normalizedBinary = normalizeName(binary);
        return normalizedProject + "_" + normalizedBinary;
    }

    /**
     * Normalize a name by replacing special characters with hyphens.
     * This is important for storage paths and identifiers.
     */
    public static String normalizeName(String name) {
        if (name == null) {
            return null;
        }
        return SPECIAL_CHARS_PATTERN.matcher(name).replaceAll("-").replaceAll("^-+|-+$", "");
    }

    /**
     * Update the qualified names based on current engine, project, and binary values.
     * This should be called whenever any of these fields change.
     */
    public void refreshQualifiedNames() {
        updateQualifiedNames();
    }

    // JPA lifecycle callbacks
    @PrePersist
    @PreUpdate
    private void prePersist() {
        updateQualifiedNames();
    }

    // Utility methods
    public boolean isValid() {
        return engine != null && !engine.trim().isEmpty() &&
               project != null && !project.trim().isEmpty() &&
               binary != null && !binary.trim().isEmpty();
    }

    public String getDisplayName() {
        return projectQualifiedName != null ? projectQualifiedName : 
               (project + "_" + binary);
    }

    // equals, hashCode, and toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FuzzTarget that = (FuzzTarget) o;
        return Objects.equals(id, that.id) && 
               Objects.equals(fullyQualifiedName, that.fullyQualifiedName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, fullyQualifiedName);
    }

    @Override
    public String toString() {
        return "FuzzTarget{" +
                "id=" + id +
                ", engine='" + engine + '\'' +
                ", project='" + project + '\'' +
                ", binary='" + binary + '\'' +
                ", fullyQualifiedName='" + fullyQualifiedName + '\'' +
                ", projectQualifiedName='" + projectQualifiedName + '\'' +
                ", created=" + created +
                '}';
    }
}