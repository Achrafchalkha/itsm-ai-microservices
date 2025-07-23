package com.itsm.notifications.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA Entity for NotificationPreferences
 * Maps to notification_preferences table in notifications_db database
 */
@Entity
@Table(name = "notification_preferences")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationPreferencesEntity {
    
    @Id
    @Column(name = "user_id", columnDefinition = "UUID")
    private UUID userId;
    
    @Column(name = "email_enabled", nullable = false)
    @Builder.Default
    private Boolean emailEnabled = true;
    
    @Column(name = "dashboard_enabled", nullable = false)
    @Builder.Default
    private Boolean dashboardEnabled = true;
    
    @Column(name = "email_address", length = 255)
    private String emailAddress;
    
    // Email preferences by notification type
    @Column(name = "ticket_assigned_email", nullable = false)
    @Builder.Default
    private Boolean ticketAssignedEmail = true;
    
    @Column(name = "ticket_reassigned_email", nullable = false)
    @Builder.Default
    private Boolean ticketReassignedEmail = true;
    
    @Column(name = "ticket_updated_email", nullable = false)
    @Builder.Default
    private Boolean ticketUpdatedEmail = false;
    
    @Column(name = "assignment_failed_email", nullable = false)
    @Builder.Default
    private Boolean assignmentFailedEmail = true;
    
    @Column(name = "sla_warning_email", nullable = false)
    @Builder.Default
    private Boolean slaWarningEmail = true;
    
    @Column(name = "team_member_added_email", nullable = false)
    @Builder.Default
    private Boolean teamMemberAddedEmail = false;
    
    // Dashboard preferences by notification type
    @Column(name = "ticket_assigned_dashboard", nullable = false)
    @Builder.Default
    private Boolean ticketAssignedDashboard = true;
    
    @Column(name = "ticket_reassigned_dashboard", nullable = false)
    @Builder.Default
    private Boolean ticketReassignedDashboard = true;
    
    @Column(name = "ticket_updated_dashboard", nullable = false)
    @Builder.Default
    private Boolean ticketUpdatedDashboard = true;
    
    @Column(name = "assignment_failed_dashboard", nullable = false)
    @Builder.Default
    private Boolean assignmentFailedDashboard = true;
    
    @Column(name = "sla_warning_dashboard", nullable = false)
    @Builder.Default
    private Boolean slaWarningDashboard = true;
    
    @Column(name = "team_member_added_dashboard", nullable = false)
    @Builder.Default
    private Boolean teamMemberAddedDashboard = true;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
