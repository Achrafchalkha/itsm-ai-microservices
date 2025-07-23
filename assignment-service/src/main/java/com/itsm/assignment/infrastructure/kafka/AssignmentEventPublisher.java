package com.itsm.assignment.infrastructure.kafka;

import com.itsm.assignment.infrastructure.kafka.event.AssignmentCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Service for publishing assignment events to Kafka
 * Notifies other services about assignment operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AssignmentEventPublisher {
    
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    /**
     * Publish assignment created event
     * Consumed by notifications-service and other interested services
     */
    public void publishAssignmentCreated(AssignmentCreatedEvent event) {
        try {
            log.info("Publishing AssignmentCreatedEvent for ticket {} assigned to technician {}", 
                    event.getTicketId(), event.getTechnicianId());
            
            CompletableFuture<SendResult<String, Object>> future = 
                    kafkaTemplate.send("assignment.created", event.getTicketId().toString(), event);
            
            // Wait for confirmation with timeout
            future.get(5, TimeUnit.SECONDS);
            
            log.info("Successfully published AssignmentCreatedEvent for ticket: {}", event.getTicketId());
            
        } catch (Exception e) {
            log.error("Failed to publish AssignmentCreatedEvent for ticket {}: {}", 
                    event.getTicketId(), e.getMessage(), e);
            // Don't throw exception - assignment should still succeed even if event publishing fails
        }
    }
    
    /**
     * Publish assignment reassigned event
     */
    public void publishAssignmentReassigned(AssignmentReassignedEvent event) {
        try {
            log.info("Publishing AssignmentReassignedEvent for ticket {} reassigned from {} to {}", 
                    event.getTicketId(), event.getPreviousTechnicianId(), event.getNewTechnicianId());
            
            CompletableFuture<SendResult<String, Object>> future = 
                    kafkaTemplate.send("assignment.reassigned", event.getTicketId().toString(), event);
            
            future.get(5, TimeUnit.SECONDS);
            
            log.info("Successfully published AssignmentReassignedEvent for ticket: {}", event.getTicketId());
            
        } catch (Exception e) {
            log.error("Failed to publish AssignmentReassignedEvent for ticket {}: {}", 
                    event.getTicketId(), e.getMessage(), e);
        }
    }
    
    /**
     * Publish assignment failed event
     */
    public void publishAssignmentFailed(AssignmentFailedEvent event) {
        try {
            log.info("Publishing AssignmentFailedEvent for ticket {}: {}", 
                    event.getTicketId(), event.getFailureReason());
            
            CompletableFuture<SendResult<String, Object>> future = 
                    kafkaTemplate.send("assignment.failed", event.getTicketId().toString(), event);
            
            future.get(5, TimeUnit.SECONDS);
            
            log.info("Successfully published AssignmentFailedEvent for ticket: {}", event.getTicketId());
            
        } catch (Exception e) {
            log.error("Failed to publish AssignmentFailedEvent for ticket {}: {}", 
                    event.getTicketId(), e.getMessage(), e);
        }
    }

    /**
     * Publish manager notification event
     * Notifies team managers when tickets are waiting for technicians
     */
    public void publishManagerNotification(ManagerNotificationEvent event) {
        try {
            log.info("Publishing ManagerNotificationEvent for manager {} about ticket {}",
                    event.getManagerId(), event.getTicketId());

            CompletableFuture<SendResult<String, Object>> future =
                    kafkaTemplate.send("manager.notification", event.getTicketId().toString(), event);

            // Wait for confirmation (with timeout)
            future.get(5, TimeUnit.SECONDS);

            log.info("Successfully published ManagerNotificationEvent for manager: {}", event.getManagerId());

        } catch (Exception e) {
            log.error("Failed to publish ManagerNotificationEvent for manager {}: {}",
                    event.getManagerId(), e.getMessage(), e);
        }
    }

    /**
     * Event for manager notification when tickets are waiting
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ManagerNotificationEvent {
        private java.util.UUID managerId;
        private java.util.UUID teamId;
        private java.util.UUID ticketId;
        private String ticketTitle;
        private String ticketCategory;
        private String ticketPriority;
        private String reason;
        private java.time.LocalDateTime notificationAt;

        // Additional context
        private String teamName;
        private String managerName;
        private String managerEmail;
    }

    /**
     * Event for assignment reassignment
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class AssignmentReassignedEvent {
        private java.util.UUID assignmentId;
        private java.util.UUID ticketId;
        private java.util.UUID previousTechnicianId;
        private java.util.UUID newTechnicianId;
        private java.util.UUID reassignedBy;
        private String reassignmentReason;
        private java.time.LocalDateTime reassignedAt;
        
        // Context information
        private String ticketTitre;
        private String previousTechnicianName;
        private String newTechnicianName;
        private String reassignedByName;
    }
    
    /**
     * Event for assignment failure
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class AssignmentFailedEvent {
        private java.util.UUID ticketId;
        private String failureReason;
        private java.time.LocalDateTime failedAt;
        private String ticketCategory;
        private String ticketPriority;
        
        // Context for fallback actions
        private java.util.UUID fallbackManagerId;
        private String fallbackManagerEmail;
        private java.util.UUID ticketCreatorId;
    }
}
