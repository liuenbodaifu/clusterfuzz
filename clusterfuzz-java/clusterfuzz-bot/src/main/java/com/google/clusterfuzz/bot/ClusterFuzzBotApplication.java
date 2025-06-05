package com.google.clusterfuzz.bot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main Spring Boot application class for ClusterFuzz Bot.
 * Handles fuzzing tasks, testcase processing, and worker operations.
 */
@SpringBootApplication(scanBasePackages = "com.google.clusterfuzz")
@EnableScheduling
public class ClusterFuzzBotApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClusterFuzzBotApplication.class, args);
    }
}