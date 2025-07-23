package com.itsm.ticket.infrastructure.kafka;

import com.itsm.ticket.infrastructure.kafka.event.TicketCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * Service for publishing ticket events to Kafka
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TicketEventPublisher {
    
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    private static final String TICKET_CREATED_TOPIC = "ticket.created";
    
    /**
     * Publish ticket created event for assignment-service
     */
    public void publishTicketCreated(TicketCreatedEvent event) {
        try {
            log.info("Publishing ticket created event for assignment-service: {}", event.getTicketId());
            kafkaTemplate.send(TICKET_CREATED_TOPIC, event.getTicketId().toString(), event);
            log.info("Successfully published ticket created event to assignment-service: {}", event);
        } catch (Exception e) {
            log.error("Error publishing ticket created event to assignment-service: {}", e.getMessage(), e);
        }
    }
}
