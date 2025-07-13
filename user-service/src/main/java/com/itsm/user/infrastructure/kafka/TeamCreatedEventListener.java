package com.itsm.user.infrastructure.kafka;

import com.itsm.user.application.service.TeamService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * Kafka event listener for TeamCreatedEvent from auth-service
 * Handles the creation of team business profiles when teams are created by admin
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TeamCreatedEventListener {
    
    private final TeamService teamService;
    
    /**
     * Listen for team creation events from auth-service
     * Creates a business profile for the newly created team
     */
    @KafkaListener(
            topics = "team-created",
            groupId = "user-service-group",
            containerFactory = "teamKafkaListenerContainerFactory"
    )
    public void handleTeamCreated(
            @Payload TeamCreatedEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {
        
        log.info("Received TeamCreatedEvent from topic: {}, partition: {}, offset: {}, teamId: {}, teamName: {}", 
                topic, partition, offset, event.getTeamId(), event.getTeamName());
        
        try {
            // Create team business profile from auth service event
            teamService.creerEquipeDepuisAuth(event);
            
            log.info("Successfully created team profile for teamId: {}, teamName: {}", 
                    event.getTeamId(), event.getTeamName());
            
            // Acknowledge the message
            acknowledgment.acknowledge();
            
        } catch (Exception e) {
            log.error("Error processing TeamCreatedEvent for teamId: {}, teamName: {}", 
                    event.getTeamId(), event.getTeamName(), e);
            
            // In a production environment, you might want to:
            // 1. Send to a dead letter queue
            // 2. Implement retry logic
            // 3. Alert monitoring systems
            // For now, we'll acknowledge to prevent infinite retries
            acknowledgment.acknowledge();
        }
    }
}
