package com.google.clusterfuzz.web.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.clusterfuzz.core.entity.TestCase;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for TestCase entity.
 * Used for API requests and responses.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "TestCase data transfer object")
public class TestCaseDto {

    @Schema(description = "Unique identifier", example = "12345")
    private Long id;

    @NotBlank(message = "Crash type is required")
    @Schema(description = "Type of crash", example = "ASSERT", required = true)
    private String crashType;

    @NotBlank(message = "Crash state is required")
    @Schema(description = "Crash state signature", example = "NULL_DEREFERENCE", required = true)
    private String crashState;

    @Schema(description = "Security flag indicating if this is a security issue", example = "true")
    private Boolean securityFlag;

    @NotBlank(message = "Status is required")
    @Schema(description = "Current status", example = "Processed", required = true)
    private String status;

    @NotBlank(message = "Job type is required")
    @Schema(description = "Job type that found this testcase", example = "libfuzzer_chrome_asan", required = true)
    private String jobType;

    @Schema(description = "Fuzzer name that generated this testcase", example = "libFuzzer")
    private String fuzzerName;

    @NotBlank(message = "Platform is required")
    @Schema(description = "Platform where crash occurred", example = "linux", required = true)
    private String platform;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "Timestamp when testcase was created", example = "2025-06-05T10:30:00")
    private LocalDateTime timestamp;

    @Schema(description = "Stack trace of the crash")
    private String stacktrace;

    @Schema(description = "Key for reproducer file in storage")
    private String reproducerKey;

    @Schema(description = "Keys for minimized testcase files")
    private String minimizedKeys;

    @Schema(description = "Revision where crash was found", example = "123456")
    private Integer crashRevision;

    @Schema(description = "Additional comments")
    private String comments;

    @Schema(description = "Bug information from issue tracker")
    private String bugInformation;

    @Schema(description = "Group bug information")
    private String groupBugInformation;

    @Schema(description = "Whether issue tracker entry exists", example = "true")
    private Boolean hasIssueTracker;

    @Schema(description = "Issue tracker issue ID", example = "ISSUE-123")
    private String issueTrackerIssueId;

    @Schema(description = "Whether this is a one-time crasher", example = "false")
    private Boolean oneTimeCrasherFlag;

    @Schema(description = "Group ID for related testcases", example = "67890")
    private Long groupId;

    @Schema(description = "Project name", example = "chromium")
    private String projectName;

    @Schema(description = "Impact on extended stable version", example = "YES")
    private String impactExtendedStableVersion;

    @Schema(description = "Impact on stable version", example = "YES")
    private String impactStableVersion;

    @Schema(description = "Impact on beta version", example = "YES")
    private String impactBetaVersion;

    @Schema(description = "Impact on head version", example = "YES")
    private String impactHeadVersion;

    @Schema(description = "Whether impact is set", example = "true")
    private Boolean isImpactSetFlag;

    @Schema(description = "Whether testcase is open", example = "true")
    private Boolean open;

    @Schema(description = "Whether testcase is fixed", example = "false")
    private Boolean fixed;

    @Schema(description = "Whether this is a regression", example = "false")
    private Boolean regression;

    @Schema(description = "Fixed revision", example = "123457")
    private Integer fixedRevision;

    @Schema(description = "Regression range", example = "123400:123456")
    private String regressionRange;

    @Schema(description = "Absolute path to testcase file")
    private String absolutePath;

    @Schema(description = "Archive filename")
    private String archiveFilename;

    @Schema(description = "Archive state", example = "ARCHIVED")
    private String archiveState;

    @Schema(description = "Binary flag", example = "false")
    private Boolean binaryFlag;

    @Schema(description = "Duplicate of another testcase ID")
    private Long duplicateOf;

    @Schema(description = "Flaky stack flag", example = "false")
    private Boolean flakyStack;

    @Schema(description = "Gestures used for reproduction")
    private String gestures;

    @Schema(description = "HTTP flag for web-based crashes", example = "false")
    private Boolean httpFlag;

    @Schema(description = "Last tested crash revision", example = "123456")
    private Integer lastTestedCrashRevision;

    @Schema(description = "Minimized arguments")
    private String minimizedArguments;

    @Schema(description = "Original crash revision", example = "123456")
    private Integer originalCrashRevision;

    @Schema(description = "Overridden fuzzer name")
    private String overriddenFuzzerName;

    @Schema(description = "Queue name for processing")
    private String queue;

    @Schema(description = "Redzone size", example = "32")
    private Integer redzone;

    @Schema(description = "Symbolized flag", example = "true")
    private Boolean symbolized;

    @Schema(description = "Triaged flag", example = "true")
    private Boolean triaged;

    @Schema(description = "Uploader email")
    private String uploaderEmail;

    @Schema(description = "Window argument")
    private String windowArgument;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "Last modified timestamp")
    private LocalDateTime lastModified;

    /**
     * Convert DTO to entity.
     */
    public TestCase toEntity() {
        return TestCase.builder()
                .id(this.id)
                .crashType(this.crashType)
                .crashState(this.crashState)
                .securityFlag(this.securityFlag)
                .status(this.status)
                .jobType(this.jobType)
                .fuzzerName(this.fuzzerName)
                .platform(this.platform)
                .timestamp(this.timestamp)
                .stacktrace(this.stacktrace)
                .reproducerKey(this.reproducerKey)
                .minimizedKeys(this.minimizedKeys)
                .crashRevision(this.crashRevision)
                .comments(this.comments)
                .bugInformation(this.bugInformation)
                .groupBugInformation(this.groupBugInformation)
                .hasIssueTracker(this.hasIssueTracker)
                .issueTrackerIssueId(this.issueTrackerIssueId)
                .oneTimeCrasherFlag(this.oneTimeCrasherFlag)
                .groupId(this.groupId)
                .projectName(this.projectName)
                .impactExtendedStableVersion(this.impactExtendedStableVersion)
                .impactStableVersion(this.impactStableVersion)
                .impactBetaVersion(this.impactBetaVersion)
                .impactHeadVersion(this.impactHeadVersion)
                .isImpactSetFlag(this.isImpactSetFlag)
                .open(this.open)
                .fixed(this.fixed)
                .regression(this.regression)
                .fixedRevision(this.fixedRevision)
                .regressionRange(this.regressionRange)
                .absolutePath(this.absolutePath)
                .archiveFilename(this.archiveFilename)
                .archiveState(this.archiveState)
                .binaryFlag(this.binaryFlag)
                .duplicateOf(this.duplicateOf)
                .flakyStack(this.flakyStack)
                .gestures(this.gestures)
                .httpFlag(this.httpFlag)
                .lastTestedCrashRevision(this.lastTestedCrashRevision)
                .minimizedArguments(this.minimizedArguments)
                .originalCrashRevision(this.originalCrashRevision)
                .overriddenFuzzerName(this.overriddenFuzzerName)
                .queue(this.queue)
                .redzone(this.redzone)
                .symbolized(this.symbolized)
                .triaged(this.triaged)
                .uploaderEmail(this.uploaderEmail)
                .windowArgument(this.windowArgument)
                .lastModified(this.lastModified)
                .build();
    }

    /**
     * Convert entity to DTO.
     */
    public static TestCaseDto fromEntity(TestCase testCase) {
        return TestCaseDto.builder()
                .id(testCase.getId())
                .crashType(testCase.getCrashType())
                .crashState(testCase.getCrashState())
                .securityFlag(testCase.getSecurityFlag())
                .status(testCase.getStatus())
                .jobType(testCase.getJobType())
                .fuzzerName(testCase.getFuzzerName())
                .platform(testCase.getPlatform())
                .timestamp(testCase.getTimestamp())
                .stacktrace(testCase.getStacktrace())
                .reproducerKey(testCase.getReproducerKey())
                .minimizedKeys(testCase.getMinimizedKeys())
                .crashRevision(testCase.getCrashRevision())
                .comments(testCase.getComments())
                .bugInformation(testCase.getBugInformation())
                .groupBugInformation(testCase.getGroupBugInformation())
                .hasIssueTracker(testCase.getHasIssueTracker())
                .issueTrackerIssueId(testCase.getIssueTrackerIssueId())
                .oneTimeCrasherFlag(testCase.getOneTimeCrasherFlag())
                .groupId(testCase.getGroupId())
                .projectName(testCase.getProjectName())
                .impactExtendedStableVersion(testCase.getImpactExtendedStableVersion())
                .impactStableVersion(testCase.getImpactStableVersion())
                .impactBetaVersion(testCase.getImpactBetaVersion())
                .impactHeadVersion(testCase.getImpactHeadVersion())
                .isImpactSetFlag(testCase.getIsImpactSetFlag())
                .open(testCase.getOpen())
                .fixed(testCase.getFixed())
                .regression(testCase.getRegression())
                .fixedRevision(testCase.getFixedRevision())
                .regressionRange(testCase.getRegressionRange())
                .absolutePath(testCase.getAbsolutePath())
                .archiveFilename(testCase.getArchiveFilename())
                .archiveState(testCase.getArchiveState())
                .binaryFlag(testCase.getBinaryFlag())
                .duplicateOf(testCase.getDuplicateOf())
                .flakyStack(testCase.getFlakyStack())
                .gestures(testCase.getGestures())
                .httpFlag(testCase.getHttpFlag())
                .lastTestedCrashRevision(testCase.getLastTestedCrashRevision())
                .minimizedArguments(testCase.getMinimizedArguments())
                .originalCrashRevision(testCase.getOriginalCrashRevision())
                .overriddenFuzzerName(testCase.getOverriddenFuzzerName())
                .queue(testCase.getQueue())
                .redzone(testCase.getRedzone())
                .symbolized(testCase.getSymbolized())
                .triaged(testCase.getTriaged())
                .uploaderEmail(testCase.getUploaderEmail())
                .windowArgument(testCase.getWindowArgument())
                .lastModified(testCase.getLastModified())
                .build();
    }
}