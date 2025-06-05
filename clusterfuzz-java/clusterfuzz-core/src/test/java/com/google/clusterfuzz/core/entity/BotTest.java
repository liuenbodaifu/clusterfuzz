package com.google.clusterfuzz.core.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Bot entity.
 */
@DisplayName("Bot Entity Tests")
class BotTest {

    private Validator validator;
    private Bot.BotBuilder validBotBuilder;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        
        validBotBuilder = Bot.builder()
                .name("test-bot-001")
                .platform("linux")
                .version("1.0.0")
                .status(Bot.BotStatus.IDLE)
                .ipAddress("192.168.1.100")
                .port(8080)
                .cpuArchitecture("x86_64")
                .cpuCores(8)
                .memoryMb(16384)
                .diskSpaceMb(500000)
                .available(true)
                .tasksCompleted(0L)
                .crashesFound(0L);
    }

    @Test
    @DisplayName("Should create valid bot with all required fields")
    void shouldCreateValidBot() {
        Bot bot = validBotBuilder.build();
        
        Set<ConstraintViolation<Bot>> violations = validator.validate(bot);
        assertTrue(violations.isEmpty(), "Valid bot should have no validation violations");
        
        assertEquals("test-bot-001", bot.getName());
        assertEquals("linux", bot.getPlatform());
        assertEquals(Bot.BotStatus.IDLE, bot.getStatus());
        assertTrue(bot.getAvailable());
    }

    @Test
    @DisplayName("Should fail validation when name is null")
    void shouldFailValidationWhenNameIsNull() {
        Bot bot = validBotBuilder.name(null).build();
        
        Set<ConstraintViolation<Bot>> violations = validator.validate(bot);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("name")));
    }

    @Test
    @DisplayName("Should fail validation when name is blank")
    void shouldFailValidationWhenNameIsBlank() {
        Bot bot = validBotBuilder.name("   ").build();
        
        Set<ConstraintViolation<Bot>> violations = validator.validate(bot);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("name")));
    }

    @Test
    @DisplayName("Should fail validation when name exceeds max length")
    void shouldFailValidationWhenNameExceedsMaxLength() {
        String longName = "a".repeat(101); // Max is 100
        Bot bot = validBotBuilder.name(longName).build();
        
        Set<ConstraintViolation<Bot>> violations = validator.validate(bot);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("name")));
    }

    @Test
    @DisplayName("Should fail validation when platform is null")
    void shouldFailValidationWhenPlatformIsNull() {
        Bot bot = validBotBuilder.platform(null).build();
        
        Set<ConstraintViolation<Bot>> violations = validator.validate(bot);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("platform")));
    }

    @Test
    @DisplayName("Should fail validation when status is null")
    void shouldFailValidationWhenStatusIsNull() {
        Bot bot = validBotBuilder.status(null).build();
        
        Set<ConstraintViolation<Bot>> violations = validator.validate(bot);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("status")));
    }

    @ParameterizedTest
    @EnumSource(Bot.BotStatus.class)
    @DisplayName("Should accept all valid bot statuses")
    void shouldAcceptAllValidBotStatuses(Bot.BotStatus status) {
        Bot bot = validBotBuilder.status(status).build();
        
        Set<ConstraintViolation<Bot>> violations = validator.validate(bot);
        assertTrue(violations.isEmpty());
        assertEquals(status, bot.getStatus());
    }

    @ParameterizedTest
    @ValueSource(strings = {"linux", "windows", "macos", "freebsd"})
    @DisplayName("Should accept valid platform names")
    void shouldAcceptValidPlatformNames(String platform) {
        Bot bot = validBotBuilder.platform(platform).build();
        
        Set<ConstraintViolation<Bot>> violations = validator.validate(bot);
        assertTrue(violations.isEmpty());
        assertEquals(platform, bot.getPlatform());
    }

    @Test
    @DisplayName("Should correctly identify alive bot")
    void shouldCorrectlyIdentifyAliveBot() {
        Bot bot = validBotBuilder
                .lastBeatTime(LocalDateTime.now().minusMinutes(2))
                .build();
        
        assertTrue(bot.isAlive(), "Bot with recent heartbeat should be alive");
    }

    @Test
    @DisplayName("Should correctly identify dead bot")
    void shouldCorrectlyIdentifyDeadBot() {
        Bot bot = validBotBuilder
                .lastBeatTime(LocalDateTime.now().minusMinutes(10))
                .build();
        
        assertFalse(bot.isAlive(), "Bot with old heartbeat should be dead");
    }

    @Test
    @DisplayName("Should correctly identify bot without heartbeat as dead")
    void shouldCorrectlyIdentifyBotWithoutHeartbeatAsDead() {
        Bot bot = validBotBuilder
                .lastBeatTime(null)
                .build();
        
        assertFalse(bot.isAlive(), "Bot without heartbeat should be dead");
    }

    @Test
    @DisplayName("Should correctly identify available bot for tasks")
    void shouldCorrectlyIdentifyAvailableBotForTasks() {
        Bot bot = validBotBuilder
                .available(true)
                .status(Bot.BotStatus.IDLE)
                .lastBeatTime(LocalDateTime.now().minusMinutes(1))
                .build();
        
        assertTrue(bot.isAvailableForTasks(), "Idle, available, alive bot should be available for tasks");
    }

    @Test
    @DisplayName("Should correctly identify unavailable bot for tasks when busy")
    void shouldCorrectlyIdentifyUnavailableBotForTasksWhenBusy() {
        Bot bot = validBotBuilder
                .available(true)
                .status(Bot.BotStatus.BUSY)
                .lastBeatTime(LocalDateTime.now().minusMinutes(1))
                .build();
        
        assertFalse(bot.isAvailableForTasks(), "Busy bot should not be available for tasks");
    }

    @Test
    @DisplayName("Should correctly identify unavailable bot for tasks when not available")
    void shouldCorrectlyIdentifyUnavailableBotForTasksWhenNotAvailable() {
        Bot bot = validBotBuilder
                .available(false)
                .status(Bot.BotStatus.IDLE)
                .lastBeatTime(LocalDateTime.now().minusMinutes(1))
                .build();
        
        assertFalse(bot.isAvailableForTasks(), "Unavailable bot should not be available for tasks");
    }

    @Test
    @DisplayName("Should correctly identify unavailable bot for tasks when dead")
    void shouldCorrectlyIdentifyUnavailableBotForTasksWhenDead() {
        Bot bot = validBotBuilder
                .available(true)
                .status(Bot.BotStatus.IDLE)
                .lastBeatTime(LocalDateTime.now().minusMinutes(10))
                .build();
        
        assertFalse(bot.isAvailableForTasks(), "Dead bot should not be available for tasks");
    }

    @Test
    @DisplayName("Should update heartbeat correctly")
    void shouldUpdateHeartbeatCorrectly() {
        Bot bot = validBotBuilder.build();
        LocalDateTime beforeUpdate = LocalDateTime.now().minusSeconds(1);
        
        bot.updateHeartbeat();
        
        assertNotNull(bot.getLastBeatTime());
        assertTrue(bot.getLastBeatTime().isAfter(beforeUpdate));
        assertTrue(bot.getLastBeatTime().isBefore(LocalDateTime.now().plusSeconds(1)));
    }

    @Test
    @DisplayName("Should assign task correctly")
    void shouldAssignTaskCorrectly() {
        Bot bot = validBotBuilder.build();
        String taskName = "test-task";
        String taskPayload = "{\"type\":\"fuzz\"}";
        LocalDateTime taskEndTime = LocalDateTime.now().plusHours(1);
        
        bot.assignTask(taskName, taskPayload, taskEndTime);
        
        assertEquals(taskName, bot.getTaskName());
        assertEquals(taskPayload, bot.getTaskPayload());
        assertEquals(taskEndTime, bot.getTaskEndTime());
        assertEquals(Bot.BotStatus.BUSY, bot.getStatus());
        assertFalse(bot.getAvailable());
    }

    @Test
    @DisplayName("Should complete task correctly")
    void shouldCompleteTaskCorrectly() {
        Bot bot = validBotBuilder
                .taskName("test-task")
                .taskPayload("{\"type\":\"fuzz\"}")
                .taskEndTime(LocalDateTime.now().plusHours(1))
                .status(Bot.BotStatus.BUSY)
                .available(false)
                .tasksCompleted(5L)
                .build();
        
        bot.completeTask();
        
        assertNull(bot.getTaskName());
        assertNull(bot.getTaskPayload());
        assertNull(bot.getTaskEndTime());
        assertEquals(Bot.BotStatus.IDLE, bot.getStatus());
        assertTrue(bot.getAvailable());
        assertEquals(6L, bot.getTasksCompleted());
    }

    @Test
    @DisplayName("Should record crash found correctly")
    void shouldRecordCrashFoundCorrectly() {
        Bot bot = validBotBuilder.crashesFound(10L).build();
        
        bot.recordCrashFound();
        
        assertEquals(11L, bot.getCrashesFound());
    }

    @Test
    @DisplayName("Should set error correctly")
    void shouldSetErrorCorrectly() {
        Bot bot = validBotBuilder.build();
        String errorMessage = "Connection timeout";
        
        bot.setError(errorMessage);
        
        assertEquals(Bot.BotStatus.ERROR, bot.getStatus());
        assertEquals(errorMessage, bot.getLastError());
        assertFalse(bot.getAvailable());
    }

    @Test
    @DisplayName("Should clear error correctly")
    void shouldClearErrorCorrectly() {
        Bot bot = validBotBuilder
                .status(Bot.BotStatus.ERROR)
                .lastError("Previous error")
                .available(false)
                .build();
        
        bot.clearError();
        
        assertEquals(Bot.BotStatus.IDLE, bot.getStatus());
        assertNull(bot.getLastError());
        assertTrue(bot.getAvailable());
    }

    @Test
    @DisplayName("Should handle environment variables correctly")
    void shouldHandleEnvironmentVariablesCorrectly() {
        Map<String, String> environment = Map.of(
                "JAVA_HOME", "/usr/lib/jvm/java-17",
                "PATH", "/usr/bin:/bin",
                "FUZZER_TIMEOUT", "3600"
        );
        
        Bot bot = validBotBuilder.environment(environment).build();
        
        assertEquals(environment, bot.getEnvironment());
        assertEquals("/usr/lib/jvm/java-17", bot.getEnvironment().get("JAVA_HOME"));
    }

    @Test
    @DisplayName("Should handle supported engines correctly")
    void shouldHandleSupportedEnginesCorrectly() {
        Set<String> engines = Set.of("libfuzzer", "afl", "honggfuzz");
        
        Bot bot = validBotBuilder.supportedEngines(engines).build();
        
        assertEquals(engines, bot.getSupportedEngines());
        assertTrue(bot.getSupportedEngines().contains("libfuzzer"));
        assertTrue(bot.getSupportedEngines().contains("afl"));
    }

    @Test
    @DisplayName("Should validate IP address length")
    void shouldValidateIpAddressLength() {
        String longIpAddress = "a".repeat(46); // Max is 45 for IPv6
        Bot bot = validBotBuilder.ipAddress(longIpAddress).build();
        
        Set<ConstraintViolation<Bot>> violations = validator.validate(bot);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("ipAddress")));
    }

    @Test
    @DisplayName("Should accept valid IPv4 address")
    void shouldAcceptValidIpv4Address() {
        Bot bot = validBotBuilder.ipAddress("192.168.1.100").build();
        
        Set<ConstraintViolation<Bot>> violations = validator.validate(bot);
        assertTrue(violations.isEmpty());
        assertEquals("192.168.1.100", bot.getIpAddress());
    }

    @Test
    @DisplayName("Should accept valid IPv6 address")
    void shouldAcceptValidIpv6Address() {
        Bot bot = validBotBuilder.ipAddress("2001:0db8:85a3:0000:0000:8a2e:0370:7334").build();
        
        Set<ConstraintViolation<Bot>> violations = validator.validate(bot);
        assertTrue(violations.isEmpty());
        assertEquals("2001:0db8:85a3:0000:0000:8a2e:0370:7334", bot.getIpAddress());
    }

    @Test
    @DisplayName("Should handle resource specifications correctly")
    void shouldHandleResourceSpecificationsCorrectly() {
        Bot bot = validBotBuilder
                .cpuCores(16)
                .memoryMb(32768)
                .diskSpaceMb(1000000)
                .build();
        
        assertEquals(16, bot.getCpuCores());
        assertEquals(32768, bot.getMemoryMb());
        assertEquals(1000000, bot.getDiskSpaceMb());
    }

    @Test
    @DisplayName("Should initialize default values correctly")
    void shouldInitializeDefaultValuesCorrectly() {
        Bot bot = Bot.builder()
                .name("test-bot")
                .platform("linux")
                .build();
        
        assertEquals(Bot.BotStatus.IDLE, bot.getStatus());
        assertTrue(bot.getAvailable());
        assertEquals(0L, bot.getTasksCompleted());
        assertEquals(0L, bot.getCrashesFound());
    }
}