package com.itsm.notifications.infrastructure.kafka;

import com.itsm.notifications.application.service.NotificationService;
import com.itsm.notifications.infrastructure.kafka.event.AssignmentCreatedEvent;
import com.itsm.notifications.infrastructure.kafka.event.AssignmentFailedEvent;
import com.itsm.notifications.infrastructure.kafka.event.AssignmentReassignedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

/**
 * Kafka listener for assignment events
 * Processes assignment events and creates appropriate notifications
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AssignmentEventListener {
    
    private final NotificationService notificationService;
    
    /**
     * Handle assignment created events from assignment-service
     * Creates notification for the assigned technician
     */
    @KafkaListener(topics = "assignment.created", groupId = "notifications-service-group",
                   containerFactory = "assignmentCreatedKafkaListenerContainerFactory")
    public void handleAssignmentCreated(
            @Payload AssignmentCreatedEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {
        
        log.info("Received AssignmentCreatedEvent from topic: {}, partition: {}, offset: {}, ticketId: {}, technicianId: {}", 
                topic, partition, offset, event.getTicketId(), event.getTechnicianId());
        
        try {
            // Create notification for assigned technician
            notificationService.createTicketAssignmentNotification(event);
            
            log.info("Successfully created assignment notification for technician: {}", event.getTechnicianId());
            
            // Acknowledge the message
            acknowledgment.acknowledge();
            
        } catch (Exception e) {
            log.error("Error processing assignment created event for ticket {}: {}", 
                    event.getTicketId(), e.getMessage(), e);
            
            // Acknowledge to avoid infinite retries (notifications are not critical for system operation)
            acknowledgment.acknowledge();
        }
    }
    
    /**
     * Handle assignment reassigned events from assignment-service
     * Creates notifications for both old and new technicians
     */
    @KafkaListener(topics = "assignment.reassigned", groupId = "notifications-service-group",
                   containerFactory = "assignmentReassignedKafkaListenerContainerFactory")
    public void handleAssignmentReassigned(
            @Payload AssignmentReassignedEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            Acknowledgment acknowledgment) {
        
        log.info("Received AssignmentReassignedEvent from topic: {}, ticketId: {}, from: {}, to: {}", 
                topic, event.getTicketId(), event.getPreviousTechnicianId(), event.getNewTechnicianId());
        
        try {
            // Create notifications for reassignment
            notificationService.createTicketReassignmentNotifications(event);
            
            log.info("Successfully created reassignment notifications for ticket: {}", event.getTicketId());
            
            acknowledgment.acknowledge();
            
        } catch (Exception e) {
            log.error("Error processing assignment reassigned event for ticket {}: {}", 
                    event.getTicketId(), e.getMessage(), e);
            acknowledgment.acknowledge();
        }
    }
    
    /**
     * Handle assignment failed events from assignment-service
     * Creates notification for manager to handle manual assignment
     */
    @KafkaListener(topics = "assignment.failed", groupId = "notifications-service-group",
                   containerFactory = "assignmentFailedKafkaListenerContainerFactory")
    public void handleAssignmentFailed(
            @Payload AssignmentFailedEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            Acknowledgment acknowledgment) {
        
        log.info("Received AssignmentFailedEvent from topic: {}, ticketId: {}, reason: {}", 
                topic, event.getTicketId(), event.getFailureReason());
        
        try {
            // Create notification for manager about assignment failure
            notificationService.createAssignmentFailureNotification(event);
            
            log.info("Successfully created assignment failure notification for ticket: {}", event.getTicketId());
            
            acknowledgment.acknowledge();
            
        } catch (Exception e) {
            log.error("Error processing assignment failed event for ticket {}: {}", 
                    event.getTicketId(), e.getMessage(), e);
            acknowledgment.acknowledge();
        }
    }
    
    /**
     * Handle ticket updated events (if needed)
     */
    @KafkaListener(topics = "ticket.updated", groupId = "notifications-service-group")
    public void handleTicketUpdated(
            @Payload TicketUpdatedEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            Acknowledgment acknowledgment) {
        
        log.info("Received TicketUpdatedEvent from topic: {}, ticketId: {}", 
                topic, event.getTicketId());
        
        try {
            // Create notification for ticket update (if technician is assigned)
            if (event.getTechnicianId() != null) {
                notificationService.createTicketUpdateNotification(event);
            }
            
            acknowledgment.acknowledge();
            
        } catch (Exception e) {
            log.error("Error processing ticket updated event for ticket {}: {}", 
                    event.getTicketId(), e.getMessage(), e);
            acknowledgment.acknowledge();
        }
    }
    
    /**
     * Event for ticket updates
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class TicketUpdatedEvent {
        private java.util.UUID ticketId;
        private String titre;
        private String description;
        private String statut;
        private String priorite;
        private java.util.UUID technicianId;
        private java.util.UUID utilisateurId;
        private String updateReason;
        private java.time.LocalDateTime updatedAt;
    }
}
