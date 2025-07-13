package com.itsm.auth_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {"com.itsm.auth", "com.itsm.auth_service"})
@EntityScan({"com.itsm.auth.infrastructure.persistence.entity"})
@EnableJpaRepositories({"com.itsm.auth.infrastructure.persistence.repository"})
public class AuthServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(AuthServiceApplication.class, args);
	}

}
