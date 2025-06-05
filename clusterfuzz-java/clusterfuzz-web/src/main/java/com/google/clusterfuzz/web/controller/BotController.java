package com.google.clusterfuzz.web.controller;

import com.google.clusterfuzz.core.entity.Bot;
import com.google.clusterfuzz.core.service.BotService;
import com.google.clusterfuzz.web.dto.BotDto;
import com.google.clusterfuzz.web.dto.BotListResponse;
import com.google.clusterfuzz.web.dto.TaskAssignmentRequest;
import com.google.clusterfuzz.web.dto.TaskCompletionRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * REST API controller for Bot management.
 * Provides endpoints for managing bot instances, task assignment, and monitoring.
 */
@RestController
@RequestMapping("/api/v1/bots")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Bot Management", description = "APIs for managing bot instances")
public class BotController {

    private final BotService botService;

    /**
     * Get paginated list of bots.
     */
    @GetMapping
    @Operation(summary = "List bots", description = "Get paginated list of active bots")
    public ResponseEntity<BotListResponse> getBots(
            @Parameter(description = "Filter by platform") @RequestParam(value = "platform", required = false) String platform,
            @Parameter(description = "Filter by status") @RequestParam(value = "status", required = false) String status,
            @PageableDefault(size = 20) Pageable pageable) {
        
        log.debug("Getting bots with platform: {} and status: {}", platform, status);
        
        Page<Bot> botPage = botService.findBots(platform, status, pageable);
        BotListResponse response = BotListResponse.builder()
                .bots(botPage.getContent().stream()
                        .map(BotDto::fromEntity)
                        .toList())
                .totalElements(botPage.getTotalElements())
                .totalPages(botPage.getTotalPages())
                .currentPage(botPage.getNumber())
                .pageSize(botPage.getSize())
                .hasNext(botPage.hasNext())
                .hasPrevious(botPage.hasPrevious())
                .build();
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get specific bot by ID.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get bot", description = "Get specific bot by ID")
    public ResponseEntity<BotDto> getBot(
            @Parameter(description = "Bot ID") @PathVariable String id) {
        
        log.debug("Getting bot with id: {}", id);
        
        Optional<Bot> bot = botService.findById(id);
        return bot.map(b -> ResponseEntity.ok(BotDto.fromEntity(b)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Register bot heartbeat.
     */
    @PostMapping("/{id}/heartbeat")
    @Operation(summary = "Bot heartbeat", description = "Register bot heartbeat")
    @PreAuthorize("hasRole('BOT') or hasRole('ADMIN')")
    public ResponseEntity<BotDto> botHeartbeat(
            @Parameter(description = "Bot ID") @PathVariable String id,
            @Parameter(description = "Bot status information") @RequestBody(required = false) BotDto botStatus) {
        
        log.debug("Heartbeat from bot: {}", id);
        
        try {
            Bot bot = botService.updateHeartbeat(id, botStatus);
            return ResponseEntity.ok(BotDto.fromEntity(bot));
        } catch (Exception e) {
            log.error("Error processing heartbeat for bot: " + id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Assign task to bot.
     */
    @PostMapping("/{id}/assign-task")
    @Operation(summary = "Assign task", description = "Assign task to bot")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SCHEDULER')")
    public ResponseEntity<BotDto> assignTask(
            @Parameter(description = "Bot ID") @PathVariable String id,
            @Parameter(description = "Task assignment details") @Valid @RequestBody TaskAssignmentRequest taskRequest) {
        
        log.info("Assigning task {} to bot: {}", taskRequest.getTaskName(), id);
        
        try {
            Bot bot = botService.assignTask(id, taskRequest);
            return ResponseEntity.ok(BotDto.fromEntity(bot));
        } catch (IllegalStateException e) {
            log.warn("Cannot assign task to bot {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (Exception e) {
            log.error("Error assigning task to bot: " + id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get bot's current tasks.
     */
    @GetMapping("/{id}/tasks")
    @Operation(summary = "Get bot tasks", description = "Get bot's current tasks")
    public ResponseEntity<List<Object>> getBotTasks(
            @Parameter(description = "Bot ID") @PathVariable String id) {
        
        log.debug("Getting tasks for bot: {}", id);
        
        try {
            List<Object> tasks = botService.getBotTasks(id);
            return ResponseEntity.ok(tasks);
        } catch (Exception e) {
            log.error("Error getting tasks for bot: " + id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Mark task as complete.
     */
    @PostMapping("/{id}/complete-task")
    @Operation(summary = "Complete task", description = "Mark task as complete")
    @PreAuthorize("hasRole('BOT') or hasRole('ADMIN')")
    public ResponseEntity<BotDto> completeTask(
            @Parameter(description = "Bot ID") @PathVariable String id,
            @Parameter(description = "Task completion details") @Valid @RequestBody TaskCompletionRequest completionRequest) {
        
        log.info("Completing task {} for bot: {}", completionRequest.getTaskName(), id);
        
        try {
            Bot bot = botService.completeTask(id, completionRequest);
            return ResponseEntity.ok(BotDto.fromEntity(bot));
        } catch (Exception e) {
            log.error("Error completing task for bot: " + id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get bot statistics.
     */
    @GetMapping("/{id}/stats")
    @Operation(summary = "Get bot stats", description = "Get bot performance statistics")
    public ResponseEntity<Object> getBotStats(
            @Parameter(description = "Bot ID") @PathVariable String id,
            @Parameter(description = "Time range in hours") @RequestParam(value = "hours", defaultValue = "24") int hours) {
        
        log.debug("Getting stats for bot: {} for {} hours", id, hours);
        
        try {
            Object stats = botService.getBotStats(id, hours);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error getting stats for bot: " + id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Restart bot.
     */
    @PostMapping("/{id}/restart")
    @Operation(summary = "Restart bot", description = "Restart bot instance")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> restartBot(
            @Parameter(description = "Bot ID") @PathVariable String id) {
        
        log.info("Restarting bot: {}", id);
        
        try {
            botService.restartBot(id);
            return ResponseEntity.accepted().build();
        } catch (Exception e) {
            log.error("Error restarting bot: " + id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Shutdown bot.
     */
    @PostMapping("/{id}/shutdown")
    @Operation(summary = "Shutdown bot", description = "Shutdown bot instance")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> shutdownBot(
            @Parameter(description = "Bot ID") @PathVariable String id) {
        
        log.info("Shutting down bot: {}", id);
        
        try {
            botService.shutdownBot(id);
            return ResponseEntity.accepted().build();
        } catch (Exception e) {
            log.error("Error shutting down bot: " + id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get bot logs.
     */
    @GetMapping("/{id}/logs")
    @Operation(summary = "Get bot logs", description = "Get bot execution logs")
    public ResponseEntity<String> getBotLogs(
            @Parameter(description = "Bot ID") @PathVariable String id,
            @Parameter(description = "Number of lines") @RequestParam(value = "lines", defaultValue = "100") int lines) {
        
        log.debug("Getting logs for bot: {} (last {} lines)", id, lines);
        
        try {
            String logs = botService.getBotLogs(id, lines);
            return ResponseEntity.ok(logs);
        } catch (Exception e) {
            log.error("Error getting logs for bot: " + id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Update bot configuration.
     */
    @PutMapping("/{id}/config")
    @Operation(summary = "Update bot config", description = "Update bot configuration")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BotDto> updateBotConfig(
            @Parameter(description = "Bot ID") @PathVariable String id,
            @Parameter(description = "Bot configuration") @RequestBody String config) {
        
        log.info("Updating config for bot: {}", id);
        
        try {
            Bot bot = botService.updateBotConfig(id, config);
            return ResponseEntity.ok(BotDto.fromEntity(bot));
        } catch (Exception e) {
            log.error("Error updating config for bot: " + id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Scale bot pool.
     */
    @PostMapping("/scale")
    @Operation(summary = "Scale bot pool", description = "Scale the bot pool size")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> scaleBotPool(
            @Parameter(description = "Target pool size") @RequestParam("size") int targetSize,
            @Parameter(description = "Platform") @RequestParam(value = "platform", required = false) String platform) {
        
        log.info("Scaling bot pool to size: {} for platform: {}", targetSize, platform);
        
        try {
            botService.scaleBotPool(targetSize, platform);
            return ResponseEntity.accepted().build();
        } catch (Exception e) {
            log.error("Error scaling bot pool", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get bot pool status.
     */
    @GetMapping("/pool-status")
    @Operation(summary = "Get pool status", description = "Get bot pool status")
    public ResponseEntity<Object> getBotPoolStatus() {
        
        log.debug("Getting bot pool status");
        
        try {
            Object poolStatus = botService.getBotPoolStatus();
            return ResponseEntity.ok(poolStatus);
        } catch (Exception e) {
            log.error("Error getting bot pool status", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}