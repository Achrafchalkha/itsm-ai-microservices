package com.itsm.assignment.infrastructure.kafka;

import com.itsm.assignment.application.service.AssignmentService;
import com.itsm.assignment.infrastructure.kafka.event.TicketCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

/**
 * Kafka listener for ticket events
 * Triggers automatic assignment when tickets are created
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TicketEventListener {
    
    private final AssignmentService assignmentService;
    
    /**
     * Handle ticket created events from ticket-service
     * Triggers automatic assignment process
     */
    @KafkaListener(topics = "ticket.created", groupId = "assignment-service-group",
                   containerFactory = "ticketKafkaListenerContainerFactory")
    public void handleTicketCreated(
            @Payload TicketCreatedEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {
        
        log.info("Received TicketCreatedEvent from topic: {}, partition: {}, offset: {}, ticketId: {}", 
                topic, partition, offset, event.getTicketId());
        
        try {
            // Process automatic assignment
            assignmentService.processAutomaticAssignment(event);
            
            log.info("Successfully processed automatic assignment for ticket: {}", event.getTicketId());
            
            // Acknowledge the message
            acknowledgment.acknowledge();
            
        } catch (Exception e) {
            log.error("Error processing ticket created event for ticket {}: {}", 
                    event.getTicketId(), e.getMessage(), e);
            
            // For now, acknowledge even on error to avoid infinite retries
            // In production, you might want to implement dead letter queue
            acknowledgment.acknowledge();
        }
    }
    
    /**
     * Handle technician status updates
     * May trigger reassignment if technician becomes unavailable
     */
    @KafkaListener(topics = "technician.status.updated", groupId = "assignment-service-group")
    public void handleTechnicianStatusUpdated(
            @Payload TechnicianStatusUpdatedEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            Acknowledgment acknowledgment) {
        
        log.info("Received TechnicianStatusUpdatedEvent from topic: {}, technicianId: {}, status: {}", 
                topic, event.getTechnicianId(), event.getStatus());
        
        try {
            // If technician becomes inactive, handle reassignment
            if (!event.isActif()) {
                assignmentService.handleTechnicianUnavailable(event.getTechnicianId());
            }
            
            acknowledgment.acknowledge();
            
        } catch (Exception e) {
            log.error("Error processing technician status update for technician {}: {}", 
                    event.getTechnicianId(), e.getMessage(), e);
            acknowledgment.acknowledge();
        }
    }
    
    /**
     * Event for technician status updates
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class TechnicianStatusUpdatedEvent {
        private java.util.UUID technicianId;
        private String status;
        private boolean actif;
        private java.time.LocalDateTime updatedAt;
    }
}
