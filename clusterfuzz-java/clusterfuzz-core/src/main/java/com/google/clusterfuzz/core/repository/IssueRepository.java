package com.google.clusterfuzz.core.repository;

import com.google.clusterfuzz.core.entity.Issue;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Issue entities.
 */
@Repository
public interface IssueRepository extends JpaRepository<Issue, Long> {

    /**
     * Find an issue by external ID and tracker.
     */
    Optional<Issue> findByExternalIdAndTracker(String externalId, Issue.IssueTracker tracker);

    /**
     * Find issues by tracker and project.
     */
    List<Issue> findByTrackerAndProject(Issue.IssueTracker tracker, String project);

    /**
     * Find issues by status.
     */
    List<Issue> findByStatus(Issue.IssueStatus status);

    /**
     * Find open issues.
     */
    @Query("SELECT i FROM Issue i WHERE i.status NOT IN ('FIXED', 'VERIFIED', 'INVALID', 'WONTFIX', 'DUPLICATE', 'CLOSED')")
    List<Issue> findOpenIssues();

    /**
     * Find closed issues.
     */
    @Query("SELECT i FROM Issue i WHERE i.status IN ('FIXED', 'VERIFIED', 'INVALID', 'WONTFIX', 'DUPLICATE', 'CLOSED')")
    List<Issue> findClosedIssues();

    /**
     * Find security issues.
     */
    List<Issue> findByIsSecurityTrue();

    /**
     * Find public issues.
     */
    List<Issue> findByIsPublicTrue();

    /**
     * Find issues by priority.
     */
    List<Issue> findByPriority(Issue.IssuePriority priority);

    /**
     * Find issues by severity.
     */
    List<Issue> findBySeverity(Issue.IssueSeverity severity);

    /**
     * Find issues assigned to a specific user.
     */
    List<Issue> findByAssignee(String assignee);

    /**
     * Find issues reported by a specific user.
     */
    List<Issue> findByReporter(String reporter);

    /**
     * Find issues with specific labels.
     */
    @Query("SELECT i FROM Issue i JOIN i.labels l WHERE l IN :labels")
    List<Issue> findByLabelsIn(@Param("labels") List<String> labels);

    /**
     * Find issues affecting specific components.
     */
    @Query("SELECT i FROM Issue i JOIN i.components c WHERE c IN :components")
    List<Issue> findByComponentsIn(@Param("components") List<String> components);

    /**
     * Find issues created within a date range.
     */
    @Query("SELECT i FROM Issue i WHERE i.createdAt BETWEEN :startDate AND :endDate")
    List<Issue> findIssuesCreatedBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    /**
     * Find issues updated within a date range.
     */
    @Query("SELECT i FROM Issue i WHERE i.updatedAt BETWEEN :startDate AND :endDate")
    List<Issue> findIssuesUpdatedBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    /**
     * Find issues closed within a date range.
     */
    @Query("SELECT i FROM Issue i WHERE i.closedAt BETWEEN :startDate AND :endDate")
    List<Issue> findIssuesClosedBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    /**
     * Find stale issues (not updated recently).
     */
    @Query("SELECT i FROM Issue i WHERE i.updatedAt < :cutoffDate AND i.status NOT IN ('FIXED', 'VERIFIED', 'CLOSED')")
    List<Issue> findStaleIssues(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Search issues by title.
     */
    @Query("SELECT i FROM Issue i WHERE LOWER(i.title) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Issue> findByTitleContainingIgnoreCase(@Param("searchTerm") String searchTerm);

    /**
     * Search issues by description.
     */
    @Query("SELECT i FROM Issue i WHERE LOWER(i.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Issue> findByDescriptionContainingIgnoreCase(@Param("searchTerm") String searchTerm);

    /**
     * Get issue statistics by status.
     */
    @Query("SELECT i.status, COUNT(i) FROM Issue i GROUP BY i.status")
    List<Object[]> getIssueCountByStatus();

    /**
     * Get issue statistics by priority.
     */
    @Query("SELECT i.priority, COUNT(i) FROM Issue i WHERE i.priority IS NOT NULL GROUP BY i.priority")
    List<Object[]> getIssueCountByPriority();

    /**
     * Get issue statistics by severity.
     */
    @Query("SELECT i.severity, COUNT(i) FROM Issue i WHERE i.severity IS NOT NULL GROUP BY i.severity")
    List<Object[]> getIssueCountBySeverity();

    /**
     * Get issue statistics by tracker.
     */
    @Query("SELECT i.tracker, COUNT(i) FROM Issue i GROUP BY i.tracker")
    List<Object[]> getIssueCountByTracker();

    /**
     * Get issue statistics by project.
     */
    @Query("SELECT i.project, COUNT(i) FROM Issue i GROUP BY i.project")
    List<Object[]> getIssueCountByProject();

    /**
     * Find high priority open issues.
     */
    @Query("SELECT i FROM Issue i WHERE i.priority IN ('CRITICAL', 'HIGH') AND i.status NOT IN ('FIXED', 'VERIFIED', 'CLOSED')")
    List<Issue> findHighPriorityOpenIssues();

    /**
     * Find security issues that are still open.
     */
    @Query("SELECT i FROM Issue i WHERE i.isSecurity = true AND i.status NOT IN ('FIXED', 'VERIFIED', 'CLOSED')")
    List<Issue> findOpenSecurityIssues();

    /**
     * Find issues without assignee.
     */
    @Query("SELECT i FROM Issue i WHERE i.assignee IS NULL AND i.status NOT IN ('FIXED', 'VERIFIED', 'CLOSED')")
    List<Issue> findUnassignedOpenIssues();

    /**
     * Find issues with many comments (popular issues).
     */
    @Query("SELECT i FROM Issue i WHERE i.commentCount > :minComments ORDER BY i.commentCount DESC")
    List<Issue> findPopularIssues(@Param("minComments") Integer minComments);

    /**
     * Find recently created issues.
     */
    @Query("SELECT i FROM Issue i WHERE i.createdAt > :cutoffDate ORDER BY i.createdAt DESC")
    List<Issue> findRecentIssues(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Find issues by milestone.
     */
    List<Issue> findByMilestone(String milestone);

    /**
     * Get average time to close issues.
     */
    @Query("SELECT AVG(EXTRACT(EPOCH FROM (i.closedAt - i.createdAt))) FROM Issue i WHERE i.closedAt IS NOT NULL")
    Double getAverageTimeToClose();

    /**
     * Get issues closed in the last N days.
     */
    @Query("SELECT COUNT(i) FROM Issue i WHERE i.closedAt > :cutoffDate")
    long countIssuesClosedSince(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Get issues created in the last N days.
     */
    @Query("SELECT COUNT(i) FROM Issue i WHERE i.createdAt > :cutoffDate")
    long countIssuesCreatedSince(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Find issues that need external sync (not updated recently in external tracker).
     */
    @Query("SELECT i FROM Issue i WHERE i.lastUpdatedExternal IS NULL OR i.lastUpdatedExternal < :cutoffDate")
    List<Issue> findIssuesNeedingSync(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Find duplicate issues by title similarity.
     */
    @Query("SELECT i FROM Issue i WHERE LOWER(i.title) LIKE LOWER(CONCAT('%', :titlePattern, '%')) AND i.id != :excludeId")
    List<Issue> findPotentialDuplicates(@Param("titlePattern") String titlePattern, @Param("excludeId") Long excludeId);

    /**
     * Get issues with testcases.
     */
    @Query("SELECT i FROM Issue i WHERE SIZE(i.testcases) > 0")
    List<Issue> findIssuesWithTestcases();

    /**
     * Get issues without testcases.
     */
    @Query("SELECT i FROM Issue i WHERE SIZE(i.testcases) = 0")
    List<Issue> findIssuesWithoutTestcases();

    /**
     * Find issues by URL pattern.
     */
    @Query("SELECT i FROM Issue i WHERE i.url LIKE %:urlPattern%")
    List<Issue> findByUrlContaining(@Param("urlPattern") String urlPattern);

    /**
     * Get paginated issues with filters.
     */
    @Query("SELECT i FROM Issue i WHERE " +
           "(:status IS NULL OR i.status = :status) AND " +
           "(:priority IS NULL OR i.priority = :priority) AND " +
           "(:isSecurity IS NULL OR i.isSecurity = :isSecurity) AND " +
           "(:assignee IS NULL OR i.assignee = :assignee) AND " +
           "(:project IS NULL OR i.project = :project)")
    Page<Issue> findIssuesWithFilters(
            @Param("status") Issue.IssueStatus status,
            @Param("priority") Issue.IssuePriority priority,
            @Param("isSecurity") Boolean isSecurity,
            @Param("assignee") String assignee,
            @Param("project") String project,
            Pageable pageable);
}