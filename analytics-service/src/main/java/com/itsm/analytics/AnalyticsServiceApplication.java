package com.itsm.analytics;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Analytics Service Application
 * Provides KPIs, dashboards and performance monitoring for ITSM system
 * 
 * Features:
 * - ADMIN global dashboards and KPIs
 * - MANAGER team performance monitoring
 * - SLA configuration and monitoring
 * - Satisfaction scoring system
 * - Real-time analytics via Kafka
 * - Scheduled aggregation jobs
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableKafka
@EnableAsync
@EnableScheduling
public class AnalyticsServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AnalyticsServiceApplication.class, args);
    }
}
