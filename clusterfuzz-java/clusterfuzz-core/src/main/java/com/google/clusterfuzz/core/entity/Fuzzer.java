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
 * Represents a fuzzer in the ClusterFuzz system.
 * This is the Java equivalent of the Python Fuzzer model.
 */
@Entity
@Table(name = "fuzzers", indexes = {
    @Index(name = "idx_fuzzer_name", columnList = "name", unique = true),
    @Index(name = "idx_fuzzer_source", columnList = "source"),
    @Index(name = "idx_fuzzer_builtin", columnList = "builtin"),
    @Index(name = "idx_fuzzer_differential", columnList = "differential"),
    @Index(name = "idx_fuzzer_external_contribution", columnList = "externalContribution")
})
@EntityListeners(AuditingEntityListener.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Fuzzer {

    // Valid name regex pattern (allows alphanumeric, underscore, dot, at, hyphen)
    public static final String VALID_NAME_REGEX = "^[a-zA-Z0-9_@.-]+$";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Fuzzer name is required")
    @Pattern(regexp = VALID_NAME_REGEX, message = "Fuzzer name must contain only alphanumeric characters, underscores, dots, at signs, and hyphens")
    @Column(name = "name", unique = true, nullable = false)
    private String name;

    @Column(name = "timestamp")
    private LocalDateTime timestamp;

    @Column(name = "filename")
    private String filename;

    @Column(name = "blobstore_key")
    private String blobstoreKey;

    @Column(name = "file_size")
    private String fileSize;

    @Column(name = "executable_path")
    private String executablePath;

    @Column(name = "revision")
    private Long revision;

    @Column(name = "source")
    private String source;

    @Column(name = "timeout")
    private Integer timeout;

    @Column(name = "supported_platforms")
    private String supportedPlatforms;

    @Column(name = "launcher_script")
    private String launcherScript;

    @Column(name = "result")
    private String result;

    @Column(name = "result_timestamp")
    private LocalDateTime resultTimestamp;

    @Lob
    @Column(name = "console_output")
    private String consoleOutput;

    @Column(name = "return_code")
    private Integer returnCode;

    @Column(name = "sample_testcase")
    private String sampleTestcase;

    @ElementCollection
    @CollectionTable(name = "fuzzer_jobs", joinColumns = @JoinColumn(name = "fuzzer_id"))
    @Column(name = "job")
    private List<String> jobs;

    @Column(name = "external_contribution")
    private Boolean externalContribution = false;

    @Column(name = "max_testcases")
    private Integer maxTestcases;

    @Column(name = "untrusted_content")
    private Boolean untrustedContent = false;

    @Column(name = "data_bundle_name")
    private String dataBundleName = "";

    @Lob
    @Column(name = "additional_environment_string")
    private String additionalEnvironmentString;

    @Lob
    @Column(name = "stats_columns")
    private String statsColumns;

    @Lob
    @Column(name = "stats_column_descriptions")
    private String statsColumnDescriptions;

    @Column(name = "builtin")
    private Boolean builtin = false;

    @Column(name = "differential")
    private Boolean differential = false;

    @Column(name = "has_large_testcases")
    private Boolean hasLargeTestcases = false;

    @CreatedDate
    @Column(name = "created", updatable = false)
    private LocalDateTime created;

    @LastModifiedDate
    @Column(name = "updated")
    private LocalDateTime updated;

    // Constructors
    public Fuzzer() {
        this.timestamp = LocalDateTime.now();
    }

    public Fuzzer(String name, String source) {
        this();
        this.name = name;
        this.source = source;
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

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getBlobstoreKey() {
        return blobstoreKey;
    }

    public void setBlobstoreKey(String blobstoreKey) {
        this.blobstoreKey = blobstoreKey;
    }

    public String getFileSize() {
        return fileSize;
    }

    public void setFileSize(String fileSize) {
        this.fileSize = fileSize;
    }

    public String getExecutablePath() {
        return executablePath;
    }

    public void setExecutablePath(String executablePath) {
        this.executablePath = executablePath;
    }

    public Long getRevision() {
        return revision;
    }

    public void setRevision(Long revision) {
        this.revision = revision;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public Integer getTimeout() {
        return timeout;
    }

    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }

    public String getSupportedPlatforms() {
        return supportedPlatforms;
    }

    public void setSupportedPlatforms(String supportedPlatforms) {
        this.supportedPlatforms = supportedPlatforms;
    }

    public String getLauncherScript() {
        return launcherScript;
    }

    public void setLauncherScript(String launcherScript) {
        this.launcherScript = launcherScript;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public LocalDateTime getResultTimestamp() {
        return resultTimestamp;
    }

    public void setResultTimestamp(LocalDateTime resultTimestamp) {
        this.resultTimestamp = resultTimestamp;
    }

    public String getConsoleOutput() {
        return consoleOutput;
    }

    public void setConsoleOutput(String consoleOutput) {
        this.consoleOutput = consoleOutput;
    }

    public Integer getReturnCode() {
        return returnCode;
    }

    public void setReturnCode(Integer returnCode) {
        this.returnCode = returnCode;
    }

    public String getSampleTestcase() {
        return sampleTestcase;
    }

    public void setSampleTestcase(String sampleTestcase) {
        this.sampleTestcase = sampleTestcase;
    }

    public List<String> getJobs() {
        return jobs;
    }

    public void setJobs(List<String> jobs) {
        this.jobs = jobs;
    }

    public Boolean getExternalContribution() {
        return externalContribution;
    }

    public void setExternalContribution(Boolean externalContribution) {
        this.externalContribution = externalContribution;
    }

    public Integer getMaxTestcases() {
        return maxTestcases;
    }

    public void setMaxTestcases(Integer maxTestcases) {
        this.maxTestcases = maxTestcases;
    }

    public Boolean getUntrustedContent() {
        return untrustedContent;
    }

    public void setUntrustedContent(Boolean untrustedContent) {
        this.untrustedContent = untrustedContent;
    }

    public String getDataBundleName() {
        return dataBundleName;
    }

    public void setDataBundleName(String dataBundleName) {
        this.dataBundleName = dataBundleName;
    }

    public String getAdditionalEnvironmentString() {
        return additionalEnvironmentString;
    }

    public void setAdditionalEnvironmentString(String additionalEnvironmentString) {
        this.additionalEnvironmentString = additionalEnvironmentString;
    }

    public String getStatsColumns() {
        return statsColumns;
    }

    public void setStatsColumns(String statsColumns) {
        this.statsColumns = statsColumns;
    }

    public String getStatsColumnDescriptions() {
        return statsColumnDescriptions;
    }

    public void setStatsColumnDescriptions(String statsColumnDescriptions) {
        this.statsColumnDescriptions = statsColumnDescriptions;
    }

    public Boolean getBuiltin() {
        return builtin;
    }

    public void setBuiltin(Boolean builtin) {
        this.builtin = builtin;
    }

    public Boolean getDifferential() {
        return differential;
    }

    public void setDifferential(Boolean differential) {
        this.differential = differential;
    }

    public Boolean getHasLargeTestcases() {
        return hasLargeTestcases;
    }

    public void setHasLargeTestcases(Boolean hasLargeTestcases) {
        this.hasLargeTestcases = hasLargeTestcases;
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
    public boolean isBuiltin() {
        return Boolean.TRUE.equals(builtin);
    }

    public boolean isDifferential() {
        return Boolean.TRUE.equals(differential);
    }

    public boolean isExternalContribution() {
        return Boolean.TRUE.equals(externalContribution);
    }

    public boolean hasUntrustedContent() {
        return Boolean.TRUE.equals(untrustedContent);
    }

    public boolean hasLargeTestcases() {
        return Boolean.TRUE.equals(hasLargeTestcases);
    }

    public boolean hasCustomLauncher() {
        return launcherScript != null && !launcherScript.isEmpty();
    }

    public boolean hasDataBundle() {
        return dataBundleName != null && !dataBundleName.isEmpty();
    }

    public boolean hasJobs() {
        return jobs != null && !jobs.isEmpty();
    }

    // equals, hashCode, and toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Fuzzer fuzzer = (Fuzzer) o;
        return Objects.equals(id, fuzzer.id) && Objects.equals(name, fuzzer.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }

    @Override
    public String toString() {
        return "Fuzzer{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", source='" + source + '\'' +
                ", revision=" + revision +
                ", builtin=" + builtin +
                ", differential=" + differential +
                ", externalContribution=" + externalContribution +
                ", created=" + created +
                '}';
    }
}