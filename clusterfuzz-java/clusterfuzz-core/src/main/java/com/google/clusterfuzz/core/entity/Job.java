package com.google.clusterfuzz.core.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * Definition of a job type used by the bots.
 * This is the Java equivalent of the Python Job model.
 */
@Entity
@Table(name = "jobs", indexes = {
    @Index(name = "idx_job_name", columnList = "name", unique = true),
    @Index(name = "idx_job_platform", columnList = "platform"),
    @Index(name = "idx_job_project", columnList = "project")
})
@EntityListeners(AuditingEntityListener.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Job {

    // Valid name regex pattern (alphanumeric, underscore, hyphen)
    public static final String VALID_NAME_REGEX = "^[a-zA-Z0-9_-]+$";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Job name is required")
    @Pattern(regexp = VALID_NAME_REGEX, message = "Job name must contain only alphanumeric characters, underscores, and hyphens")
    @Column(name = "name", unique = true, nullable = false)
    private String name;

    @Lob
    @Column(name = "environment_string")
    private String environmentString;

    @Column(name = "platform")
    private String platform;

    @Column(name = "custom_binary_key")
    private String customBinaryKey;

    @Column(name = "custom_binary_filename")
    private String customBinaryFilename;

    @Column(name = "custom_binary_revision")
    private Long customBinaryRevision;

    @Lob
    @Column(name = "description")
    private String description;

    @ElementCollection
    @CollectionTable(name = "job_templates", joinColumns = @JoinColumn(name = "job_id"))
    @Column(name = "template")
    private List<String> templates;

    @Column(name = "project")
    private String project;

    @ElementCollection
    @CollectionTable(name = "job_keywords", joinColumns = @JoinColumn(name = "job_id"))
    @Column(name = "keyword")
    private List<String> keywords;

    @Column(name = "external_reproduction_topic")
    private String externalReproductionTopic;

    @Column(name = "external_updates_subscription")
    private String externalUpdatesSubscription;

    @CreatedDate
    @Column(name = "created", updatable = false)
    private LocalDateTime created;

    @LastModifiedDate
    @Column(name = "updated")
    private LocalDateTime updated;

    // Constructors
    public Job() {
    }

    public Job(String name, String platform, String project) {
        this.name = name;
        this.platform = platform;
        this.project = project;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEnvironmentString() {
        return environmentString;
    }

    public void setEnvironmentString(String environmentString) {
        this.environmentString = environmentString;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getCustomBinaryKey() {
        return customBinaryKey;
    }

    public void setCustomBinaryKey(String customBinaryKey) {
        this.customBinaryKey = customBinaryKey;
    }

    public String getCustomBinaryFilename() {
        return customBinaryFilename;
    }

    public void setCustomBinaryFilename(String customBinaryFilename) {
        this.customBinaryFilename = customBinaryFilename;
    }

    public Long getCustomBinaryRevision() {
        return customBinaryRevision;
    }

    public void setCustomBinaryRevision(Long customBinaryRevision) {
        this.customBinaryRevision = customBinaryRevision;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getTemplates() {
        return templates;
    }

    public void setTemplates(List<String> templates) {
        this.templates = templates;
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }

    public String getExternalReproductionTopic() {
        return externalReproductionTopic;
    }

    public void setExternalReproductionTopic(String externalReproductionTopic) {
        this.externalReproductionTopic = externalReproductionTopic;
    }

    public String getExternalUpdatesSubscription() {
        return externalUpdatesSubscription;
    }

    public void setExternalUpdatesSubscription(String externalUpdatesSubscription) {
        this.externalUpdatesSubscription = externalUpdatesSubscription;
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

    // Utility methods
    public boolean isExternal() {
        return (externalReproductionTopic != null && !externalReproductionTopic.isEmpty()) ||
               (externalUpdatesSubscription != null && !externalUpdatesSubscription.isEmpty());
    }

    public boolean hasCustomBinary() {
        return customBinaryKey != null && !customBinaryKey.isEmpty();
    }

    public boolean hasTemplates() {
        return templates != null && !templates.isEmpty();
    }

    // equals, hashCode, and toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Job job = (Job) o;
        return Objects.equals(id, job.id) && Objects.equals(name, job.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }

    @Override
    public String toString() {
        return "Job{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", platform='" + platform + '\'' +
                ", project='" + project + '\'' +
                ", isExternal=" + isExternal() +
                ", hasCustomBinary=" + hasCustomBinary() +
                ", created=" + created +
                '}';
    }
}