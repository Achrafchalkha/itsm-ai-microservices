package com.itsm.notifications.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itsm.notifications.domain.model.*;
import com.itsm.notifications.infrastructure.kafka.event.AssignmentCreatedEvent;
import com.itsm.notifications.infrastructure.kafka.event.AssignmentFailedEvent;
import com.itsm.notifications.infrastructure.kafka.event.TicketNoteAddedEvent;
import com.itsm.notifications.infrastructure.kafka.event.TicketStatusChangedEvent;
import com.itsm.notifications.infrastructure.client.UserServiceClient;
import com.itsm.notifications.infrastructure.kafka.event.AssignmentReassignedEvent;
import com.itsm.notifications.infrastructure.persistence.entity.NotificationEntity;
import com.itsm.notifications.infrastructure.persistence.repository.JpaNotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Main service for notification operations
 * Handles creation, delivery, and management of notifications
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
    
    private final JpaNotificationRepository notificationRepository;
    private final NotificationPreferencesService preferencesService;
    private final EmailNotificationService emailService;
    private final WebSocketNotificationService webSocketService;
    private final UserServiceClient userServiceClient;
    private final ObjectMapper objectMapper;
    
    /**
     * Create notification for ticket assignment (ONLY for technicians)
     */
    @Transactional
    public void createTicketAssignmentNotification(AssignmentCreatedEvent event) {
        log.info("🔔 TECHNICIAN NOTIFICATION: Creating for technician: {} on ticket: {} (NOT for user: {} or manager)",
                event.getTechnicianId(), event.getTicketId(), event.getTicketUtilisateurId());
        log.info("🔔 TECHNICIAN NOTIFICATION: This notification should ONLY go to technician {}", event.getTechnicianId());
        
        try {
            // Get user preferences
            NotificationPreferences preferences = preferencesService
                    .getPreferences(event.getTechnicianId());
            
            // Check if user wants this type of notification
            NotificationChannel channel = preferences.getPreferredChannel(NotificationType.TICKET_ASSIGNED);
            if (channel == null) {
                log.info("User {} doesn't want ticket assignment notifications", event.getTechnicianId());
                return;
            }
            
            // Create notification
            NotificationPriority priority = NotificationPriority.fromTicketPriority(event.getTicketPriorite());
            
            Notification notification = Notification.createTicketAssignmentNotification(
                    event.getTechnicianId(),
                    event.getTicketId(),
                    event.getTicketTitre(),
                    "System", // Could be improved to show actual assigner
                    priority
            );
            
            // Add contextual data
            Map<String, Object> data = new HashMap<>();
            data.put("assignmentId", event.getAssignmentId());
            data.put("ticketCategory", event.getTicketCategorie());
            data.put("assignmentStrategy", event.getAssignmentStrategy());
            data.put("confidenceScore", event.getConfidenceScore());
            notification.setData(data);
            
            // Set channel based on preferences
            notification.setChannel(channel);
            
            // Save notification
            NotificationEntity entity = convertToEntity(notification);
            entity = notificationRepository.save(entity);

            // Send via appropriate channels
            sendNotification(notification, preferences);

            log.info("✅ TECHNICIAN NOTIFICATION SAVED: id={}, userId={}, technician={}, ticket={}, message='{}'",
                    entity.getId(), entity.getUserId(), event.getTechnicianId(), event.getTicketId(), entity.getMessage());
            
        } catch (Exception e) {
            log.error("Error creating ticket assignment notification: {}", e.getMessage(), e);
        }
    }

    /**
     * Create notification for manager when ticket is assigned to their team member (ONLY for managers)
     */
    public void createManagerAssignmentNotification(AssignmentCreatedEvent event) {
        log.info("🔔 MANAGER NOTIFICATION START: Creating manager notification for team {} on ticket {} - NOT for ticket owner {}",
                event.getTeamId(), event.getTicketId(), event.getTicketUtilisateurId());

        try {
            // Get team manager ID from user service (we'll need to add this)
            log.info("🔍 MANAGER LOOKUP: Looking up manager for team: {}", event.getTeamId());
            UUID managerId = getTeamManagerId(event.getTeamId());
            if (managerId == null) {
                log.error("❌ MANAGER NOT FOUND: No manager found for team: {} - NOTIFICATION WILL NOT BE CREATED", event.getTeamId());
                return;
            }
            log.info("✅ MANAGER FOUND: Manager {} found for team {}", managerId, event.getTeamId());

            // Get manager preferences (same pattern as technician)
            log.info("🔧 PREFERENCES: Checking notification preferences for manager: {}", managerId);
            NotificationPreferences preferences = preferencesService.getPreferences(managerId);

            // Check if manager wants this type of notification (same pattern as technician)
            NotificationChannel channel = preferences.getPreferredChannel(NotificationType.TICKET_ASSIGNED);
            if (channel == null) {
                log.warn("❌ PREFERENCES DISABLED: Manager {} doesn't want ticket assignment notifications - SKIPPING", managerId);
                return;
            }
            log.info("✅ PREFERENCES OK: Manager {} accepts ticket assignment notifications via {}", managerId, channel);

            // Create notification (SAME PATTERN AS TECHNICIAN)
            NotificationPriority priority = NotificationPriority.fromTicketPriority(event.getTicketPriorite());

            // Get technician name from event
            String technicianName = "un technicien"; // Default fallback
            if (event.getTechnicianPrenom() != null && event.getTechnicianNom() != null) {
                technicianName = (event.getTechnicianPrenom() + " " + event.getTechnicianNom()).trim();
                if (technicianName.isEmpty()) {
                    technicianName = "un technicien";
                }
            }

            // Use the new dedicated method (same pattern as technician notification)
            Notification notification = Notification.createManagerAssignmentNotification(
                    managerId,
                    event.getTicketId(),
                    event.getTicketTitre(),
                    technicianName,
                    event.getTicketPriorite(),
                    event.getTicketCategorie(),
                    priority
            );

            // Add contextual data (SAME PATTERN AS TECHNICIAN)
            Map<String, Object> data = new HashMap<>();
            data.put("assignmentId", event.getAssignmentId());
            data.put("ticketCategory", event.getTicketCategorie());
            data.put("assignmentStrategy", event.getAssignmentStrategy());
            data.put("confidenceScore", event.getConfidenceScore());
            data.put("technicianId", event.getTechnicianId());
            data.put("teamId", event.getTeamId());
            data.put("notificationType", "MANAGER_TEAM_ASSIGNMENT");
            notification.setData(data);

            // Force BOTH channel (EXACTLY SAME AS TECHNICIAN)
            // This ensures the notification appears in dashboard regardless of preferences
            notification.setChannel(NotificationChannel.BOTH);

            // Save notification (SAME PATTERN AS TECHNICIAN)
            log.info("💾 SAVING: Saving manager notification to database...");
            NotificationEntity entity = convertToEntity(notification);
            entity = notificationRepository.save(entity);
            log.info("✅ SAVED: Manager notification saved with ID: {}", entity.getId());

            // Send via appropriate channels (SAME PATTERN AS TECHNICIAN)
            log.info("📤 SENDING: Sending manager notification via channels...");
            sendNotification(notification, preferences);

            log.info("🎉 MANAGER NOTIFICATION SAVED: id={}, userId={}, manager={}, team={}, ticket={}, message='{}'",
                    entity.getId(), entity.getUserId(), managerId, event.getTeamId(), event.getTicketId(), entity.getMessage());

        } catch (Exception e) {
            log.error("Error creating manager assignment notification for team {}: {}",
                    event.getTeamId(), e.getMessage(), e);
        }
    }

    /**
     * Get team manager ID from user service
     */
    private UUID getTeamManagerId(UUID teamId) {
        try {
            log.info("🔍 LOOKING FOR MANAGER: Requesting manager for team: {}", teamId);
            UUID managerId = userServiceClient.getTeamManagerId(teamId);
            if (managerId != null) {
                log.info("✅ MANAGER FOUND: Manager {} found for team {}", managerId, teamId);
            } else {
                log.warn("❌ NO MANAGER: No manager found for team {}", teamId);
            }
            return managerId;
        } catch (Exception e) {
            log.error("❌ MANAGER ERROR: Error getting manager for team {}: {}", teamId, e.getMessage(), e);
            return null;
        }
    }

    /**
     * Create notifications for ticket reassignment
     */
    @Transactional
    public void createTicketReassignmentNotifications(AssignmentReassignedEvent event) {
        log.info("Creating ticket reassignment notifications for ticket: {}", event.getTicketId());
        
        try {
            // Notification for new technician
            createReassignmentNotificationForNewTechnician(event);
            
            // Notification for previous technician (informational)
            createReassignmentNotificationForPreviousTechnician(event);
            
        } catch (Exception e) {
            log.error("Error creating ticket reassignment notifications: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Create notification for assignment failure
     */
    @Transactional
    public void createAssignmentFailureNotification(AssignmentFailedEvent event) {
        log.info("Creating assignment failure notification for ticket: {}", event.getTicketId());
        
        try {
            UUID managerId = event.getFallbackManagerId();
            if (managerId == null) {
                log.warn("No manager ID provided for assignment failure notification");
                return;
            }
            
            // Get manager preferences
            NotificationPreferences preferences = preferencesService.getPreferences(managerId);
            NotificationChannel channel = preferences.getPreferredChannel(NotificationType.ASSIGNMENT_FAILED);
            
            if (channel == null) {
                log.info("Manager {} doesn't want assignment failure notifications", managerId);
                return;
            }
            
            // Create notification
            Notification notification = Notification.createAssignmentFailureNotification(
                    managerId,
                    event.getTicketId(),
                    "Ticket #" + event.getTicketId().toString().substring(0, 8), // Simplified title
                    event.getFailureReason()
            );
            
            // Add contextual data
            Map<String, Object> data = new HashMap<>();
            data.put("ticketCategory", event.getTicketCategory());
            data.put("ticketPriority", event.getTicketPriority());
            data.put("failedAt", event.getFailedAt());
            notification.setData(data);
            
            notification.setChannel(channel);
            
            // Save notification
            NotificationEntity entity = convertToEntity(notification);
            entity = notificationRepository.save(entity);
            
            // Send notification
            sendNotification(notification, preferences);
            
            log.info("Successfully created assignment failure notification: {}", entity.getId());
            
        } catch (Exception e) {
            log.error("Error creating assignment failure notification: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Create notification for ticket update
     */
    @Transactional
    public void createTicketUpdateNotification(Object ticketUpdateEvent) {
        // Implementation for ticket update notifications
        log.info("Creating ticket update notification");
        // TODO: Implement based on ticket update event structure
    }
    
    /**
     * Get notifications for a user
     */
    @Transactional(readOnly = true)
    public List<Notification> getUserNotifications(UUID userId, boolean unreadOnly, int limit) {
        log.debug("Getting notifications for user: {}, unreadOnly: {}", userId, unreadOnly);
        
        List<NotificationEntity> entities;
        if (unreadOnly) {
            entities = notificationRepository.findByUserIdAndReadStatusOrderByCreatedAtDesc(userId, false);
        } else {
            entities = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
        }
        
        return entities.stream()
                .limit(limit)
                .map(this::convertFromEntity)
                .toList();
    }
    
    /**
     * Mark notification as read
     */
    @Transactional
    public void markAsRead(UUID notificationId, UUID userId) {
        log.debug("Marking notification {} as read for user: {}", notificationId, userId);
        
        NotificationEntity entity = notificationRepository.findByIdAndUserId(notificationId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found: " + notificationId));
        
        entity.markAsRead();
        notificationRepository.save(entity);
        
        log.info("Marked notification {} as read", notificationId);
    }
    
    /**
     * Mark all notifications as read for a user
     */
    @Transactional
    public void markAllAsRead(UUID userId) {
        log.debug("Marking all notifications as read for user: {}", userId);
        
        List<NotificationEntity> unreadNotifications = notificationRepository
                .findByUserIdAndReadStatusOrderByCreatedAtDesc(userId, false);
        
        for (NotificationEntity entity : unreadNotifications) {
            entity.markAsRead();
        }
        
        notificationRepository.saveAll(unreadNotifications);
        
        log.info("Marked {} notifications as read for user: {}", unreadNotifications.size(), userId);
    }
    
    /**
     * Send notification via appropriate channels
     */
    private void sendNotification(Notification notification, NotificationPreferences preferences) {
        try {
            // Send via dashboard (WebSocket)
            if (notification.getChannel().includesDashboard()) {
                webSocketService.sendNotificationToUser(notification);
            }
            
            // Send via email
            if (notification.getChannel().includesEmail() && preferences.getEmailAddress() != null) {
                emailService.sendNotificationEmail(notification, preferences.getEmailAddress());
            }
            
        } catch (Exception e) {
            log.error("Error sending notification {}: {}", notification.getId(), e.getMessage(), e);
        }
    }
    
    /**
     * Create reassignment notification for new technician
     */
    private void createReassignmentNotificationForNewTechnician(AssignmentReassignedEvent event) {
        NotificationPreferences preferences = preferencesService.getPreferences(event.getNewTechnicianId());
        NotificationChannel channel = preferences.getPreferredChannel(NotificationType.TICKET_REASSIGNED);
        
        if (channel == null) return;
        
        Notification notification = Notification.createTicketReassignmentNotification(
                event.getNewTechnicianId(),
                event.getTicketId(),
                event.getTicketTitre(),
                event.getPreviousTechnicianName(),
                event.getReassignedByName(),
                NotificationPriority.NORMAL
        );
        
        notification.setChannel(channel);
        
        NotificationEntity entity = convertToEntity(notification);
        notificationRepository.save(entity);
        
        sendNotification(notification, preferences);
    }
    
    /**
     * Create reassignment notification for previous technician
     */
    private void createReassignmentNotificationForPreviousTechnician(AssignmentReassignedEvent event) {
        NotificationPreferences preferences = preferencesService.getPreferences(event.getPreviousTechnicianId());
        NotificationChannel channel = preferences.getPreferredChannel(NotificationType.TICKET_REASSIGNED);
        
        if (channel == null) return;
        
        String title = String.format("Ticket #%s reassigned", event.getTicketId().toString().substring(0, 8));
        String message = String.format("Ticket '%s' has been reassigned from you to %s by %s", 
                event.getTicketTitre(), event.getNewTechnicianName(), event.getReassignedByName());
        
        Notification notification = Notification.createNotification(
                event.getPreviousTechnicianId(),
                NotificationType.TICKET_REASSIGNED,
                title,
                message,
                NotificationPriority.LOW,
                channel
        );
        
        notification.setTicketId(event.getTicketId());
        
        NotificationEntity entity = convertToEntity(notification);
        notificationRepository.save(entity);
        
        sendNotification(notification, preferences);
    }
    
    /**
     * Convert domain Notification to JPA entity
     */
    private NotificationEntity convertToEntity(Notification notification) {
        String dataJson = null;
        if (notification.getData() != null) {
            try {
                dataJson = objectMapper.writeValueAsString(notification.getData());
            } catch (Exception e) {
                log.warn("Error serializing notification data: {}", e.getMessage());
            }
        }
        
        return NotificationEntity.builder()
                .id(notification.getId())
                .userId(notification.getUserId())
                .type(notification.getType())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .dataJson(dataJson)
                .readStatus(notification.isReadStatus())
                .priority(notification.getPriority())
                .channel(notification.getChannel())
                .createdAt(notification.getCreatedAt())
                .readAt(notification.getReadAt())
                .expiresAt(notification.getExpiresAt())
                .ticketId(notification.getTicketId())
                .assignmentId(notification.getAssignmentId())
                .relatedUserId(notification.getRelatedUserId())
                .build();
    }
    
    /**
     * Convert JPA entity to domain Notification
     */
    private Notification convertFromEntity(NotificationEntity entity) {
        Map<String, Object> data = null;
        if (entity.getDataJson() != null) {
            try {
                data = objectMapper.readValue(entity.getDataJson(), Map.class);
            } catch (Exception e) {
                log.warn("Error deserializing notification data: {}", e.getMessage());
            }
        }
        
        return Notification.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .type(entity.getType())
                .title(entity.getTitle())
                .message(entity.getMessage())
                .data(data)
                .readStatus(entity.getReadStatus())
                .priority(entity.getPriority())
                .channel(entity.getChannel())
                .createdAt(entity.getCreatedAt())
                .readAt(entity.getReadAt())
                .expiresAt(entity.getExpiresAt())
                .ticketId(entity.getTicketId())
                .assignmentId(entity.getAssignmentId())
                .relatedUserId(entity.getRelatedUserId())
                .build();
    }

    /**
     * Create notification when technician adds note to ticket (ONLY for ticket owner/user)
     */
    public void createTicketNoteAddedNotification(TicketNoteAddedEvent event) {
        log.info("Creating USER note notification for ticket owner {} on ticket {} - technician {} added note",
                event.getUtilisateurId(), event.getTicketId(), event.getTechnicienId());

        try {
            // Get user preferences
            NotificationPreferences preferences = preferencesService.getPreferences(event.getUtilisateurId());

            // Check if user wants this type of notification
            NotificationChannel channel = preferences.getPreferredChannel(NotificationType.TICKET_UPDATED);
            if (channel == null) {
                log.info("User {} doesn't want ticket note notifications", event.getUtilisateurId());
                return;
            }

            // Create notification
            String technicianName = (event.getTechnicienPrenom() + " " + event.getTechnicienNom()).trim();
            if (technicianName.isEmpty()) {
                technicianName = "Technicien";
            }

            Notification notification = Notification.createNotification(
                    event.getUtilisateurId(),
                    NotificationType.TICKET_UPDATED,
                    "Note ajoutée à votre ticket",
                    String.format("Le technicien %s a ajouté une note à votre ticket \"%s\"",
                            technicianName, event.getTicketTitre()),
                    NotificationPriority.NORMAL,
                    channel
            );

            // Add contextual data
            Map<String, Object> data = new HashMap<>();
            data.put("ticketId", event.getTicketId());
            data.put("technicienId", event.getTechnicienId());
            data.put("technicienName", technicianName);
            data.put("note", event.getNote());
            data.put("noteType", "WORK_NOTE");
            notification.setData(data);
            notification.setTicketId(event.getTicketId());
            notification.setRelatedUserId(event.getTechnicienId());

            // Save notification
            NotificationEntity entity = convertToEntity(notification);
            entity = notificationRepository.save(entity);

            // Send via appropriate channels
            sendNotification(notification, preferences);

            log.info("✅ USER NOTE notification created: id={}, user={}, ticket={}, technician={}",
                    entity.getId(), event.getUtilisateurId(), event.getTicketId(), event.getTechnicienId());

        } catch (Exception e) {
            log.error("Error creating ticket note added notification for user {}: {}",
                    event.getUtilisateurId(), e.getMessage(), e);
        }
    }

    /**
     * Create notification when ticket status changes (ONLY for ticket owner/user)
     */
    public void createTicketStatusChangedNotification(TicketStatusChangedEvent event) {
        log.info("🔔 CREATING USER STATUS NOTIFICATION: user={}, ticket={}, {} -> {}, technician={}",
                event.getUtilisateurId(), event.getTicketId(), event.getOldStatus(), event.getNewStatus(), event.getTechnicienId());

        try {
            // Get user preferences
            NotificationPreferences preferences = preferencesService.getPreferences(event.getUtilisateurId());

            // Check if user wants this type of notification
            NotificationChannel channel = preferences.getPreferredChannel(NotificationType.TICKET_UPDATED);
            if (channel == null) {
                log.info("User {} doesn't want ticket status change notifications", event.getUtilisateurId());
                return;
            }

            // Create notification
            String technicianName = (event.getTechnicienPrenom() + " " + event.getTechnicienNom()).trim();
            if (technicianName.isEmpty()) {
                technicianName = "Technicien";
            }

            String statusMessage = getStatusChangeMessage(event.getOldStatus(), event.getNewStatus());

            Notification notification = Notification.createNotification(
                    event.getUtilisateurId(),
                    NotificationType.TICKET_UPDATED,
                    "Statut de votre ticket modifié",
                    String.format("Le statut de votre ticket \"%s\" a été modifié par %s : %s",
                            event.getTicketTitre(), technicianName, statusMessage),
                    getStatusChangePriority(event.getNewStatus()),
                    channel
            );

            // Add contextual data
            Map<String, Object> data = new HashMap<>();
            data.put("ticketId", event.getTicketId());
            data.put("technicienId", event.getTechnicienId());
            data.put("technicienName", technicianName);
            data.put("oldStatus", event.getOldStatus());
            data.put("newStatus", event.getNewStatus());
            data.put("changeReason", event.getChangeReason());
            notification.setData(data);
            notification.setTicketId(event.getTicketId());
            notification.setRelatedUserId(event.getTechnicienId());

            // Save notification
            NotificationEntity entity = convertToEntity(notification);
            entity = notificationRepository.save(entity);

            // Send via appropriate channels
            sendNotification(notification, preferences);

            log.info("✅ USER STATUS notification created: id={}, user={}, ticket={}, {} -> {}, technician={}",
                    entity.getId(), event.getUtilisateurId(), event.getTicketId(),
                    event.getOldStatus(), event.getNewStatus(), event.getTechnicienId());

        } catch (Exception e) {
            log.error("Error creating ticket status changed notification for user {}: {}",
                    event.getUtilisateurId(), e.getMessage(), e);
        }
    }

    /**
     * Get user-friendly status change message
     */
    private String getStatusChangeMessage(String oldStatus, String newStatus) {
        return switch (newStatus) {
            case "OUVERT" -> "Le technicien a commencé le travail";
            case "EN_COURS" -> "Le ticket est en cours de traitement";
            case "RESOLU" -> "Le ticket a été résolu";
            case "FERME" -> "Le ticket a été fermé";
            default -> String.format("%s → %s", oldStatus, newStatus);
        };
    }

    /**
     * Get notification priority based on status change
     */
    private NotificationPriority getStatusChangePriority(String newStatus) {
        return switch (newStatus) {
            case "RESOLU", "FERME" -> NotificationPriority.HIGH;
            case "OUVERT" -> NotificationPriority.NORMAL;
            default -> NotificationPriority.LOW;
        };
    }
}
