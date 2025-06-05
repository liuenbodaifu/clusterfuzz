package com.google.clusterfuzz.core.repository;

import com.google.clusterfuzz.core.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Notification entities.
 * Provides data access methods for notification management and tracking.
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // Basic queries
    List<Notification> findByTestcaseId(Long testcaseId);
    
    List<Notification> findByUserEmail(String userEmail);
    
    List<Notification> findByNotificationType(String notificationType);
    
    Optional<Notification> findByTestcaseIdAndUserEmailAndNotificationType(
        Long testcaseId, String userEmail, String notificationType);

    // Sent status queries
    List<Notification> findBySent(Boolean sent);
    
    List<Notification> findByTestcaseIdAndSent(Long testcaseId, Boolean sent);
    
    List<Notification> findByUserEmailAndSent(String userEmail, Boolean sent);

    // Pending notifications
    @Query("SELECT n FROM Notification n WHERE n.sent = false AND " +
           "(n.nextRetryAt IS NULL OR n.nextRetryAt <= :now) AND n.sendAttempts < 5 " +
           "ORDER BY n.priority DESC, n.createdAt ASC")
    List<Notification> findPendingNotifications(@Param("now") LocalDateTime now);
    
    @Query("SELECT n FROM Notification n WHERE n.sent = false AND n.sendAttempts < 5 " +
           "ORDER BY n.priority DESC, n.createdAt ASC")
    List<Notification> findAllPendingNotifications();

    // Failed notifications
    @Query("SELECT n FROM Notification n WHERE n.sent = false AND n.sendAttempts >= 5")
    List<Notification> findFailedNotifications();
    
    @Query("SELECT n FROM Notification n WHERE n.sent = false AND n.lastError IS NOT NULL")
    List<Notification> findNotificationsWithErrors();

    // Priority queries
    List<Notification> findByPriority(String priority);
    
    @Query("SELECT n FROM Notification n WHERE n.priority IN ('HIGH', 'URGENT') AND n.sent = false " +
           "ORDER BY n.priority DESC, n.createdAt ASC")
    List<Notification> findHighPriorityPending();

    // Notification type specific queries
    @Query("SELECT n FROM Notification n WHERE n.notificationType = 'SECURITY_ISSUE' ORDER BY n.createdAt DESC")
    List<Notification> findSecurityNotifications();
    
    @Query("SELECT n FROM Notification n WHERE n.notificationType = 'NEW_CRASH' ORDER BY n.createdAt DESC")
    List<Notification> findNewCrashNotifications();
    
    @Query("SELECT n FROM Notification n WHERE n.notificationType = 'FIXED' ORDER BY n.createdAt DESC")
    List<Notification> findFixedNotifications();

    // Time-based queries
    List<Notification> findByCreatedAtAfter(LocalDateTime timestamp);
    
    List<Notification> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    
    List<Notification> findBySentAtAfter(LocalDateTime timestamp);
    
    @Query("SELECT n FROM Notification n WHERE n.createdAt >= :since ORDER BY n.createdAt DESC")
    List<Notification> findRecentNotifications(@Param("since") LocalDateTime since);

    // Retry queries
    @Query("SELECT n FROM Notification n WHERE n.nextRetryAt IS NOT NULL AND n.nextRetryAt <= :now AND n.sent = false")
    List<Notification> findReadyForRetry(@Param("now") LocalDateTime now);
    
    @Query("SELECT n FROM Notification n WHERE n.sendAttempts > 0 AND n.sent = false ORDER BY n.sendAttempts DESC")
    List<Notification> findNotificationsWithRetries();

    // Statistics queries
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.testcaseId = :testcaseId")
    long countByTestcaseId(@Param("testcaseId") Long testcaseId);
    
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.userEmail = :userEmail")
    long countByUserEmail(@Param("userEmail") String userEmail);
    
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.sent = true")
    long countSentNotifications();
    
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.sent = false")
    long countPendingNotifications();
    
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.sent = false AND n.sendAttempts >= 5")
    long countFailedNotifications();

    // Aggregation queries
    @Query("SELECT n.notificationType, COUNT(n) FROM Notification n GROUP BY n.notificationType ORDER BY COUNT(n) DESC")
    List<Object[]> getNotificationCountsByType();
    
    @Query("SELECT n.userEmail, COUNT(n) FROM Notification n GROUP BY n.userEmail ORDER BY COUNT(n) DESC")
    List<Object[]> getNotificationCountsByUser();
    
    @Query("SELECT n.priority, COUNT(n) FROM Notification n GROUP BY n.priority ORDER BY COUNT(n) DESC")
    List<Object[]> getNotificationCountsByPriority();

    // Success rate analysis
    @Query("SELECT n.notificationType, " +
           "COUNT(n), " +
           "COUNT(CASE WHEN n.sent = true THEN 1 END), " +
           "AVG(n.sendAttempts) " +
           "FROM Notification n GROUP BY n.notificationType ORDER BY n.notificationType")
    List<Object[]> getNotificationSuccessRates();
    
    @Query("SELECT (COUNT(CASE WHEN n.sent = true THEN 1 END) * 100.0 / COUNT(n)) " +
           "FROM Notification n WHERE n.userEmail = :userEmail")
    Double getSuccessRateForUser(@Param("userEmail") String userEmail);

    // Daily statistics
    @Query("SELECT DATE(n.createdAt), COUNT(n), COUNT(CASE WHEN n.sent = true THEN 1 END) " +
           "FROM Notification n WHERE n.createdAt >= :since " +
           "GROUP BY DATE(n.createdAt) ORDER BY DATE(n.createdAt)")
    List<Object[]> getDailyNotificationStats(@Param("since") LocalDateTime since);

    // User activity
    @Query("SELECT n FROM Notification n WHERE n.userEmail = :userEmail ORDER BY n.createdAt DESC")
    List<Notification> findByUserEmailOrderByCreatedAtDesc(@Param("userEmail") String userEmail);
    
    @Query("SELECT n FROM Notification n WHERE n.userEmail = :userEmail AND n.createdAt >= :since ORDER BY n.createdAt DESC")
    List<Notification> findRecentByUser(@Param("userEmail") String userEmail, @Param("since") LocalDateTime since);

    // Testcase notifications
    @Query("SELECT n FROM Notification n WHERE n.testcaseId = :testcaseId ORDER BY n.createdAt DESC")
    List<Notification> findByTestcaseIdOrderByCreatedAtDesc(@Param("testcaseId") Long testcaseId);
    
    @Query("SELECT DISTINCT n.userEmail FROM Notification n WHERE n.testcaseId = :testcaseId")
    List<String> findNotifiedUsersForTestcase(@Param("testcaseId") Long testcaseId);

    // Bulk operations
    @Modifying
    @Query("UPDATE Notification n SET n.sent = true, n.sentAt = :sentAt WHERE n.id IN :ids")
    int markAsSent(@Param("ids") List<Long> ids, @Param("sentAt") LocalDateTime sentAt);
    
    @Modifying
    @Query("UPDATE Notification n SET n.sent = false, n.sendAttempts = n.sendAttempts + 1, " +
           "n.lastError = :error, n.nextRetryAt = :nextRetry WHERE n.id IN :ids")
    int markAsFailed(@Param("ids") List<Long> ids, @Param("error") String error, @Param("nextRetry") LocalDateTime nextRetry);
    
    @Modifying
    @Query("UPDATE Notification n SET n.sendAttempts = 0, n.lastError = NULL, n.nextRetryAt = NULL, n.sent = false WHERE n.id IN :ids")
    int resetForRetry(@Param("ids") List<Long> ids);

    // Cleanup operations
    @Query("SELECT n FROM Notification n WHERE n.sent = true AND n.sentAt < :cutoffDate")
    List<Notification> findOldSentNotifications(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    @Query("SELECT n FROM Notification n WHERE n.sent = false AND n.sendAttempts >= 5 AND n.createdAt < :cutoffDate")
    List<Notification> findOldFailedNotifications(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.sent = true AND n.sentAt < :cutoffDate")
    int deleteOldSentNotifications(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.sent = false AND n.sendAttempts >= 5 AND n.createdAt < :cutoffDate")
    int deleteOldFailedNotifications(@Param("cutoffDate") LocalDateTime cutoffDate);

    // Search and filtering
    @Query("SELECT n FROM Notification n WHERE " +
           "n.subject LIKE %:searchTerm% OR " +
           "n.body LIKE %:searchTerm% OR " +
           "n.userEmail LIKE %:searchTerm% " +
           "ORDER BY n.createdAt DESC")
    List<Notification> searchNotifications(@Param("searchTerm") String searchTerm);

    // Pagination support
    Page<Notification> findByUserEmail(String userEmail, Pageable pageable);
    
    Page<Notification> findByNotificationType(String notificationType, Pageable pageable);
    
    Page<Notification> findBySent(Boolean sent, Pageable pageable);

    // Existence checks
    boolean existsByTestcaseIdAndUserEmailAndNotificationType(
        Long testcaseId, String userEmail, String notificationType);
    
    boolean existsByTestcaseIdAndUserEmail(Long testcaseId, String userEmail);

    // Complex filtering
    @Query("SELECT n FROM Notification n WHERE " +
           "(:testcaseId IS NULL OR n.testcaseId = :testcaseId) AND " +
           "(:userEmail IS NULL OR n.userEmail = :userEmail) AND " +
           "(:notificationType IS NULL OR n.notificationType = :notificationType) AND " +
           "(:sent IS NULL OR n.sent = :sent) AND " +
           "(:priority IS NULL OR n.priority = :priority) AND " +
           "(:startDate IS NULL OR n.createdAt >= :startDate) AND " +
           "(:endDate IS NULL OR n.createdAt <= :endDate) " +
           "ORDER BY n.createdAt DESC")
    Page<Notification> findWithFilters(
        @Param("testcaseId") Long testcaseId,
        @Param("userEmail") String userEmail,
        @Param("notificationType") String notificationType,
        @Param("sent") Boolean sent,
        @Param("priority") String priority,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        Pageable pageable
    );

    // Performance monitoring
    @Query("SELECT AVG(n.sendAttempts) FROM Notification n WHERE n.sent = true")
    Double getAverageSendAttempts();
    
    @Query("SELECT n.notificationType, AVG(n.sendAttempts) FROM Notification n WHERE n.sent = true " +
           "GROUP BY n.notificationType ORDER BY AVG(n.sendAttempts) DESC")
    List<Object[]> getAverageSendAttemptsByType();
}