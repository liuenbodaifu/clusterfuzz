package com.google.clusterfuzz.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Main Spring Boot application class for ClusterFuzz Web.
 */
@SpringBootApplication(scanBasePackages = "com.google.clusterfuzz")
public class ClusterFuzzWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClusterFuzzWebApplication.class, args);
    }
}