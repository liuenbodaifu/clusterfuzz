package com.google.clusterfuzz.core.service;

import com.google.clusterfuzz.core.entity.Bot;
import com.google.clusterfuzz.core.repository.BotRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for BotService.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("BotService Tests")
class BotServiceTest {

    @Mock
    private BotRepository botRepository;

    @InjectMocks
    private BotService botService;

    private Bot testBot;

    @BeforeEach
    void setUp() {
        testBot = Bot.builder()
                .id(1L)
                .name("test-bot-001")
                .platform("linux")
                .version("1.0.0")
                .status(Bot.BotStatus.IDLE)
                .available(true)
                .lastBeatTime(LocalDateTime.now().minusMinutes(2))
                .ipAddress("192.168.1.100")
                .cpuCores(8)
                .memoryMb(16384)
                .supportedEngines(Set.of("libfuzzer", "afl"))
                .tasksCompleted(10L)
                .crashesFound(5L)
                .build();
    }

    @Test
    @DisplayName("Should register new bot")
    void shouldRegisterNewBot() {
        when(botRepository.findByName("test-bot-001")).thenReturn(Optional.empty());
        when(botRepository.save(any(Bot.class))).thenReturn(testBot);

        Bot result = botService.registerBot(testBot);

        assertNotNull(result);
        assertEquals("test-bot-001", result.getName());
        assertEquals(Bot.BotStatus.IDLE, result.getStatus());
        assertTrue(result.getAvailable());

        verify(botRepository).findByName("test-bot-001");
        verify(botRepository).save(any(Bot.class));
    }

    @Test
    @DisplayName("Should update existing bot during registration")
    void shouldUpdateExistingBotDuringRegistration() {
        Bot existingBot = Bot.builder()
                .id(1L)
                .name("test-bot-001")
                .platform("linux")
                .version("0.9.0") // Old version
                .status(Bot.BotStatus.OFFLINE)
                .available(false)
                .build();

        when(botRepository.findByName("test-bot-001")).thenReturn(Optional.of(existingBot));
        when(botRepository.save(any(Bot.class))).thenReturn(testBot);

        Bot result = botService.registerBot(testBot);

        assertNotNull(result);
        assertEquals("test-bot-001", result.getName());
        assertEquals(Bot.BotStatus.IDLE, result.getStatus());
        assertTrue(result.getAvailable());

        verify(botRepository).findByName("test-bot-001");
        verify(botRepository).save(any(Bot.class));
    }

    @Test
    @DisplayName("Should update heartbeat")
    void shouldUpdateHeartbeat() {
        when(botRepository.updateHeartbeat(eq("test-bot-001"), any(LocalDateTime.class))).thenReturn(1);

        botService.updateHeartbeat("test-bot-001");

        verify(botRepository).updateHeartbeat(eq("test-bot-001"), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("Should find available bots")
    void shouldFindAvailableBots() {
        List<Bot> availableBots = List.of(testBot);
        when(botRepository.findAvailableBots(any(LocalDateTime.class))).thenReturn(availableBots);

        List<Bot> result = botService.findAvailableBots();

        assertEquals(1, result.size());
        assertEquals("test-bot-001", result.get(0).getName());

        verify(botRepository).findAvailableBots(any(LocalDateTime.class));
    }

    @Test
    @DisplayName("Should find available bots by platform")
    void shouldFindAvailableBotsByPlatform() {
        List<Bot> availableBots = List.of(testBot);
        when(botRepository.findAvailableBotsByPlatform(eq("linux"), any(LocalDateTime.class)))
                .thenReturn(availableBots);

        List<Bot> result = botService.findAvailableBots("linux");

        assertEquals(1, result.size());
        assertEquals("test-bot-001", result.get(0).getName());
        assertEquals("linux", result.get(0).getPlatform());

        verify(botRepository).findAvailableBotsByPlatform(eq("linux"), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("Should find bots supporting engine")
    void shouldFindBotsSupportingEngine() {
        List<Bot> supportingBots = List.of(testBot);
        when(botRepository.findBotsSupportingEngine("libfuzzer")).thenReturn(supportingBots);

        List<Bot> result = botService.findBotsSupportingEngine("libfuzzer");

        assertEquals(1, result.size());
        assertTrue(result.get(0).getSupportedEngines().contains("libfuzzer"));

        verify(botRepository).findBotsSupportingEngine("libfuzzer");
    }

    @Test
    @DisplayName("Should assign task to available bot")
    void shouldAssignTaskToAvailableBot() {
        when(botRepository.findByName("test-bot-001")).thenReturn(Optional.of(testBot));
        when(botRepository.save(any(Bot.class))).thenReturn(testBot);

        boolean result = botService.assignTask("test-bot-001", "fuzz-task", "{\"type\":\"fuzz\"}", 60);

        assertTrue(result);

        verify(botRepository).findByName("test-bot-001");
        verify(botRepository).save(any(Bot.class));
    }

    @Test
    @DisplayName("Should not assign task to non-existent bot")
    void shouldNotAssignTaskToNonExistentBot() {
        when(botRepository.findByName("non-existent-bot")).thenReturn(Optional.empty());

        boolean result = botService.assignTask("non-existent-bot", "fuzz-task", "{\"type\":\"fuzz\"}", 60);

        assertFalse(result);

        verify(botRepository).findByName("non-existent-bot");
        verify(botRepository, never()).save(any(Bot.class));
    }

    @Test
    @DisplayName("Should not assign task to unavailable bot")
    void shouldNotAssignTaskToUnavailableBot() {
        Bot unavailableBot = Bot.builder()
                .name("test-bot-001")
                .status(Bot.BotStatus.BUSY)
                .available(false)
                .lastBeatTime(LocalDateTime.now().minusMinutes(10)) // Stale
                .build();

        when(botRepository.findByName("test-bot-001")).thenReturn(Optional.of(unavailableBot));

        boolean result = botService.assignTask("test-bot-001", "fuzz-task", "{\"type\":\"fuzz\"}", 60);

        assertFalse(result);

        verify(botRepository).findByName("test-bot-001");
        verify(botRepository, never()).save(any(Bot.class));
    }

    @Test
    @DisplayName("Should complete task")
    void shouldCompleteTask() {
        Bot busyBot = Bot.builder()
                .name("test-bot-001")
                .status(Bot.BotStatus.BUSY)
                .available(false)
                .taskName("fuzz-task")
                .taskPayload("{\"type\":\"fuzz\"}")
                .taskEndTime(LocalDateTime.now().plusHours(1))
                .tasksCompleted(10L)
                .build();

        when(botRepository.findByName("test-bot-001")).thenReturn(Optional.of(busyBot));
        when(botRepository.save(any(Bot.class))).thenReturn(busyBot);

        botService.completeTask("test-bot-001");

        verify(botRepository).findByName("test-bot-001");
        verify(botRepository).save(any(Bot.class));
    }

    @Test
    @DisplayName("Should record crash found")
    void shouldRecordCrashFound() {
        when(botRepository.findByName("test-bot-001")).thenReturn(Optional.of(testBot));
        when(botRepository.incrementCrashesFound(1L)).thenReturn(1);

        botService.recordCrashFound("test-bot-001");

        verify(botRepository).findByName("test-bot-001");
        verify(botRepository).incrementCrashesFound(1L);
    }

    @Test
    @DisplayName("Should set bot error")
    void shouldSetBotError() {
        when(botRepository.findByName("test-bot-001")).thenReturn(Optional.of(testBot));
        when(botRepository.save(any(Bot.class))).thenReturn(testBot);

        botService.setBotError("test-bot-001", "Connection timeout");

        verify(botRepository).findByName("test-bot-001");
        verify(botRepository).save(any(Bot.class));
    }

    @Test
    @DisplayName("Should clear bot error")
    void shouldClearBotError() {
        Bot errorBot = Bot.builder()
                .name("test-bot-001")
                .status(Bot.BotStatus.ERROR)
                .lastError("Previous error")
                .available(false)
                .build();

        when(botRepository.findByName("test-bot-001")).thenReturn(Optional.of(errorBot));
        when(botRepository.save(any(Bot.class))).thenReturn(errorBot);

        botService.clearBotError("test-bot-001");

        verify(botRepository).findByName("test-bot-001");
        verify(botRepository).save(any(Bot.class));
    }

    @Test
    @DisplayName("Should get bot by name")
    void shouldGetBotByName() {
        when(botRepository.findByName("test-bot-001")).thenReturn(Optional.of(testBot));

        Optional<Bot> result = botService.getBotByName("test-bot-001");

        assertTrue(result.isPresent());
        assertEquals("test-bot-001", result.get().getName());

        verify(botRepository).findByName("test-bot-001");
    }

    @Test
    @DisplayName("Should get bots by platform")
    void shouldGetBotsByPlatform() {
        List<Bot> linuxBots = List.of(testBot);
        when(botRepository.findByPlatform("linux")).thenReturn(linuxBots);

        List<Bot> result = botService.getBotsByPlatform("linux");

        assertEquals(1, result.size());
        assertEquals("linux", result.get(0).getPlatform());

        verify(botRepository).findByPlatform("linux");
    }

    @Test
    @DisplayName("Should get bots by status")
    void shouldGetBotsByStatus() {
        List<Bot> idleBots = List.of(testBot);
        when(botRepository.findByStatus(Bot.BotStatus.IDLE)).thenReturn(idleBots);

        List<Bot> result = botService.getBotsByStatus(Bot.BotStatus.IDLE);

        assertEquals(1, result.size());
        assertEquals(Bot.BotStatus.IDLE, result.get(0).getStatus());

        verify(botRepository).findByStatus(Bot.BotStatus.IDLE);
    }

    @Test
    @DisplayName("Should get bot statistics")
    void shouldGetBotStatistics() {
        when(botRepository.count()).thenReturn(10L);
        when(botRepository.countActiveBots(any(LocalDateTime.class))).thenReturn(8L);
        when(botRepository.countAvailableBots(any(LocalDateTime.class))).thenReturn(5L);
        when(botRepository.countBusyBots(any(LocalDateTime.class))).thenReturn(3L);
        when(botRepository.getBotStatisticsByPlatform()).thenReturn(List.of());

        BotService.BotStatistics stats = botService.getBotStatistics();

        assertNotNull(stats);
        assertEquals(10L, stats.getTotalBots());
        assertEquals(8L, stats.getActiveBots());
        assertEquals(5L, stats.getAvailableBots());
        assertEquals(3L, stats.getBusyBots());
        assertEquals(2L, stats.getOfflineBots()); // total - active

        verify(botRepository).count();
        verify(botRepository).countActiveBots(any(LocalDateTime.class));
        verify(botRepository).countAvailableBots(any(LocalDateTime.class));
        verify(botRepository).countBusyBots(any(LocalDateTime.class));
        verify(botRepository).getBotStatisticsByPlatform();
    }

    @Test
    @DisplayName("Should restart bot")
    void shouldRestartBot() {
        when(botRepository.findByName("test-bot-001")).thenReturn(Optional.of(testBot));
        when(botRepository.save(any(Bot.class))).thenReturn(testBot);

        botService.restartBot("test-bot-001");

        verify(botRepository).findByName("test-bot-001");
        verify(botRepository).save(any(Bot.class));
    }

    @Test
    @DisplayName("Should shutdown bot")
    void shouldShutdownBot() {
        when(botRepository.findByName("test-bot-001")).thenReturn(Optional.of(testBot));
        when(botRepository.save(any(Bot.class))).thenReturn(testBot);

        botService.shutdownBot("test-bot-001");

        verify(botRepository).findByName("test-bot-001");
        verify(botRepository).save(any(Bot.class));
    }

    @Test
    @DisplayName("Should update performance metrics")
    void shouldUpdatePerformanceMetrics() {
        when(botRepository.findByName("test-bot-001")).thenReturn(Optional.of(testBot));
        when(botRepository.updatePerformanceMetrics(1L, "{\"cpu\":50}")).thenReturn(1);

        botService.updatePerformanceMetrics("test-bot-001", "{\"cpu\":50}");

        verify(botRepository).findByName("test-bot-001");
        verify(botRepository).updatePerformanceMetrics(1L, "{\"cpu\":50}");
    }

    @Test
    @DisplayName("Should delete bot")
    void shouldDeleteBot() {
        when(botRepository.findByName("test-bot-001")).thenReturn(Optional.of(testBot));

        botService.deleteBot("test-bot-001");

        verify(botRepository).findByName("test-bot-001");
        verify(botRepository).delete(testBot);
    }
}