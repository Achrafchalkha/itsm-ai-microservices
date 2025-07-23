package com.itsm.notifications.application.service;

import com.itsm.notifications.domain.model.NotificationPreferences;
import com.itsm.notifications.domain.model.NotificationType;
import com.itsm.notifications.infrastructure.persistence.entity.NotificationPreferencesEntity;
import com.itsm.notifications.infrastructure.persistence.repository.JpaNotificationPreferencesRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Service for managing user notification preferences
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationPreferencesService {
    
    private final JpaNotificationPreferencesRepository preferencesRepository;
    
    /**
     * Get notification preferences for a user
     * Creates default preferences if none exist
     */
    @Transactional
    public NotificationPreferences getPreferences(UUID userId) {
        log.debug("Getting notification preferences for user: {}", userId);
        
        return preferencesRepository.findById(userId)
                .map(this::convertFromEntity)
                .orElseGet(() -> createDefaultPreferences(userId));
    }
    
    /**
     * Update notification preferences for a user
     */
    @Transactional
    public NotificationPreferences updatePreferences(UUID userId, NotificationPreferences preferences) {
        log.info("Updating notification preferences for user: {}", userId);
        
        preferences.setUserId(userId);
        preferences.setUpdatedAt(java.time.LocalDateTime.now());
        
        NotificationPreferencesEntity entity = convertToEntity(preferences);
        entity = preferencesRepository.save(entity);
        
        log.info("Successfully updated notification preferences for user: {}", userId);
        return convertFromEntity(entity);
    }
    
    /**
     * Update email address for a user
     */
    @Transactional
    public void updateEmailAddress(UUID userId, String emailAddress) {
        log.info("Updating email address for user: {}", userId);
        
        NotificationPreferencesEntity entity = preferencesRepository.findById(userId)
                .orElse(createDefaultPreferencesEntity(userId, emailAddress));
        
        entity.setEmailAddress(emailAddress);
        entity.setUpdatedAt(java.time.LocalDateTime.now());
        
        preferencesRepository.save(entity);
        
        log.info("Successfully updated email address for user: {}", userId);
    }
    
    /**
     * Enable/disable email notifications for a user
     */
    @Transactional
    public void setEmailEnabled(UUID userId, boolean enabled) {
        log.info("Setting email notifications {} for user: {}", enabled ? "enabled" : "disabled", userId);
        
        NotificationPreferencesEntity entity = preferencesRepository.findById(userId)
                .orElse(createDefaultPreferencesEntity(userId, null));
        
        entity.setEmailEnabled(enabled);
        entity.setUpdatedAt(java.time.LocalDateTime.now());
        
        preferencesRepository.save(entity);
        
        log.info("Successfully {} email notifications for user: {}", 
                enabled ? "enabled" : "disabled", userId);
    }
    
    /**
     * Enable/disable dashboard notifications for a user
     */
    @Transactional
    public void setDashboardEnabled(UUID userId, boolean enabled) {
        log.info("Setting dashboard notifications {} for user: {}", enabled ? "enabled" : "disabled", userId);
        
        NotificationPreferencesEntity entity = preferencesRepository.findById(userId)
                .orElse(createDefaultPreferencesEntity(userId, null));
        
        entity.setDashboardEnabled(enabled);
        entity.setUpdatedAt(java.time.LocalDateTime.now());
        
        preferencesRepository.save(entity);
        
        log.info("Successfully {} dashboard notifications for user: {}", 
                enabled ? "enabled" : "disabled", userId);
    }
    
    /**
     * Update preference for a specific notification type and channel
     */
    @Transactional
    public void updateTypePreference(UUID userId, NotificationType type, boolean emailEnabled, boolean dashboardEnabled) {
        log.info("Updating {} preferences for user: {} - email: {}, dashboard: {}", 
                type, userId, emailEnabled, dashboardEnabled);
        
        NotificationPreferencesEntity entity = preferencesRepository.findById(userId)
                .orElse(createDefaultPreferencesEntity(userId, null));
        
        // Update email preference
        switch (type) {
            case TICKET_ASSIGNED -> entity.setTicketAssignedEmail(emailEnabled);
            case TICKET_REASSIGNED -> entity.setTicketReassignedEmail(emailEnabled);
            case TICKET_UPDATED -> entity.setTicketUpdatedEmail(emailEnabled);
            case ASSIGNMENT_FAILED -> entity.setAssignmentFailedEmail(emailEnabled);
            case SLA_WARNING -> entity.setSlaWarningEmail(emailEnabled);
            case TEAM_MEMBER_ADDED -> entity.setTeamMemberAddedEmail(emailEnabled);
        }
        
        // Update dashboard preference
        switch (type) {
            case TICKET_ASSIGNED -> entity.setTicketAssignedDashboard(dashboardEnabled);
            case TICKET_REASSIGNED -> entity.setTicketReassignedDashboard(dashboardEnabled);
            case TICKET_UPDATED -> entity.setTicketUpdatedDashboard(dashboardEnabled);
            case ASSIGNMENT_FAILED -> entity.setAssignmentFailedDashboard(dashboardEnabled);
            case SLA_WARNING -> entity.setSlaWarningDashboard(dashboardEnabled);
            case TEAM_MEMBER_ADDED -> entity.setTeamMemberAddedDashboard(dashboardEnabled);
        }
        
        entity.setUpdatedAt(java.time.LocalDateTime.now());
        preferencesRepository.save(entity);
        
        log.info("Successfully updated {} preferences for user: {}", type, userId);
    }
    
    /**
     * Create default preferences for a new user
     */
    private NotificationPreferences createDefaultPreferences(UUID userId) {
        log.info("Creating default notification preferences for user: {}", userId);
        
        NotificationPreferences defaultPrefs = NotificationPreferences.createDefaultPreferences(userId, null);
        NotificationPreferencesEntity entity = convertToEntity(defaultPrefs);
        entity = preferencesRepository.save(entity);
        
        log.info("Successfully created default notification preferences for user: {}", userId);
        return convertFromEntity(entity);
    }
    
    /**
     * Create default preferences entity
     */
    private NotificationPreferencesEntity createDefaultPreferencesEntity(UUID userId, String emailAddress) {
        return NotificationPreferencesEntity.builder()
                .userId(userId)
                .emailEnabled(true)
                .dashboardEnabled(true)
                .emailAddress(emailAddress)
                .ticketAssignedEmail(true)
                .ticketReassignedEmail(true)
                .ticketUpdatedEmail(false)
                .assignmentFailedEmail(true)
                .slaWarningEmail(true)
                .teamMemberAddedEmail(false)
                .ticketAssignedDashboard(true)
                .ticketReassignedDashboard(true)
                .ticketUpdatedDashboard(true)
                .assignmentFailedDashboard(true)
                .slaWarningDashboard(true)
                .teamMemberAddedDashboard(true)
                .createdAt(java.time.LocalDateTime.now())
                .updatedAt(java.time.LocalDateTime.now())
                .build();
    }
    
    /**
     * Convert entity to domain model
     */
    private NotificationPreferences convertFromEntity(NotificationPreferencesEntity entity) {
        return NotificationPreferences.builder()
                .userId(entity.getUserId())
                .emailEnabled(entity.getEmailEnabled())
                .dashboardEnabled(entity.getDashboardEnabled())
                .emailAddress(entity.getEmailAddress())
                .ticketAssignedEmail(entity.getTicketAssignedEmail())
                .ticketReassignedEmail(entity.getTicketReassignedEmail())
                .ticketUpdatedEmail(entity.getTicketUpdatedEmail())
                .assignmentFailedEmail(entity.getAssignmentFailedEmail())
                .slaWarningEmail(entity.getSlaWarningEmail())
                .teamMemberAddedEmail(entity.getTeamMemberAddedEmail())
                .ticketAssignedDashboard(entity.getTicketAssignedDashboard())
                .ticketReassignedDashboard(entity.getTicketReassignedDashboard())
                .ticketUpdatedDashboard(entity.getTicketUpdatedDashboard())
                .assignmentFailedDashboard(entity.getAssignmentFailedDashboard())
                .slaWarningDashboard(entity.getSlaWarningDashboard())
                .teamMemberAddedDashboard(entity.getTeamMemberAddedDashboard())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
    
    /**
     * Convert domain model to entity
     */
    private NotificationPreferencesEntity convertToEntity(NotificationPreferences preferences) {
        return NotificationPreferencesEntity.builder()
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
}
