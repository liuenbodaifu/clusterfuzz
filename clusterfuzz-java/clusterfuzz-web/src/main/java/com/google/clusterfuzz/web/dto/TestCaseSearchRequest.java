package com.google.clusterfuzz.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Search request DTO for testcase filtering.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "TestCase search criteria")
public class TestCaseSearchRequest {

    @Schema(description = "Search query string", example = "crash_type:ASSERT")
    private String query;

    @Schema(description = "Filter by crash type", example = "ASSERT")
    private String crashType;

    @Schema(description = "Filter by crash state", example = "NULL_DEREFERENCE")
    private String crashState;

    @Schema(description = "Filter by job type", example = "libfuzzer_chrome_asan")
    private String jobType;

    @Schema(description = "Filter by fuzzer name", example = "libFuzzer")
    private String fuzzerName;

    @Schema(description = "Filter by platform", example = "linux")
    private String platform;

    @Schema(description = "Filter by status", example = "Processed")
    private String status;

    @Schema(description = "Filter by security flag", example = "true")
    private Boolean securityFlag;

    @Schema(description = "Filter by open status", example = "true")
    private Boolean open;

    @Schema(description = "Filter by fixed status", example = "false")
    private Boolean fixed;

    @Schema(description = "Filter by regression status", example = "false")
    private Boolean regression;

    @Schema(description = "Filter by one-time crasher flag", example = "false")
    private Boolean oneTimeCrasherFlag;

    @Schema(description = "Filter by project name", example = "chromium")
    private String projectName;

    @Schema(description = "Filter by group ID", example = "12345")
    private Long groupId;

    @Schema(description = "Filter by uploader email", example = "user@example.com")
    private String uploaderEmail;

    @Schema(description = "Filter from timestamp", example = "2025-01-01T00:00:00")
    private LocalDateTime fromTimestamp;

    @Schema(description = "Filter to timestamp", example = "2025-12-31T23:59:59")
    private LocalDateTime toTimestamp;

    @Schema(description = "Filter by minimum crash revision", example = "123000")
    private Integer minCrashRevision;

    @Schema(description = "Filter by maximum crash revision", example = "124000")
    private Integer maxCrashRevision;

    @Schema(description = "Filter by impact on stable version", example = "YES")
    private String impactStableVersion;

    @Schema(description = "Filter by impact on beta version", example = "YES")
    private String impactBetaVersion;

    @Schema(description = "Filter by impact on head version", example = "YES")
    private String impactHeadVersion;

    @Schema(description = "Filter by triaged status", example = "true")
    private Boolean triaged;

    @Schema(description = "Filter by symbolized status", example = "true")
    private Boolean symbolized;

    @Schema(description = "Filter by archive state", example = "ARCHIVED")
    private String archiveState;
}