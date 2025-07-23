package com.itsm.notifications.presentation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for NotificationPreferences responses
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationPreferencesDTO {
    
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
}
