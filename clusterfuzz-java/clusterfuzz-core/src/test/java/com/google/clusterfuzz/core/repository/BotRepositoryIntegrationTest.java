package com.google.clusterfuzz.core.repository;

import com.google.clusterfuzz.core.entity.Bot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for BotRepository.
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("BotRepository Integration Tests")
class BotRepositoryIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private BotRepository botRepository;

    private Bot testBot1;
    private Bot testBot2;
    private Bot testBot3;

    @BeforeEach
    void setUp() {
        // Create test bots
        testBot1 = Bot.builder()
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

        testBot2 = Bot.builder()
                .name("test-bot-002")
                .platform("windows")
                .version("1.0.0")
                .status(Bot.BotStatus.BUSY)
                .available(false)
                .lastBeatTime(LocalDateTime.now().minusMinutes(1))
                .taskName("fuzz-task")
                .taskEndTime(LocalDateTime.now().plusHours(1))
                .ipAddress("192.168.1.101")
                .cpuCores(4)
                .memoryMb(8192)
                .supportedEngines(Set.of("libfuzzer"))
                .tasksCompleted(20L)
                .crashesFound(15L)
                .build();

        testBot3 = Bot.builder()
                .name("test-bot-003")
                .platform("linux")
                .version("1.1.0")
                .status(Bot.BotStatus.OFFLINE)
                .available(false)
                .lastBeatTime(LocalDateTime.now().minusMinutes(10)) // Stale
                .ipAddress("192.168.1.102")
                .cpuCores(16)
                .memoryMb(32768)
                .supportedEngines(Set.of("afl", "honggfuzz"))
                .tasksCompleted(5L)
                .crashesFound(2L)
                .build();

        // Persist test data
        entityManager.persistAndFlush(testBot1);
        entityManager.persistAndFlush(testBot2);
        entityManager.persistAndFlush(testBot3);
    }

    @Test
    @DisplayName("Should find bot by name")
    void shouldFindBotByName() {
        Optional<Bot> found = botRepository.findByName("test-bot-001");
        
        assertTrue(found.isPresent());
        assertEquals("test-bot-001", found.get().getName());
        assertEquals("linux", found.get().getPlatform());
    }

    @Test
    @DisplayName("Should return empty when bot name not found")
    void shouldReturnEmptyWhenBotNameNotFound() {
        Optional<Bot> found = botRepository.findByName("non-existent-bot");
        
        assertFalse(found.isPresent());
    }

    @Test
    @DisplayName("Should find bots by platform")
    void shouldFindBotsByPlatform() {
        List<Bot> linuxBots = botRepository.findByPlatform("linux");
        
        assertEquals(2, linuxBots.size());
        assertTrue(linuxBots.stream().allMatch(bot -> "linux".equals(bot.getPlatform())));
        assertTrue(linuxBots.stream().anyMatch(bot -> "test-bot-001".equals(bot.getName())));
        assertTrue(linuxBots.stream().anyMatch(bot -> "test-bot-003".equals(bot.getName())));
    }

    @Test
    @DisplayName("Should find bots by status")
    void shouldFindBotsByStatus() {
        List<Bot> idleBots = botRepository.findByStatus(Bot.BotStatus.IDLE);
        
        assertEquals(1, idleBots.size());
        assertEquals("test-bot-001", idleBots.get(0).getName());
        assertEquals(Bot.BotStatus.IDLE, idleBots.get(0).getStatus());
    }

    @Test
    @DisplayName("Should find available bots")
    void shouldFindAvailableBots() {
        LocalDateTime cutoffTime = LocalDateTime.now().minusMinutes(5);
        List<Bot> availableBots = botRepository.findAvailableBots(cutoffTime);
        
        assertEquals(1, availableBots.size());
        assertEquals("test-bot-001", availableBots.get(0).getName());
        assertTrue(availableBots.get(0).getAvailable());
        assertEquals(Bot.BotStatus.IDLE, availableBots.get(0).getStatus());
    }

    @Test
    @DisplayName("Should count active bots")
    void shouldCountActiveBots() {
        LocalDateTime cutoffTime = LocalDateTime.now().minusMinutes(5);
        long activeCount = botRepository.countActiveBots(cutoffTime);
        
        assertEquals(2L, activeCount); // testBot1 and testBot2 are active
    }

    @Test
    @DisplayName("Should update heartbeat")
    void shouldUpdateHeartbeat() {
        LocalDateTime newHeartbeat = LocalDateTime.now();
        int updated = botRepository.updateHeartbeat("test-bot-001", newHeartbeat);
        
        assertEquals(1, updated);
        
        // Verify the update
        entityManager.clear(); // Clear persistence context to force reload
        Optional<Bot> bot = botRepository.findByName("test-bot-001");
        assertTrue(bot.isPresent());
        assertTrue(bot.get().getLastBeatTime().isAfter(newHeartbeat.minusSeconds(1)));
    }
}