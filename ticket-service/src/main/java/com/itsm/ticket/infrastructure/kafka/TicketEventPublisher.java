package com.itsm.ticket.infrastructure.kafka;

import com.itsm.ticket.infrastructure.kafka.event.TicketCreatedEvent;
import com.itsm.ticket.infrastructure.kafka.event.TicketUpdatedEvent;
import com.itsm.ticket.infrastructure.kafka.event.TicketNoteAddedEvent;
import com.itsm.ticket.infrastructure.kafka.event.TicketStatusChangedEvent;
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
    private static final String TICKET_UPDATED_TOPIC = "ticket.updated";
    private static final String TICKET_NOTE_ADDED_TOPIC = "ticket.note.added";
    private static final String TICKET_STATUS_CHANGED_TOPIC = "ticket.status.changed";

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

    /**
     * Publish ticket updated event for notifications
     */
    public void publishTicketUpdated(TicketUpdatedEvent event) {
        try {
            log.info("Publishing ticket updated event: {}", event.getTicketId());
            kafkaTemplate.send(TICKET_UPDATED_TOPIC, event.getTicketId().toString(), event);
            log.info("Successfully published ticket updated event: {}", event);
        } catch (Exception e) {
            log.error("Error publishing ticket updated event: {}", e.getMessage(), e);
        }
    }

    /**
     * Publish ticket note added event for user notifications
     */
    public void publishTicketNoteAdded(TicketNoteAddedEvent event) {
        try {
            log.info("Publishing ticket note added event: {}", event.getTicketId());
            kafkaTemplate.send(TICKET_NOTE_ADDED_TOPIC, event.getTicketId().toString(), event);
            log.info("Successfully published ticket note added event: {}", event);
        } catch (Exception e) {
            log.error("Error publishing ticket note added event: {}", e.getMessage(), e);
        }
    }

    /**
     * Publish ticket status changed event for user notifications
     */
    public void publishTicketStatusChanged(TicketStatusChangedEvent event) {
        try {
            log.info("Publishing ticket status changed event: {} - {} -> {}",
                    event.getTicketId(), event.getOldStatus(), event.getNewStatus());
            kafkaTemplate.send(TICKET_STATUS_CHANGED_TOPIC, event.getTicketId().toString(), event);
            log.info("Successfully published ticket status changed event: {}", event);
        } catch (Exception e) {
            log.error("Error publishing ticket status changed event: {}", e.getMessage(), e);
        }
    }
}
