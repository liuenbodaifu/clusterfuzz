package com.google.clusterfuzz.core.service;

import com.google.clusterfuzz.core.entity.Bot;
import com.google.clusterfuzz.core.repository.BotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for managing bot instances and their lifecycle.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BotService {

    private final BotRepository botRepository;
    
    private static final int HEARTBEAT_TIMEOUT_MINUTES = 5;
    private static final int TASK_TIMEOUT_MINUTES = 30;

    /**
     * Register a new bot or update existing bot information.
     */
    public Bot registerBot(Bot bot) {
        Optional<Bot> existingBot = botRepository.findByName(bot.getName());
        
        if (existingBot.isPresent()) {
            Bot existing = existingBot.get();
            // Update existing bot with new information
            existing.setPlatform(bot.getPlatform());
            existing.setVersion(bot.getVersion());
            existing.setIpAddress(bot.getIpAddress());
            existing.setPort(bot.getPort());
            existing.setCpuArchitecture(bot.getCpuArchitecture());
            existing.setCpuCores(bot.getCpuCores());
            existing.setMemoryMb(bot.getMemoryMb());
            existing.setDiskSpaceMb(bot.getDiskSpaceMb());
            existing.setConfiguration(bot.getConfiguration());
            existing.setEnvironment(bot.getEnvironment());
            existing.setSupportedEngines(bot.getSupportedEngines());
            existing.updateHeartbeat();
            existing.setStatus(Bot.BotStatus.IDLE);
            existing.setAvailable(true);
            existing.clearError();
            
            log.info("Updated existing bot: {}", bot.getName());
            return botRepository.save(existing);
        } else {
            bot.updateHeartbeat();
            bot.setStatus(Bot.BotStatus.IDLE);
            bot.setAvailable(true);
            
            log.info("Registered new bot: {}", bot.getName());
            return botRepository.save(bot);
        }
    }

    /**
     * Update bot heartbeat.
     */
    public void updateHeartbeat(String botName) {
        int updated = botRepository.updateHeartbeat(botName, LocalDateTime.now());
        if (updated == 0) {
            log.warn("Failed to update heartbeat for bot: {}", botName);
        }
    }

    /**
     * Find available bots for task assignment.
     */
    @Transactional(readOnly = true)
    public List<Bot> findAvailableBots() {
        LocalDateTime cutoffTime = LocalDateTime.now().minusMinutes(HEARTBEAT_TIMEOUT_MINUTES);
        return botRepository.findAvailableBots(cutoffTime);
    }

    /**
     * Find available bots for a specific platform.
     */
    @Transactional(readOnly = true)
    public List<Bot> findAvailableBots(String platform) {
        LocalDateTime cutoffTime = LocalDateTime.now().minusMinutes(HEARTBEAT_TIMEOUT_MINUTES);
        return botRepository.findAvailableBotsByPlatform(platform, cutoffTime);
    }

    /**
     * Find bots that support a specific fuzzing engine.
     */
    @Transactional(readOnly = true)
    public List<Bot> findBotsSupportingEngine(String engineName) {
        return botRepository.findBotsSupportingEngine(engineName);
    }

    /**
     * Assign a task to a bot.
     */
    public boolean assignTask(String botName, String taskName, String taskPayload, int timeoutMinutes) {
        Optional<Bot> botOpt = botRepository.findByName(botName);
        if (botOpt.isEmpty()) {
            log.error("Bot not found: {}", botName);
            return false;
        }

        Bot bot = botOpt.get();
        if (!bot.isAvailableForTasks()) {
            log.warn("Bot {} is not available for tasks", botName);
            return false;
        }

        LocalDateTime taskEndTime = LocalDateTime.now().plusMinutes(timeoutMinutes);
        bot.assignTask(taskName, taskPayload, taskEndTime);
        botRepository.save(bot);
        
        log.info("Assigned task {} to bot {}", taskName, botName);
        return true;
    }

    /**
     * Mark a task as completed by a bot.
     */
    public void completeTask(String botName) {
        Optional<Bot> botOpt = botRepository.findByName(botName);
        if (botOpt.isEmpty()) {
            log.error("Bot not found: {}", botName);
            return;
        }

        Bot bot = botOpt.get();
        bot.completeTask();
        botRepository.save(bot);
        
        log.info("Task completed by bot {}", botName);
    }

    /**
     * Record a crash found by a bot.
     */
    public void recordCrashFound(String botName) {
        botRepository.incrementCrashesFound(
            botRepository.findByName(botName)
                .map(Bot::getId)
                .orElse(null)
        );
    }

    /**
     * Set error status for a bot.
     */
    public void setBotError(String botName, String errorMessage) {
        Optional<Bot> botOpt = botRepository.findByName(botName);
        if (botOpt.isEmpty()) {
            log.error("Bot not found: {}", botName);
            return;
        }

        Bot bot = botOpt.get();
        bot.setError(errorMessage);
        botRepository.save(bot);
        
        log.warn("Bot {} encountered error: {}", botName, errorMessage);
    }

    /**
     * Clear error status for a bot.
     */
    public void clearBotError(String botName) {
        Optional<Bot> botOpt = botRepository.findByName(botName);
        if (botOpt.isEmpty()) {
            log.error("Bot not found: {}", botName);
            return;
        }

        Bot bot = botOpt.get();
        bot.clearError();
        botRepository.save(bot);
        
        log.info("Cleared error status for bot {}", botName);
    }

    /**
     * Get bot by name.
     */
    @Transactional(readOnly = true)
    public Optional<Bot> getBotByName(String name) {
        return botRepository.findByName(name);
    }

    /**
     * Get all bots with pagination.
     */
    @Transactional(readOnly = true)
    public Page<Bot> getAllBots(Pageable pageable) {
        return botRepository.findAll(pageable);
    }

    /**
     * Get bots by platform.
     */
    @Transactional(readOnly = true)
    public List<Bot> getBotsByPlatform(String platform) {
        return botRepository.findByPlatform(platform);
    }

    /**
     * Get bots by status.
     */
    @Transactional(readOnly = true)
    public List<Bot> getBotsByStatus(Bot.BotStatus status) {
        return botRepository.findByStatus(status);
    }

    /**
     * Get bot statistics.
     */
    @Transactional(readOnly = true)
    public BotStatistics getBotStatistics() {
        LocalDateTime cutoffTime = LocalDateTime.now().minusMinutes(HEARTBEAT_TIMEOUT_MINUTES);
        
        long totalBots = botRepository.count();
        long activeBots = botRepository.countActiveBots(cutoffTime);
        long availableBots = botRepository.countAvailableBots(cutoffTime);
        long busyBots = botRepository.countBusyBots(cutoffTime);
        
        List<Object[]> platformStats = botRepository.getBotStatisticsByPlatform();
        Map<String, PlatformStats> platformStatsMap = platformStats.stream()
            .collect(Collectors.toMap(
                row -> (String) row[0],
                row -> new PlatformStats(
                    ((Number) row[1]).longValue(),
                    ((Number) row[2]).longValue(),
                    ((Number) row[3]).longValue(),
                    ((Number) row[4]).longValue()
                )
            ));

        return BotStatistics.builder()
            .totalBots(totalBots)
            .activeBots(activeBots)
            .availableBots(availableBots)
            .busyBots(busyBots)
            .offlineBots(totalBots - activeBots)
            .platformStats(platformStatsMap)
            .build();
    }

    /**
     * Get top performing bots.
     */
    @Transactional(readOnly = true)
    public Page<Bot> getTopPerformingBots(Pageable pageable) {
        return botRepository.findTopPerformingBots(pageable);
    }

    /**
     * Restart a bot.
     */
    public void restartBot(String botName) {
        Optional<Bot> botOpt = botRepository.findByName(botName);
        if (botOpt.isEmpty()) {
            log.error("Bot not found: {}", botName);
            return;
        }

        Bot bot = botOpt.get();
        bot.setStatus(Bot.BotStatus.MAINTENANCE);
        bot.setAvailable(false);
        bot.setLastRestartTime(LocalDateTime.now());
        botRepository.save(bot);
        
        log.info("Initiated restart for bot {}", botName);
    }

    /**
     * Shutdown a bot.
     */
    public void shutdownBot(String botName) {
        Optional<Bot> botOpt = botRepository.findByName(botName);
        if (botOpt.isEmpty()) {
            log.error("Bot not found: {}", botName);
            return;
        }

        Bot bot = botOpt.get();
        bot.setStatus(Bot.BotStatus.SHUTDOWN);
        bot.setAvailable(false);
        botRepository.save(bot);
        
        log.info("Initiated shutdown for bot {}", botName);
    }

    /**
     * Update bot performance metrics.
     */
    public void updatePerformanceMetrics(String botName, String metricsJson) {
        Long botId = botRepository.findByName(botName)
            .map(Bot::getId)
            .orElse(null);
        
        if (botId != null) {
            botRepository.updatePerformanceMetrics(botId, metricsJson);
        }
    }

    /**
     * Scheduled task to clean up stale bots and tasks.
     */
    @Scheduled(fixedRate = 300000) // Run every 5 minutes
    public void cleanupStaleBots() {
        LocalDateTime heartbeatCutoff = LocalDateTime.now().minusMinutes(HEARTBEAT_TIMEOUT_MINUTES);
        LocalDateTime taskCutoff = LocalDateTime.now().minusMinutes(TASK_TIMEOUT_MINUTES);
        
        // Mark stale bots as offline
        int offlineBots = botRepository.markStaleBotsOffline(heartbeatCutoff);
        if (offlineBots > 0) {
            log.info("Marked {} stale bots as offline", offlineBots);
        }
        
        // Clear stale tasks
        int clearedTasks = botRepository.clearStaleTasks(LocalDateTime.now(), heartbeatCutoff);
        if (clearedTasks > 0) {
            log.info("Cleared {} stale tasks", clearedTasks);
        }
    }

    /**
     * Delete a bot.
     */
    public void deleteBot(String botName) {
        Optional<Bot> botOpt = botRepository.findByName(botName);
        if (botOpt.isPresent()) {
            botRepository.delete(botOpt.get());
            log.info("Deleted bot: {}", botName);
        }
    }

    // DTOs for statistics

    @lombok.Data
    @lombok.Builder
    public static class BotStatistics {
        private long totalBots;
        private long activeBots;
        private long availableBots;
        private long busyBots;
        private long offlineBots;
        private Map<String, PlatformStats> platformStats;
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    public static class PlatformStats {
        private long totalBots;
        private long idleBots;
        private long busyBots;
        private long offlineBots;
    }
}