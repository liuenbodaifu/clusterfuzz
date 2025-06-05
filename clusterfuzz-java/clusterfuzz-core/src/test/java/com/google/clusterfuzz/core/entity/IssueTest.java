package com.google.clusterfuzz.core.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Issue entity.
 */
@DisplayName("Issue Entity Tests")
class IssueTest {

    private Validator validator;
    private Issue.IssueBuilder validIssueBuilder;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        
        validIssueBuilder = Issue.builder()
                .externalId("12345")
                .tracker(Issue.IssueTracker.GITHUB)
                .project("test-project")
                .title("Test Issue")
                .description("This is a test issue")
                .status(Issue.IssueStatus.NEW)
                .priority(Issue.IssuePriority.MEDIUM)
                .severity(Issue.IssueSeverity.MAJOR)
                .reporter("test-user")
                .isSecurity(false)
                .isPublic(true);
    }

    @Test
    @DisplayName("Should create valid issue with all required fields")
    void shouldCreateValidIssue() {
        Issue issue = validIssueBuilder.build();
        
        Set<ConstraintViolation<Issue>> violations = validator.validate(issue);
        assertTrue(violations.isEmpty(), "Valid issue should have no validation violations");
        
        assertEquals("12345", issue.getExternalId());
        assertEquals(Issue.IssueTracker.GITHUB, issue.getTracker());
        assertEquals("test-project", issue.getProject());
        assertEquals("Test Issue", issue.getTitle());
        assertEquals(Issue.IssueStatus.NEW, issue.getStatus());
    }

    @Test
    @DisplayName("Should fail validation when external ID is null")
    void shouldFailValidationWhenExternalIdIsNull() {
        Issue issue = validIssueBuilder.externalId(null).build();
        
        Set<ConstraintViolation<Issue>> violations = validator.validate(issue);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("externalId")));
    }

    @Test
    @DisplayName("Should fail validation when external ID is blank")
    void shouldFailValidationWhenExternalIdIsBlank() {
        Issue issue = validIssueBuilder.externalId("   ").build();
        
        Set<ConstraintViolation<Issue>> violations = validator.validate(issue);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("externalId")));
    }

    @Test
    @DisplayName("Should fail validation when tracker is null")
    void shouldFailValidationWhenTrackerIsNull() {
        Issue issue = validIssueBuilder.tracker(null).build();
        
        Set<ConstraintViolation<Issue>> violations = validator.validate(issue);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("tracker")));
    }

    @Test
    @DisplayName("Should fail validation when project is null")
    void shouldFailValidationWhenProjectIsNull() {
        Issue issue = validIssueBuilder.project(null).build();
        
        Set<ConstraintViolation<Issue>> violations = validator.validate(issue);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("project")));
    }

    @Test
    @DisplayName("Should fail validation when title is null")
    void shouldFailValidationWhenTitleIsNull() {
        Issue issue = validIssueBuilder.title(null).build();
        
        Set<ConstraintViolation<Issue>> violations = validator.validate(issue);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("title")));
    }

    @Test
    @DisplayName("Should fail validation when title exceeds max length")
    void shouldFailValidationWhenTitleExceedsMaxLength() {
        String longTitle = "a".repeat(501); // Max is 500
        Issue issue = validIssueBuilder.title(longTitle).build();
        
        Set<ConstraintViolation<Issue>> violations = validator.validate(issue);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("title")));
    }

    @ParameterizedTest
    @EnumSource(Issue.IssueTracker.class)
    @DisplayName("Should accept all valid issue trackers")
    void shouldAcceptAllValidIssueTrackers(Issue.IssueTracker tracker) {
        Issue issue = validIssueBuilder.tracker(tracker).build();
        
        Set<ConstraintViolation<Issue>> violations = validator.validate(issue);
        assertTrue(violations.isEmpty());
        assertEquals(tracker, issue.getTracker());
    }

    @ParameterizedTest
    @EnumSource(Issue.IssueStatus.class)
    @DisplayName("Should accept all valid issue statuses")
    void shouldAcceptAllValidIssueStatuses(Issue.IssueStatus status) {
        Issue issue = validIssueBuilder.status(status).build();
        
        Set<ConstraintViolation<Issue>> violations = validator.validate(issue);
        assertTrue(violations.isEmpty());
        assertEquals(status, issue.getStatus());
    }

    @ParameterizedTest
    @EnumSource(Issue.IssuePriority.class)
    @DisplayName("Should accept all valid issue priorities")
    void shouldAcceptAllValidIssuePriorities(Issue.IssuePriority priority) {
        Issue issue = validIssueBuilder.priority(priority).build();
        
        Set<ConstraintViolation<Issue>> violations = validator.validate(issue);
        assertTrue(violations.isEmpty());
        assertEquals(priority, issue.getPriority());
    }

    @ParameterizedTest
    @EnumSource(Issue.IssueSeverity.class)
    @DisplayName("Should accept all valid issue severities")
    void shouldAcceptAllValidIssueSeverities(Issue.IssueSeverity severity) {
        Issue issue = validIssueBuilder.severity(severity).build();
        
        Set<ConstraintViolation<Issue>> violations = validator.validate(issue);
        assertTrue(violations.isEmpty());
        assertEquals(severity, issue.getSeverity());
    }

    @Test
    @DisplayName("Should correctly identify open issue")
    void shouldCorrectlyIdentifyOpenIssue() {
        Issue issue = validIssueBuilder.status(Issue.IssueStatus.NEW).build();
        assertTrue(issue.isOpen(), "NEW issue should be open");
        
        issue = validIssueBuilder.status(Issue.IssueStatus.ASSIGNED).build();
        assertTrue(issue.isOpen(), "ASSIGNED issue should be open");
        
        issue = validIssueBuilder.status(Issue.IssueStatus.STARTED).build();
        assertTrue(issue.isOpen(), "STARTED issue should be open");
    }

    @Test
    @DisplayName("Should correctly identify closed issue")
    void shouldCorrectlyIdentifyClosedIssue() {
        Issue issue = validIssueBuilder.status(Issue.IssueStatus.FIXED).build();
        assertTrue(issue.isClosed(), "FIXED issue should be closed");
        
        issue = validIssueBuilder.status(Issue.IssueStatus.VERIFIED).build();
        assertTrue(issue.isClosed(), "VERIFIED issue should be closed");
        
        issue = validIssueBuilder.status(Issue.IssueStatus.INVALID).build();
        assertTrue(issue.isClosed(), "INVALID issue should be closed");
        
        issue = validIssueBuilder.status(Issue.IssueStatus.WONTFIX).build();
        assertTrue(issue.isClosed(), "WONTFIX issue should be closed");
        
        issue = validIssueBuilder.status(Issue.IssueStatus.DUPLICATE).build();
        assertTrue(issue.isClosed(), "DUPLICATE issue should be closed");
        
        issue = validIssueBuilder.status(Issue.IssueStatus.CLOSED).build();
        assertTrue(issue.isClosed(), "CLOSED issue should be closed");
    }

    @Test
    @DisplayName("Should mark issue as fixed correctly")
    void shouldMarkIssueAsFixedCorrectly() {
        Issue issue = validIssueBuilder.status(Issue.IssueStatus.NEW).build();
        LocalDateTime beforeFix = LocalDateTime.now().minusSeconds(1);
        
        issue.markFixed();
        
        assertEquals(Issue.IssueStatus.FIXED, issue.getStatus());
        assertNotNull(issue.getClosedAt());
        assertTrue(issue.getClosedAt().isAfter(beforeFix));
        assertTrue(issue.isClosed());
    }

    @Test
    @DisplayName("Should mark issue as verified correctly")
    void shouldMarkIssueAsVerifiedCorrectly() {
        Issue issue = validIssueBuilder.status(Issue.IssueStatus.FIXED).build();
        LocalDateTime beforeVerify = LocalDateTime.now().minusSeconds(1);
        
        issue.markVerified();
        
        assertEquals(Issue.IssueStatus.VERIFIED, issue.getStatus());
        assertNotNull(issue.getClosedAt());
        assertTrue(issue.getClosedAt().isAfter(beforeVerify));
        assertTrue(issue.isClosed());
    }

    @Test
    @DisplayName("Should mark issue as verified without changing existing closed time")
    void shouldMarkIssueAsVerifiedWithoutChangingExistingClosedTime() {
        LocalDateTime existingClosedTime = LocalDateTime.now().minusHours(1);
        Issue issue = validIssueBuilder
                .status(Issue.IssueStatus.FIXED)
                .closedAt(existingClosedTime)
                .build();
        
        issue.markVerified();
        
        assertEquals(Issue.IssueStatus.VERIFIED, issue.getStatus());
        assertEquals(existingClosedTime, issue.getClosedAt());
    }

    @Test
    @DisplayName("Should close issue with reason correctly")
    void shouldCloseIssueWithReasonCorrectly() {
        Issue issue = validIssueBuilder.status(Issue.IssueStatus.NEW).build();
        String closeReason = "Cannot reproduce";
        LocalDateTime beforeClose = LocalDateTime.now().minusSeconds(1);
        
        issue.close(Issue.IssueStatus.INVALID, closeReason);
        
        assertEquals(Issue.IssueStatus.INVALID, issue.getStatus());
        assertEquals(closeReason, issue.getCloseReason());
        assertNotNull(issue.getClosedAt());
        assertTrue(issue.getClosedAt().isAfter(beforeClose));
        assertTrue(issue.isClosed());
    }

    @Test
    @DisplayName("Should reopen issue correctly")
    void shouldReopenIssueCorrectly() {
        Issue issue = validIssueBuilder
                .status(Issue.IssueStatus.FIXED)
                .closedAt(LocalDateTime.now().minusHours(1))
                .closeReason("Fixed in version 1.0")
                .build();
        
        issue.reopen();
        
        assertEquals(Issue.IssueStatus.NEW, issue.getStatus());
        assertNull(issue.getClosedAt());
        assertNull(issue.getCloseReason());
        assertTrue(issue.isOpen());
    }

    @Test
    @DisplayName("Should add label correctly")
    void shouldAddLabelCorrectly() {
        Issue issue = validIssueBuilder.build();
        
        issue.addLabel("bug");
        issue.addLabel("high-priority");
        
        assertNotNull(issue.getLabels());
        assertTrue(issue.getLabels().contains("bug"));
        assertTrue(issue.getLabels().contains("high-priority"));
        assertEquals(2, issue.getLabels().size());
    }

    @Test
    @DisplayName("Should remove label correctly")
    void shouldRemoveLabelCorrectly() {
        Issue issue = validIssueBuilder.build();
        issue.addLabel("bug");
        issue.addLabel("high-priority");
        
        issue.removeLabel("bug");
        
        assertFalse(issue.getLabels().contains("bug"));
        assertTrue(issue.getLabels().contains("high-priority"));
        assertEquals(1, issue.getLabels().size());
    }

    @Test
    @DisplayName("Should add component correctly")
    void shouldAddComponentCorrectly() {
        Issue issue = validIssueBuilder.build();
        
        issue.addComponent("ui");
        issue.addComponent("backend");
        
        assertNotNull(issue.getComponents());
        assertTrue(issue.getComponents().contains("ui"));
        assertTrue(issue.getComponents().contains("backend"));
        assertEquals(2, issue.getComponents().size());
    }

    @Test
    @DisplayName("Should remove component correctly")
    void shouldRemoveComponentCorrectly() {
        Issue issue = validIssueBuilder.build();
        issue.addComponent("ui");
        issue.addComponent("backend");
        
        issue.removeComponent("ui");
        
        assertFalse(issue.getComponents().contains("ui"));
        assertTrue(issue.getComponents().contains("backend"));
        assertEquals(1, issue.getComponents().size());
    }

    @Test
    @DisplayName("Should handle security flag correctly")
    void shouldHandleSecurityFlagCorrectly() {
        Issue securityIssue = validIssueBuilder.isSecurity(true).build();
        assertTrue(securityIssue.getIsSecurity());
        
        Issue normalIssue = validIssueBuilder.isSecurity(false).build();
        assertFalse(normalIssue.getIsSecurity());
    }

    @Test
    @DisplayName("Should handle public flag correctly")
    void shouldHandlePublicFlagCorrectly() {
        Issue publicIssue = validIssueBuilder.isPublic(true).build();
        assertTrue(publicIssue.getIsPublic());
        
        Issue privateIssue = validIssueBuilder.isPublic(false).build();
        assertFalse(privateIssue.getIsPublic());
    }

    @Test
    @DisplayName("Should validate URL length")
    void shouldValidateUrlLength() {
        String longUrl = "https://example.com/" + "a".repeat(500); // Max is 500 total
        Issue issue = validIssueBuilder.url(longUrl).build();
        
        Set<ConstraintViolation<Issue>> violations = validator.validate(issue);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("url")));
    }

    @Test
    @DisplayName("Should accept valid URL")
    void shouldAcceptValidUrl() {
        String validUrl = "https://github.com/test-org/test-repo/issues/12345";
        Issue issue = validIssueBuilder.url(validUrl).build();
        
        Set<ConstraintViolation<Issue>> violations = validator.validate(issue);
        assertTrue(violations.isEmpty());
        assertEquals(validUrl, issue.getUrl());
    }

    @Test
    @DisplayName("Should initialize default values correctly")
    void shouldInitializeDefaultValuesCorrectly() {
        Issue issue = Issue.builder()
                .externalId("123")
                .tracker(Issue.IssueTracker.GITHUB)
                .project("test")
                .title("Test")
                .build();
        
        assertEquals(Issue.IssueStatus.NEW, issue.getStatus());
        assertFalse(issue.getIsSecurity());
        assertTrue(issue.getIsPublic());
        assertEquals(0, issue.getCommentCount());
    }

    @Test
    @DisplayName("Should handle assignee and reporter correctly")
    void shouldHandleAssigneeAndReporterCorrectly() {
        Issue issue = validIssueBuilder
                .reporter("john.doe@example.com")
                .assignee("jane.smith@example.com")
                .build();
        
        assertEquals("john.doe@example.com", issue.getReporter());
        assertEquals("jane.smith@example.com", issue.getAssignee());
    }

    @Test
    @DisplayName("Should handle milestone correctly")
    void shouldHandleMilestoneCorrectly() {
        Issue issue = validIssueBuilder.milestone("v2.0.0").build();
        
        assertEquals("v2.0.0", issue.getMilestone());
    }
}