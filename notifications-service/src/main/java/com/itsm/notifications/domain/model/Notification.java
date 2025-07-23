package com.itsm.notifications.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Domain model for Notification
 * Represents a notification sent to a user
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    
    private UUID id;
    private UUID userId;
    private NotificationType type;
    private String title;
    private String message;
    private Map<String, Object> data; // Additional contextual data
    private boolean readStatus;
    private NotificationPriority priority;
    private NotificationChannel channel;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;
    private LocalDateTime expiresAt;
    
    // Context information for quick access
    private UUID ticketId;
    private UUID assignmentId;
    private UUID relatedUserId;
    
    /**
     * Factory method to create a new notification
     */
    public static Notification createNotification(UUID userId, NotificationType type, 
                                                String title, String message,
                                                NotificationPriority priority,
                                                NotificationChannel channel) {
        return Notification.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .type(type)
                .title(title)
                .message(message)
                .priority(priority != null ? priority : NotificationPriority.NORMAL)
                .channel(channel != null ? channel : NotificationChannel.DASHBOARD)
                .readStatus(false)
                .createdAt(LocalDateTime.now())
                .build();
    }
    
    /**
     * Factory method for ticket assignment notification
     */
    public static Notification createTicketAssignmentNotification(UUID technicianId, 
                                                                UUID ticketId,
                                                                String ticketTitle,
                                                                String assignedBy,
                                                                NotificationPriority priority) {
        String title = String.format("Ticket #%s assigned to you", ticketId.toString().substring(0, 8));
        String message = String.format("You have been assigned to ticket: %s by %s", ticketTitle, assignedBy);
        
        return Notification.builder()
                .id(UUID.randomUUID())
                .userId(technicianId)
                .type(NotificationType.TICKET_ASSIGNED)
                .title(title)
                .message(message)
                .priority(priority)
                .channel(NotificationChannel.BOTH)
                .readStatus(false)
                .ticketId(ticketId)
                .createdAt(LocalDateTime.now())
                .build();
    }
    
    /**
     * Factory method for ticket reassignment notification
     */
    public static Notification createTicketReassignmentNotification(UUID newTechnicianId,
                                                                  UUID ticketId,
                                                                  String ticketTitle,
                                                                  String previousTechnician,
                                                                  String reassignedBy,
                                                                  NotificationPriority priority) {
        String title = String.format("Ticket #%s reassigned to you", ticketId.toString().substring(0, 8));
        String message = String.format("Ticket '%s' has been reassigned to you from %s by %s", 
                ticketTitle, previousTechnician, reassignedBy);
        
        return Notification.builder()
                .id(UUID.randomUUID())
                .userId(newTechnicianId)
                .type(NotificationType.TICKET_REASSIGNED)
                .title(title)
                .message(message)
                .priority(priority)
                .channel(NotificationChannel.BOTH)
                .readStatus(false)
                .ticketId(ticketId)
                .createdAt(LocalDateTime.now())
                .build();
    }
    
    /**
     * Factory method for assignment failure notification (to manager)
     */
    public static Notification createAssignmentFailureNotification(UUID managerId,
                                                                 UUID ticketId,
                                                                 String ticketTitle,
                                                                 String failureReason) {
        String title = String.format("Assignment failed for ticket #%s", ticketId.toString().substring(0, 8));
        String message = String.format("Automatic assignment failed for ticket '%s'. Reason: %s. Manual intervention required.", 
                ticketTitle, failureReason);
        
        return Notification.builder()
                .id(UUID.randomUUID())
                .userId(managerId)
                .type(NotificationType.ASSIGNMENT_FAILED)
                .title(title)
                .message(message)
                .priority(NotificationPriority.HIGH)
                .channel(NotificationChannel.BOTH)
                .readStatus(false)
                .ticketId(ticketId)
                .createdAt(LocalDateTime.now())
                .build();
    }
    
    /**
     * Mark notification as read
     */
    public void markAsRead() {
        this.readStatus = true;
        this.readAt = LocalDateTime.now();
    }
    
    /**
     * Check if notification is expired
     */
    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }
    
    /**
     * Check if notification requires immediate attention
     */
    public boolean requiresImmediateAttention() {
        return priority != null && priority.requiresImmediateNotification();
    }
    
    /**
     * Set expiration time (e.g., for temporary notifications)
     */
    public void setExpirationTime(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }
    
    /**
     * Add contextual data
     */
    public void addData(String key, Object value) {
        if (this.data == null) {
            this.data = new java.util.HashMap<>();
        }
        this.data.put(key, value);
    }
}
