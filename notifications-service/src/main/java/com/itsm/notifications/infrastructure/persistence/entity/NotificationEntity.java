package com.itsm.notifications.infrastructure.persistence.entity;

import com.itsm.notifications.domain.model.NotificationChannel;
import com.itsm.notifications.domain.model.NotificationPriority;
import com.itsm.notifications.domain.model.NotificationType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA Entity for Notification
 * Maps to notifications table in notifications_db database
 */
@Entity
@Table(name = "notifications")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEntity {
    
    @Id
    @Column(name = "id", columnDefinition = "UUID")
    private UUID id;
    
    @Column(name = "user_id", nullable = false, columnDefinition = "UUID")
    private UUID userId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 50)
    private NotificationType type;
    
    @Column(name = "title", nullable = false, length = 255)
    private String title;
    
    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;
    
    @Column(name = "data_json", columnDefinition = "TEXT")
    private String dataJson;
    
    @Column(name = "read_status", nullable = false)
    @Builder.Default
    private Boolean readStatus = false;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "priority", length = 20)
    @Builder.Default
    private NotificationPriority priority = NotificationPriority.NORMAL;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "channel", length = 20)
    @Builder.Default
    private NotificationChannel channel = NotificationChannel.DASHBOARD;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "read_at")
    private LocalDateTime readAt;
    
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;
    
    // Context information for quick access
    @Column(name = "ticket_id", columnDefinition = "UUID")
    private UUID ticketId;
    
    @Column(name = "assignment_id", columnDefinition = "UUID")
    private UUID assignmentId;
    
    @Column(name = "related_user_id", columnDefinition = "UUID")
    private UUID relatedUserId;
    
    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (readStatus == null) {
            readStatus = false;
        }
        if (priority == null) {
            priority = NotificationPriority.NORMAL;
        }
        if (channel == null) {
            channel = NotificationChannel.DASHBOARD;
        }
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
}
