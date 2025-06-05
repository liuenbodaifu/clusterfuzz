package com.google.clusterfuzz.core.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Metadata associated with a particular archived build.
 * Tracks build information, revision details, and build status.
 */
@Entity
@Table(name = "build_metadata", indexes = {
    @Index(name = "idx_build_metadata_job_type", columnList = "jobType"),
    @Index(name = "idx_build_metadata_revision", columnList = "revision"),
    @Index(name = "idx_build_metadata_timestamp", columnList = "timestamp"),
    @Index(name = "idx_build_metadata_bad_build", columnList = "badBuild"),
    @Index(name = "idx_build_metadata_bot_name", columnList = "botName")
})
@EntityListeners(AuditingEntityListener.class)
public class BuildMetadata {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Job type that this build belongs to.
     */
    @Column(name = "job_type", length = 255)
    private String jobType;

    /**
     * Revision of the build.
     */
    @Column(name = "revision")
    private Integer revision;

    /**
     * Good build or bad build.
     */
    @Column(name = "bad_build", nullable = false)
    private Boolean badBuild = false;

    /**
     * Stdout and stderr from build process.
     */
    @Lob
    @Column(name = "console_output", columnDefinition = "TEXT")
    private String consoleOutput;

    /**
     * Bot name that created this build.
     */
    @Column(name = "bot_name", length = 255)
    private String botName;

    /**
     * Symbol data for debugging.
     */
    @Column(name = "symbols", length = 500)
    private String symbols;

    /**
     * Creation timestamp.
     */
    @CreatedDate
    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    // Constructors
    public BuildMetadata() {}

    public BuildMetadata(String jobType, Integer revision) {
        this.jobType = jobType;
        this.revision = revision;
        this.badBuild = false;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getJobType() {
        return jobType;
    }

    public void setJobType(String jobType) {
        this.jobType = jobType;
    }

    public Integer getRevision() {
        return revision;
    }

    public void setRevision(Integer revision) {
        this.revision = revision;
    }

    public Boolean getBadBuild() {
        return badBuild;
    }

    public void setBadBuild(Boolean badBuild) {
        this.badBuild = badBuild;
    }

    public String getConsoleOutput() {
        return consoleOutput;
    }

    public void setConsoleOutput(String consoleOutput) {
        this.consoleOutput = consoleOutput;
    }

    public String getBotName() {
        return botName;
    }

    public void setBotName(String botName) {
        this.botName = botName;
    }

    public String getSymbols() {
        return symbols;
    }

    public void setSymbols(String symbols) {
        this.symbols = symbols;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    // Utility methods
    public boolean isGoodBuild() {
        return !Boolean.TRUE.equals(badBuild);
    }

    public void markAsBadBuild() {
        this.badBuild = true;
    }

    public void markAsGoodBuild() {
        this.badBuild = false;
    }

    public boolean hasConsoleOutput() {
        return consoleOutput != null && !consoleOutput.trim().isEmpty();
    }

    public boolean hasSymbols() {
        return symbols != null && !symbols.trim().isEmpty();
    }

    @Override
    public String toString() {
        return "BuildMetadata{" +
                "id=" + id +
                ", jobType='" + jobType + '\'' +
                ", revision=" + revision +
                ", badBuild=" + badBuild +
                ", botName='" + botName + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BuildMetadata)) return false;
        BuildMetadata that = (BuildMetadata) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}