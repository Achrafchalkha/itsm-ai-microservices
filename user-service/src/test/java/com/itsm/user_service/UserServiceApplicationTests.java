package com.itsm.user_service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.kafka.bootstrap-servers=localhost:9092",
    "eureka.client.enabled=false"
})
class UserServiceApplicationTests {

	@Test
	void contextLoads() {
		// This test verifies that the Spring context loads successfully
	}

}
