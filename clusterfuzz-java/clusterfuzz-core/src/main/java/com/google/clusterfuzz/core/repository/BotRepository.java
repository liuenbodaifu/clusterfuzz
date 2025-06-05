package com.google.clusterfuzz.core.repository;

import com.google.clusterfuzz.core.entity.Bot;
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
 * Repository interface for Bot entities.
 */
@Repository
public interface BotRepository extends JpaRepository<Bot, Long> {

    /**
     * Find a bot by its unique name.
     */
    Optional<Bot> findByName(String name);

    /**
     * Find all bots by platform.
     */
    List<Bot> findByPlatform(String platform);

    /**
     * Find all bots with a specific status.
     */
    List<Bot> findByStatus(Bot.BotStatus status);

    /**
     * Find all available bots for task assignment.
     */
    @Query("SELECT b FROM Bot b WHERE b.available = true AND b.status = 'IDLE' AND b.lastBeatTime > :cutoffTime")
    List<Bot> findAvailableBots(@Param("cutoffTime") LocalDateTime cutoffTime);

    /**
     * Find bots by platform and availability.
     */
    @Query("SELECT b FROM Bot b WHERE b.platform = :platform AND b.available = true AND b.status = 'IDLE' AND b.lastBeatTime > :cutoffTime")
    List<Bot> findAvailableBotsByPlatform(@Param("platform") String platform, @Param("cutoffTime") LocalDateTime cutoffTime);

    /**
     * Find bots that support a specific fuzzing engine.
     */
    @Query("SELECT b FROM Bot b JOIN b.supportedEngines e WHERE e = :engineName AND b.available = true")
    List<Bot> findBotsSupportingEngine(@Param("engineName") String engineName);

    /**
     * Find bots that haven't sent a heartbeat recently (potentially offline).
     */
    @Query("SELECT b FROM Bot b WHERE b.lastBeatTime < :cutoffTime OR b.lastBeatTime IS NULL")
    List<Bot> findStaleBot(@Param("cutoffTime") LocalDateTime cutoffTime);

    /**
     * Find bots currently executing a specific task.
     */
    List<Bot> findByTaskName(String taskName);

    /**
     * Find bots with tasks that should have completed by now.
     */
    @Query("SELECT b FROM Bot b WHERE b.taskEndTime IS NOT NULL AND b.taskEndTime < :currentTime AND b.status = 'BUSY'")
    List<Bot> findBotsWithOverdueTasks(@Param("currentTime") LocalDateTime currentTime);

    /**
     * Get bot statistics by platform.
     */
    @Query("SELECT b.platform, COUNT(b), " +
           "SUM(CASE WHEN b.status = 'IDLE' THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN b.status = 'BUSY' THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN b.status = 'OFFLINE' THEN 1 ELSE 0 END) " +
           "FROM Bot b GROUP BY b.platform")
    List<Object[]> getBotStatisticsByPlatform();

    /**
     * Get top performing bots by crashes found.
     */
    @Query("SELECT b FROM Bot b ORDER BY b.crashesFound DESC")
    Page<Bot> findTopPerformingBots(Pageable pageable);

    /**
     * Get bots with high task completion rates.
     */
    @Query("SELECT b FROM Bot b WHERE b.tasksCompleted > :minTasks ORDER BY b.tasksCompleted DESC")
    List<Bot> findHighPerformingBots(@Param("minTasks") Long minTasks);

    /**
     * Update bot heartbeat.
     */
    @Modifying
    @Query("UPDATE Bot b SET b.lastBeatTime = :heartbeatTime WHERE b.name = :botName")
    int updateHeartbeat(@Param("botName") String botName, @Param("heartbeatTime") LocalDateTime heartbeatTime);

    /**
     * Mark bots as offline if they haven't sent heartbeat recently.
     */
    @Modifying
    @Query("UPDATE Bot b SET b.status = 'OFFLINE', b.available = false WHERE b.lastBeatTime < :cutoffTime AND b.status != 'OFFLINE'")
    int markStaleBotsOffline(@Param("cutoffTime") LocalDateTime cutoffTime);

    /**
     * Clear tasks for bots that are no longer busy.
     */
    @Modifying
    @Query("UPDATE Bot b SET b.taskName = NULL, b.taskPayload = NULL, b.taskEndTime = NULL, b.status = 'IDLE', b.available = true " +
           "WHERE b.status = 'BUSY' AND (b.taskEndTime < :currentTime OR b.lastBeatTime < :cutoffTime)")
    int clearStaleTasks(@Param("currentTime") LocalDateTime currentTime, @Param("cutoffTime") LocalDateTime cutoffTime);

    /**
     * Get total number of active bots.
     */
    @Query("SELECT COUNT(b) FROM Bot b WHERE b.lastBeatTime > :cutoffTime")
    long countActiveBots(@Param("cutoffTime") LocalDateTime cutoffTime);

    /**
     * Get total number of available bots.
     */
    @Query("SELECT COUNT(b) FROM Bot b WHERE b.available = true AND b.status = 'IDLE' AND b.lastBeatTime > :cutoffTime")
    long countAvailableBots(@Param("cutoffTime") LocalDateTime cutoffTime);

    /**
     * Get total number of busy bots.
     */
    @Query("SELECT COUNT(b) FROM Bot b WHERE b.status = 'BUSY' AND b.lastBeatTime > :cutoffTime")
    long countBusyBots(@Param("cutoffTime") LocalDateTime cutoffTime);

    /**
     * Find bots by IP address.
     */
    List<Bot> findByIpAddress(String ipAddress);

    /**
     * Find bots with specific CPU architecture.
     */
    List<Bot> findByCpuArchitecture(String cpuArchitecture);

    /**
     * Find bots with minimum memory requirement.
     */
    @Query("SELECT b FROM Bot b WHERE b.memoryMb >= :minMemoryMb")
    List<Bot> findBotsWithMinMemory(@Param("minMemoryMb") Integer minMemoryMb);

    /**
     * Find bots with minimum CPU cores.
     */
    @Query("SELECT b FROM Bot b WHERE b.cpuCores >= :minCores")
    List<Bot> findBotsWithMinCores(@Param("minCores") Integer minCores);

    /**
     * Search bots by name pattern.
     */
    @Query("SELECT b FROM Bot b WHERE b.name LIKE %:namePattern%")
    List<Bot> findByNameContaining(@Param("namePattern") String namePattern);

    /**
     * Get bots created within a date range.
     */
    @Query("SELECT b FROM Bot b WHERE b.createdAt BETWEEN :startDate AND :endDate")
    List<Bot> findBotsCreatedBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    /**
     * Get average uptime for all bots.
     */
    @Query("SELECT AVG(b.uptimeSeconds) FROM Bot b WHERE b.uptimeSeconds IS NOT NULL")
    Double getAverageUptime();

    /**
     * Get bots with errors.
     */
    @Query("SELECT b FROM Bot b WHERE b.status = 'ERROR' AND b.lastError IS NOT NULL")
    List<Bot> findBotsWithErrors();

    /**
     * Update bot performance metrics.
     */
    @Modifying
    @Query("UPDATE Bot b SET b.performanceMetrics = :metrics WHERE b.id = :botId")
    int updatePerformanceMetrics(@Param("botId") Long botId, @Param("metrics") String metrics);

    /**
     * Increment tasks completed counter.
     */
    @Modifying
    @Query("UPDATE Bot b SET b.tasksCompleted = b.tasksCompleted + 1 WHERE b.id = :botId")
    int incrementTasksCompleted(@Param("botId") Long botId);

    /**
     * Increment crashes found counter.
     */
    @Modifying
    @Query("UPDATE Bot b SET b.crashesFound = b.crashesFound + 1 WHERE b.id = :botId")
    int incrementCrashesFound(@Param("botId") Long botId);
}