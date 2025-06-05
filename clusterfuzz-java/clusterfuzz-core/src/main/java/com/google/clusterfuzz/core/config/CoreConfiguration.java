package com.google.clusterfuzz.core.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Core configuration for ClusterFuzz Java application.
 * Configures JPA, auditing, and transaction management.
 */
@Configuration
@EnableJpaRepositories(basePackages = "com.google.clusterfuzz.core.repository")
@EntityScan(basePackages = "com.google.clusterfuzz.core.entity")
@EnableJpaAuditing
@EnableTransactionManagement
public class CoreConfiguration {
    // Configuration class for core module
}