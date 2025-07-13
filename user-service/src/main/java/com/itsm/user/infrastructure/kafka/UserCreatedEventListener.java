package com.itsm.user.infrastructure.kafka;

import com.itsm.user.application.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * Kafka event listener for UserCreatedEvent from auth-service
 * Handles the creation of user business profiles when users are registered
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class UserCreatedEventListener {
    
    private final UserService userService;
    
    /**
     * Listen for user creation events from auth-service
     * Creates a business profile for the newly registered user
     */
    @KafkaListener(
            topics = "user-created",
            groupId = "user-service-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleUserCreated(
            @Payload UserCreatedEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {
        
        log.info("Received UserCreatedEvent from topic: {}, partition: {}, offset: {}, userId: {}, email: {}", 
                topic, partition, offset, event.getUserId(), event.getEmail());
        
        try {
            // Create user business profile from auth service event
            userService.creerProfilUtilisateurDepuisAuth(event);
            
            log.info("Successfully created user profile for userId: {}, email: {}", 
                    event.getUserId(), event.getEmail());
            
            // Acknowledge the message
            acknowledgment.acknowledge();
            
        } catch (Exception e) {
            log.error("Error processing UserCreatedEvent for userId: {}, email: {}", 
                    event.getUserId(), event.getEmail(), e);
            
            // In a production environment, you might want to:
            // 1. Send to a dead letter queue
            // 2. Implement retry logic
            // 3. Alert monitoring systems
            // For now, we'll acknowledge to prevent infinite retries
            acknowledgment.acknowledge();
        }
    }
}
