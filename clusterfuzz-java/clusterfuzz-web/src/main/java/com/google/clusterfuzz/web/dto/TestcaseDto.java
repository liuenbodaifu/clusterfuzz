package com.google.clusterfuzz.web.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Data Transfer Object for Testcase entity.
 * Used for API requests and responses.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Testcase information")
public class TestcaseDto {

    @Schema(description = "Unique identifier", example = "1")
    private Long id;

    @NotBlank(message = "Crash type is required")
    @Schema(description = "Type of crash", example = "Heap-buffer-overflow")
    private String crashType;

    @Schema(description = "Crash address", example = "0x7fff12345678")
    private String crashAddress;

    @Schema(description = "Crash state (first few stack frames)", example = "main\\ntest_function\\n")
    private String crashState;

    @Schema(description = "Complete crash stacktrace")
    private String crashStacktrace;

    @Schema(description = "Last tested crash stacktrace")
    private String lastTestedCrashStacktrace;

    @Schema(description = "Blobstore keys for fuzzed testcase files")
    private String fuzzedKeys;

    @Schema(description = "Blobstore keys for minimized testcase files")
    private String minimizedKeys;

    @Schema(description = "Bug tracking information")
    private String bugInformation;

    @Schema(description = "Regression range information")
    private String regression;

    @Schema(description = "Fix information")
    private String fixed;

    @Schema(description = "Whether this is a security bug")
    private Boolean securityFlag;

    @Schema(description = "Security severity level (1-5)", example = "3")
    private Integer securitySeverity;

    @Schema(description = "Whether this is a one-time crasher")
    private Boolean oneTimeCrasherFlag;

    @Schema(description = "Additional comments")
    private String comments;

    @Schema(description = "Revision where crash was discovered")
    private Long crashRevision;

    @Schema(description = "Absolute path to testcase file")
    private String absolutePath;

    @Schema(description = "Minimized command line arguments")
    private String minimizedArguments;

    @NotBlank(message = "Job type is required")
    @Schema(description = "Type of job that found this testcase", example = "libfuzzer_asan_chrome")
    private String jobType;

    @Schema(description = "Queue used for processing")
    private String queue;

    @Schema(description = "Archive state")
    private Integer archiveState;

    @Schema(description = "Archive filename")
    private String archiveFilename;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "Timestamp when testcase was created")
    private LocalDateTime timestamp;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "Creation timestamp")
    private LocalDateTime created;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "Last update timestamp")
    private LocalDateTime updated;

    @Schema(description = "Whether the crash stack varies between crashes")
    private Boolean flakyStack;

    @Schema(description = "Whether HTTP server is needed for reproduction")
    private Boolean httpFlag;

    @NotBlank(message = "Fuzzer name is required")
    @Schema(description = "Name of the fuzzer that found this testcase", example = "libfuzzer")
    private String fuzzerName;

    @Schema(description = "Processing status", example = "Processed")
    private String status;

    @Schema(description = "ID of testcase this is a duplicate of")
    private Long duplicateOf;

    @Schema(description = "Whether stacktrace has been symbolized")
    private Boolean symbolized;

    @Schema(description = "Group ID for related testcases")
    private Long groupId;

    @Schema(description = "Bug information for the group")
    private Long groupBugInformation;

    @Schema(description = "User interaction gestures")
    private List<String> gestures;

    @Schema(description = "ASAN redzone size in bytes", example = "128")
    private Integer redzone;

    @Schema(description = "Whether UBSan detection is disabled")
    private Boolean disableUbsan;

    @Schema(description = "Whether testcase is open")
    private Boolean open;

    @Schema(description = "Timeout multiplier", example = "1.0")
    private Double timeoutMultiplier;

    @Schema(description = "Additional metadata as JSON")
    private String additionalMetadata;

    @Schema(description = "Whether testcase has been triaged")
    private Boolean triaged;

    @Schema(description = "Project name", example = "chromium")
    private String projectName;

    @Schema(description = "Search keywords")
    private List<String> keywords;

    @Schema(description = "Whether testcase has associated bug")
    private Boolean hasBugFlag;

    @Schema(description = "Bug indices for searching")
    private List<String> bugIndices;

    @Schema(description = "Overridden fuzzer name")
    private String overriddenFuzzerName;

    @Schema(description = "Platform", example = "linux")
    private String platform;

    @Schema(description = "Platform ID with version info", example = "linux:ubuntu:20.04")
    private String platformId;

    @Schema(description = "Impact indices for searching")
    private List<String> impactIndices;

    @Schema(description = "Whether this testcase is a duplicate")
    private Boolean isADuplicateFlag;

    @Schema(description = "Whether this testcase is a group leader")
    private Boolean isLeader;

    // Constructors
    public TestcaseDto() {
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCrashType() {
        return crashType;
    }

    public void setCrashType(String crashType) {
        this.crashType = crashType;
    }

    public String getCrashAddress() {
        return crashAddress;
    }

    public void setCrashAddress(String crashAddress) {
        this.crashAddress = crashAddress;
    }

    public String getCrashState() {
        return crashState;
    }

    public void setCrashState(String crashState) {
        this.crashState = crashState;
    }

    public String getCrashStacktrace() {
        return crashStacktrace;
    }

    public void setCrashStacktrace(String crashStacktrace) {
        this.crashStacktrace = crashStacktrace;
    }

    public String getLastTestedCrashStacktrace() {
        return lastTestedCrashStacktrace;
    }

    public void setLastTestedCrashStacktrace(String lastTestedCrashStacktrace) {
        this.lastTestedCrashStacktrace = lastTestedCrashStacktrace;
    }

    public String getFuzzedKeys() {
        return fuzzedKeys;
    }

    public void setFuzzedKeys(String fuzzedKeys) {
        this.fuzzedKeys = fuzzedKeys;
    }

    public String getMinimizedKeys() {
        return minimizedKeys;
    }

    public void setMinimizedKeys(String minimizedKeys) {
        this.minimizedKeys = minimizedKeys;
    }

    public String getBugInformation() {
        return bugInformation;
    }

    public void setBugInformation(String bugInformation) {
        this.bugInformation = bugInformation;
    }

    public String getRegression() {
        return regression;
    }

    public void setRegression(String regression) {
        this.regression = regression;
    }

    public String getFixed() {
        return fixed;
    }

    public void setFixed(String fixed) {
        this.fixed = fixed;
    }

    public Boolean getSecurityFlag() {
        return securityFlag;
    }

    public void setSecurityFlag(Boolean securityFlag) {
        this.securityFlag = securityFlag;
    }

    public Integer getSecuritySeverity() {
        return securitySeverity;
    }

    public void setSecuritySeverity(Integer securitySeverity) {
        this.securitySeverity = securitySeverity;
    }

    public Boolean getOneTimeCrasherFlag() {
        return oneTimeCrasherFlag;
    }

    public void setOneTimeCrasherFlag(Boolean oneTimeCrasherFlag) {
        this.oneTimeCrasherFlag = oneTimeCrasherFlag;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public Long getCrashRevision() {
        return crashRevision;
    }

    public void setCrashRevision(Long crashRevision) {
        this.crashRevision = crashRevision;
    }

    public String getAbsolutePath() {
        return absolutePath;
    }

    public void setAbsolutePath(String absolutePath) {
        this.absolutePath = absolutePath;
    }

    public String getMinimizedArguments() {
        return minimizedArguments;
    }

    public void setMinimizedArguments(String minimizedArguments) {
        this.minimizedArguments = minimizedArguments;
    }

    public String getJobType() {
        return jobType;
    }

    public void setJobType(String jobType) {
        this.jobType = jobType;
    }

    public String getQueue() {
        return queue;
    }

    public void setQueue(String queue) {
        this.queue = queue;
    }

    public Integer getArchiveState() {
        return archiveState;
    }

    public void setArchiveState(Integer archiveState) {
        this.archiveState = archiveState;
    }

    public String getArchiveFilename() {
        return archiveFilename;
    }

    public void setArchiveFilename(String archiveFilename) {
        this.archiveFilename = archiveFilename;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
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

    public Boolean getFlakyStack() {
        return flakyStack;
    }

    public void setFlakyStack(Boolean flakyStack) {
        this.flakyStack = flakyStack;
    }

    public Boolean getHttpFlag() {
        return httpFlag;
    }

    public void setHttpFlag(Boolean httpFlag) {
        this.httpFlag = httpFlag;
    }

    public String getFuzzerName() {
        return fuzzerName;
    }

    public void setFuzzerName(String fuzzerName) {
        this.fuzzerName = fuzzerName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getDuplicateOf() {
        return duplicateOf;
    }

    public void setDuplicateOf(Long duplicateOf) {
        this.duplicateOf = duplicateOf;
    }

    public Boolean getSymbolized() {
        return symbolized;
    }

    public void setSymbolized(Boolean symbolized) {
        this.symbolized = symbolized;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public Long getGroupBugInformation() {
        return groupBugInformation;
    }

    public void setGroupBugInformation(Long groupBugInformation) {
        this.groupBugInformation = groupBugInformation;
    }

    public List<String> getGestures() {
        return gestures;
    }

    public void setGestures(List<String> gestures) {
        this.gestures = gestures;
    }

    public Integer getRedzone() {
        return redzone;
    }

    public void setRedzone(Integer redzone) {
        this.redzone = redzone;
    }

    public Boolean getDisableUbsan() {
        return disableUbsan;
    }

    public void setDisableUbsan(Boolean disableUbsan) {
        this.disableUbsan = disableUbsan;
    }

    public Boolean getOpen() {
        return open;
    }

    public void setOpen(Boolean open) {
        this.open = open;
    }

    public Double getTimeoutMultiplier() {
        return timeoutMultiplier;
    }

    public void setTimeoutMultiplier(Double timeoutMultiplier) {
        this.timeoutMultiplier = timeoutMultiplier;
    }

    public String getAdditionalMetadata() {
        return additionalMetadata;
    }

    public void setAdditionalMetadata(String additionalMetadata) {
        this.additionalMetadata = additionalMetadata;
    }

    public Boolean getTriaged() {
        return triaged;
    }

    public void setTriaged(Boolean triaged) {
        this.triaged = triaged;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }

    public Boolean getHasBugFlag() {
        return hasBugFlag;
    }

    public void setHasBugFlag(Boolean hasBugFlag) {
        this.hasBugFlag = hasBugFlag;
    }

    public List<String> getBugIndices() {
        return bugIndices;
    }

    public void setBugIndices(List<String> bugIndices) {
        this.bugIndices = bugIndices;
    }

    public String getOverriddenFuzzerName() {
        return overriddenFuzzerName;
    }

    public void setOverriddenFuzzerName(String overriddenFuzzerName) {
        this.overriddenFuzzerName = overriddenFuzzerName;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getPlatformId() {
        return platformId;
    }

    public void setPlatformId(String platformId) {
        this.platformId = platformId;
    }

    public List<String> getImpactIndices() {
        return impactIndices;
    }

    public void setImpactIndices(List<String> impactIndices) {
        this.impactIndices = impactIndices;
    }

    public Boolean getIsADuplicateFlag() {
        return isADuplicateFlag;
    }

    public void setIsADuplicateFlag(Boolean isADuplicateFlag) {
        this.isADuplicateFlag = isADuplicateFlag;
    }

    public Boolean getIsLeader() {
        return isLeader;
    }

    public void setIsLeader(Boolean isLeader) {
        this.isLeader = isLeader;
    }
}