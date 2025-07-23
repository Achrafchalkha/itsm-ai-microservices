package com.itsm.notifications.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itsm.notifications.domain.model.*;
import com.itsm.notifications.infrastructure.kafka.event.AssignmentCreatedEvent;
import com.itsm.notifications.infrastructure.kafka.event.AssignmentFailedEvent;
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
    private final ObjectMapper objectMapper;
    
    /**
     * Create notification for ticket assignment
     */
    @Transactional
    public void createTicketAssignmentNotification(AssignmentCreatedEvent event) {
        log.info("Creating ticket assignment notification for technician: {}", event.getTechnicianId());
        
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
            
            log.info("Successfully created and sent ticket assignment notification: {}", entity.getId());
            
        } catch (Exception e) {
            log.error("Error creating ticket assignment notification: {}", e.getMessage(), e);
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
}
