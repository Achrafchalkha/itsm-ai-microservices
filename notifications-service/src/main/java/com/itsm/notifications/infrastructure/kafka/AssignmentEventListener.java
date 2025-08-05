package com.itsm.notifications.infrastructure.kafka;

import com.itsm.notifications.application.service.NotificationService;
import com.itsm.notifications.infrastructure.kafka.event.AssignmentCreatedEvent;
import com.itsm.notifications.infrastructure.kafka.event.AssignmentFailedEvent;
import com.itsm.notifications.infrastructure.kafka.event.TicketNoteAddedEvent;
import com.itsm.notifications.infrastructure.kafka.event.TicketStatusChangedEvent;
import com.itsm.notifications.infrastructure.kafka.event.TicketUpdatedEvent;
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
        
        log.info("🔔 ASSIGNMENT EVENT RECEIVED: topic={}, partition={}, offset={}, ticketId={}, technicianId={}, teamId={}, ticketOwner={}",
                topic, partition, offset, event.getTicketId(), event.getTechnicianId(), event.getTeamId(), event.getTicketUtilisateurId());
        log.info("🔔 EVENT DETAILS: ticketTitle='{}', technician='{} {}', priority={}, category={}",
                event.getTicketTitre(), event.getTechnicianPrenom(), event.getTechnicianNom(),
                event.getTicketPriorite(), event.getTicketCategorie());
        
        try {
            // Create notification for assigned technician ONLY
            log.info("🔔 STEP 1: CREATING TECHNICIAN ASSIGNMENT NOTIFICATION");
            log.info("   → Technician: {}, Ticket: {}, NOT for user: {}",
                    event.getTechnicianId(), event.getTicketId(), event.getTicketUtilisateurId());
            notificationService.createTicketAssignmentNotification(event);
            log.info("✅ STEP 1 COMPLETED: Technician notification created");

            // Create notification for team manager about the assignment ONLY
            log.info("🔔 STEP 2: CREATING MANAGER ASSIGNMENT NOTIFICATION");
            log.info("   → Team: {}, Ticket: {}, NOT for user: {}",
                    event.getTeamId(), event.getTicketId(), event.getTicketUtilisateurId());

            if (event.getTeamId() == null) {
                log.error("❌ STEP 2 FAILED: TeamId is NULL - cannot find manager");
            } else {
                log.info("   → Calling manager notification service...");
                notificationService.createManagerAssignmentNotification(event);
                log.info("✅ STEP 2 COMPLETED: Manager notification service called");
            }

            log.info("✅ ASSIGNMENT EVENT PROCESSING COMPLETED for technician {} and team {} (NOT for user {})",
                    event.getTechnicianId(), event.getTeamId(), event.getTicketUtilisateurId());

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
     * Handle ticket note added events - notify users when technician adds notes
     */
    @KafkaListener(topics = "ticket.note.added", groupId = "notifications-service-group",
                   containerFactory = "ticketNoteAddedKafkaListenerContainerFactory")
    public void handleTicketNoteAdded(
            @Payload TicketNoteAddedEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            Acknowledgment acknowledgment) {

        log.info("Received TicketNoteAddedEvent from topic: {}, ticketId: {}",
                topic, event.getTicketId());

        try {
            // Create notification for user when technician adds note
            notificationService.createTicketNoteAddedNotification(event);

            acknowledgment.acknowledge();

        } catch (Exception e) {
            log.error("Error processing ticket note added event for ticket {}: {}",
                    event.getTicketId(), e.getMessage(), e);
            acknowledgment.acknowledge();
        }
    }

    /**
     * Handle ticket status changed events - notify users when status changes
     */
    @KafkaListener(topics = "ticket.status.changed", groupId = "notifications-service-group",
                   containerFactory = "ticketStatusChangedKafkaListenerContainerFactory")
    public void handleTicketStatusChanged(
            @Payload TicketStatusChangedEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            Acknowledgment acknowledgment) {

        log.info("🔔 RECEIVED STATUS CHANGE EVENT: topic={}, ticketId={}, user={}, {} -> {}, technician={}",
                topic, event.getTicketId(), event.getUtilisateurId(), event.getOldStatus(), event.getNewStatus(), event.getTechnicienId());

        try {
            // Create notification for user when ticket status changes
            notificationService.createTicketStatusChangedNotification(event);

            acknowledgment.acknowledge();

        } catch (Exception e) {
            log.error("Error processing ticket status changed event for ticket {}: {}",
                    event.getTicketId(), e.getMessage(), e);
            acknowledgment.acknowledge();
        }
    }
}
