package com.itsm.user_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.itsm.user")
@EnableDiscoveryClient
@EnableJpaRepositories(basePackages = "com.itsm.user.infrastructure.persistence")
@EntityScan(basePackages = "com.itsm.user.infrastructure.persistence")
public class UserServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(UserServiceApplication.class, args);
	}

}
