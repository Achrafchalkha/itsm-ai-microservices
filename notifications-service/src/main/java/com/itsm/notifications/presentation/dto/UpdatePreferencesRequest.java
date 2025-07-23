package com.itsm.notifications.presentation.dto;

import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating notification preferences
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePreferencesRequest {
    
    private boolean emailEnabled;
    private boolean dashboardEnabled;
    
    @Email(message = "Email address must be valid")
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
}
