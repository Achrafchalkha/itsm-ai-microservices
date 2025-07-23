package com.itsm.notifications.presentation.controller;

import com.itsm.notifications.application.service.NotificationPreferencesService;
import com.itsm.notifications.domain.model.NotificationPreferences;
import com.itsm.notifications.domain.model.NotificationType;
import com.itsm.notifications.presentation.dto.NotificationPreferencesDTO;
import com.itsm.notifications.presentation.dto.UpdatePreferencesRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.UUID;

/**
 * REST Controller for notification preferences
 * Allows users to manage their notification settings
 */
@RestController
@RequestMapping("/api/notifications/preferences")
@RequiredArgsConstructor
@Slf4j
public class NotificationPreferencesController {
    
    private final NotificationPreferencesService preferencesService;
    
    /**
     * Get notification preferences for the current user
     */
    @GetMapping
    @PreAuthorize("hasRole('UTILISATEUR') or hasRole('TECHNICIEN') or hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<NotificationPreferencesDTO> getPreferences(Authentication authentication) {
        UUID userId = extractUserIdFromAuth(authentication);
        log.debug("Getting notification preferences for user: {}", userId);
        
        try {
            NotificationPreferences preferences = preferencesService.getPreferences(userId);
            NotificationPreferencesDTO response = convertToDTO(preferences);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error getting preferences for user {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Update notification preferences for the current user
     */
    @PutMapping
    @PreAuthorize("hasRole('UTILISATEUR') or hasRole('TECHNICIEN') or hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<NotificationPreferencesDTO> updatePreferences(
            @Valid @RequestBody UpdatePreferencesRequest request,
            Authentication authentication) {
        
        UUID userId = extractUserIdFromAuth(authentication);
        log.info("Updating notification preferences for user: {}", userId);
        
        try {
            NotificationPreferences preferences = convertFromRequest(request);
            preferences = preferencesService.updatePreferences(userId, preferences);
            
            NotificationPreferencesDTO response = convertToDTO(preferences);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error updating preferences for user {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Update email address for the current user
     */
    @PutMapping("/email")
    @PreAuthorize("hasRole('UTILISATEUR') or hasRole('TECHNICIEN') or hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<Void> updateEmailAddress(
            @RequestBody EmailUpdateRequest request,
            Authentication authentication) {
        
        UUID userId = extractUserIdFromAuth(authentication);
        log.info("Updating email address for user: {}", userId);
        
        try {
            preferencesService.updateEmailAddress(userId, request.getEmailAddress());
            return ResponseEntity.ok().build();
            
        } catch (Exception e) {
            log.error("Error updating email address for user {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Enable/disable email notifications
     */
    @PutMapping("/email/enabled")
    @PreAuthorize("hasRole('UTILISATEUR') or hasRole('TECHNICIEN') or hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<Void> setEmailEnabled(
            @RequestBody EnabledRequest request,
            Authentication authentication) {
        
        UUID userId = extractUserIdFromAuth(authentication);
        log.info("Setting email notifications {} for user: {}", request.isEnabled() ? "enabled" : "disabled", userId);
        
        try {
            preferencesService.setEmailEnabled(userId, request.isEnabled());
            return ResponseEntity.ok().build();
            
        } catch (Exception e) {
            log.error("Error setting email enabled for user {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Enable/disable dashboard notifications
     */
    @PutMapping("/dashboard/enabled")
    @PreAuthorize("hasRole('UTILISATEUR') or hasRole('TECHNICIEN') or hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<Void> setDashboardEnabled(
            @RequestBody EnabledRequest request,
            Authentication authentication) {
        
        UUID userId = extractUserIdFromAuth(authentication);
        log.info("Setting dashboard notifications {} for user: {}", request.isEnabled() ? "enabled" : "disabled", userId);
        
        try {
            preferencesService.setDashboardEnabled(userId, request.isEnabled());
            return ResponseEntity.ok().build();
            
        } catch (Exception e) {
            log.error("Error setting dashboard enabled for user {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Update preferences for a specific notification type
     */
    @PutMapping("/type/{type}")
    @PreAuthorize("hasRole('UTILISATEUR') or hasRole('TECHNICIEN') or hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<Void> updateTypePreferences(
            @PathVariable String type,
            @RequestBody TypePreferencesRequest request,
            Authentication authentication) {
        
        UUID userId = extractUserIdFromAuth(authentication);
        log.info("Updating {} preferences for user: {}", type, userId);
        
        try {
            NotificationType notificationType = NotificationType.valueOf(type.toUpperCase());
            preferencesService.updateTypePreference(
                    userId, 
                    notificationType, 
                    request.isEmailEnabled(), 
                    request.isDashboardEnabled()
            );
            
            return ResponseEntity.ok().build();
            
        } catch (IllegalArgumentException e) {
            log.warn("Invalid notification type: {}", type);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error updating type preferences for user {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Extract user ID from authentication
     */
    private UUID extractUserIdFromAuth(Authentication authentication) {
        // For now, return a dummy UUID
        // In real implementation, extract from JWT token
        return UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    }
    
    /**
     * Convert domain model to DTO
     */
    private NotificationPreferencesDTO convertToDTO(NotificationPreferences preferences) {
        return NotificationPreferencesDTO.builder()
                .userId(preferences.getUserId())
                .emailEnabled(preferences.isEmailEnabled())
                .dashboardEnabled(preferences.isDashboardEnabled())
                .emailAddress(preferences.getEmailAddress())
                .ticketAssignedEmail(preferences.isTicketAssignedEmail())
                .ticketReassignedEmail(preferences.isTicketReassignedEmail())
                .ticketUpdatedEmail(preferences.isTicketUpdatedEmail())
                .assignmentFailedEmail(preferences.isAssignmentFailedEmail())
                .slaWarningEmail(preferences.isSlaWarningEmail())
                .teamMemberAddedEmail(preferences.isTeamMemberAddedEmail())
                .ticketAssignedDashboard(preferences.isTicketAssignedDashboard())
                .ticketReassignedDashboard(preferences.isTicketReassignedDashboard())
                .ticketUpdatedDashboard(preferences.isTicketUpdatedDashboard())
                .assignmentFailedDashboard(preferences.isAssignmentFailedDashboard())
                .slaWarningDashboard(preferences.isSlaWarningDashboard())
                .teamMemberAddedDashboard(preferences.isTeamMemberAddedDashboard())
                .createdAt(preferences.getCreatedAt())
                .updatedAt(preferences.getUpdatedAt())
                .build();
    }
    
    /**
     * Convert request to domain model
     */
    private NotificationPreferences convertFromRequest(UpdatePreferencesRequest request) {
        return NotificationPreferences.builder()
                .emailEnabled(request.isEmailEnabled())
                .dashboardEnabled(request.isDashboardEnabled())
                .emailAddress(request.getEmailAddress())
                .ticketAssignedEmail(request.isTicketAssignedEmail())
                .ticketReassignedEmail(request.isTicketReassignedEmail())
                .ticketUpdatedEmail(request.isTicketUpdatedEmail())
                .assignmentFailedEmail(request.isAssignmentFailedEmail())
                .slaWarningEmail(request.isSlaWarningEmail())
                .teamMemberAddedEmail(request.isTeamMemberAddedEmail())
                .ticketAssignedDashboard(request.isTicketAssignedDashboard())
                .ticketReassignedDashboard(request.isTicketReassignedDashboard())
                .ticketUpdatedDashboard(request.isTicketUpdatedDashboard())
                .assignmentFailedDashboard(request.isAssignmentFailedDashboard())
                .slaWarningDashboard(request.isSlaWarningDashboard())
                .teamMemberAddedDashboard(request.isTeamMemberAddedDashboard())
                .build();
    }
    
    /**
     * Request DTO for email update
     */
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class EmailUpdateRequest {
        private String emailAddress;
    }
    
    /**
     * Request DTO for enabled/disabled toggle
     */
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class EnabledRequest {
        private boolean enabled;
    }
    
    /**
     * Request DTO for type-specific preferences
     */
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class TypePreferencesRequest {
        private boolean emailEnabled;
        private boolean dashboardEnabled;
    }
}
