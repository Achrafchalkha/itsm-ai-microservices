package com.itsm.notifications.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain model for user notification preferences
 * Defines how and when a user wants to receive notifications
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationPreferences {
    
    private UUID userId;
    private boolean emailEnabled;
    private boolean dashboardEnabled;
    private String emailAddress;
    
    // Email preferences by notification type
    private boolean ticketAssignedEmail;
    private boolean ticketReassignedEmail;
    private boolean ticketUpdatedEmail;
    private boolean assignmentFailedEmail;
    private boolean slaWarningEmail;
    private boolean teamMemberAddedEmail;
    
    // Dashboard preferences by notification type
    private boolean ticketAssignedDashboard;
    private boolean ticketReassignedDashboard;
    private boolean ticketUpdatedDashboard;
    private boolean assignmentFailedDashboard;
    private boolean slaWarningDashboard;
    private boolean teamMemberAddedDashboard;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    /**
     * Factory method to create default preferences for a new user
     */
    public static NotificationPreferences createDefaultPreferences(UUID userId, String emailAddress) {
        return NotificationPreferences.builder()
                .userId(userId)
                .emailEnabled(true)
                .dashboardEnabled(true)
                .emailAddress(emailAddress)
                
                // Default email preferences (important notifications only)
                .ticketAssignedEmail(true)
                .ticketReassignedEmail(true)
                .ticketUpdatedEmail(false)
                .assignmentFailedEmail(true)
                .slaWarningEmail(true)
                .teamMemberAddedEmail(false)
                
                // Default dashboard preferences (all notifications)
                .ticketAssignedDashboard(true)
                .ticketReassignedDashboard(true)
                .ticketUpdatedDashboard(true)
                .assignmentFailedDashboard(true)
                .slaWarningDashboard(true)
                .teamMemberAddedDashboard(true)
                
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
    
    /**
     * Check if user wants email notifications for a specific type
     */
    public boolean wantsEmailFor(NotificationType type) {
        if (!emailEnabled) return false;
        
        return switch (type) {
            case TICKET_ASSIGNED -> ticketAssignedEmail;
            case TICKET_REASSIGNED -> ticketReassignedEmail;
            case TICKET_UPDATED -> ticketUpdatedEmail;
            case ASSIGNMENT_FAILED -> assignmentFailedEmail;
            case SLA_WARNING -> slaWarningEmail;
            case TEAM_MEMBER_ADDED -> teamMemberAddedEmail;
            case SYSTEM_ALERT -> true; // Always send system alerts via email
            case CUSTOM -> false; // Custom notifications don't use email by default
        };
    }
    
    /**
     * Check if user wants dashboard notifications for a specific type
     */
    public boolean wantsDashboardFor(NotificationType type) {
        if (!dashboardEnabled) return false;
        
        return switch (type) {
            case TICKET_ASSIGNED -> ticketAssignedDashboard;
            case TICKET_REASSIGNED -> ticketReassignedDashboard;
            case TICKET_UPDATED -> ticketUpdatedDashboard;
            case ASSIGNMENT_FAILED -> assignmentFailedDashboard;
            case SLA_WARNING -> slaWarningDashboard;
            case TEAM_MEMBER_ADDED -> teamMemberAddedDashboard;
            case SYSTEM_ALERT -> true; // Always show system alerts on dashboard
            case CUSTOM -> true; // Show custom notifications on dashboard
        };
    }
    
    /**
     * Get the appropriate notification channel based on preferences
     */
    public NotificationChannel getPreferredChannel(NotificationType type) {
        boolean wantsEmail = wantsEmailFor(type);
        boolean wantsDashboard = wantsDashboardFor(type);
        
        if (wantsEmail && wantsDashboard) {
            return NotificationChannel.BOTH;
        } else if (wantsEmail) {
            return NotificationChannel.EMAIL;
        } else if (wantsDashboard) {
            return NotificationChannel.DASHBOARD;
        } else {
            return null; // User doesn't want this notification
        }
    }
    
    /**
     * Update email preferences for a notification type
     */
    public void setEmailPreference(NotificationType type, boolean enabled) {
        switch (type) {
            case TICKET_ASSIGNED -> this.ticketAssignedEmail = enabled;
            case TICKET_REASSIGNED -> this.ticketReassignedEmail = enabled;
            case TICKET_UPDATED -> this.ticketUpdatedEmail = enabled;
            case ASSIGNMENT_FAILED -> this.assignmentFailedEmail = enabled;
            case SLA_WARNING -> this.slaWarningEmail = enabled;
            case TEAM_MEMBER_ADDED -> this.teamMemberAddedEmail = enabled;
        }
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Update dashboard preferences for a notification type
     */
    public void setDashboardPreference(NotificationType type, boolean enabled) {
        switch (type) {
            case TICKET_ASSIGNED -> this.ticketAssignedDashboard = enabled;
            case TICKET_REASSIGNED -> this.ticketReassignedDashboard = enabled;
            case TICKET_UPDATED -> this.ticketUpdatedDashboard = enabled;
            case ASSIGNMENT_FAILED -> this.assignmentFailedDashboard = enabled;
            case SLA_WARNING -> this.slaWarningDashboard = enabled;
            case TEAM_MEMBER_ADDED -> this.teamMemberAddedDashboard = enabled;
        }
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Update email address
     */
    public void updateEmailAddress(String newEmailAddress) {
        this.emailAddress = newEmailAddress;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Enable/disable all email notifications
     */
    public void setEmailEnabled(boolean enabled) {
        this.emailEnabled = enabled;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Enable/disable all dashboard notifications
     */
    public void setDashboardEnabled(boolean enabled) {
        this.dashboardEnabled = enabled;
        this.updatedAt = LocalDateTime.now();
    }
}
