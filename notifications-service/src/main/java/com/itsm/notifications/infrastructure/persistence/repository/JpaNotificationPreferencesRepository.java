package com.itsm.notifications.infrastructure.persistence.repository;

import com.itsm.notifications.infrastructure.persistence.entity.NotificationPreferencesEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * JPA Repository for NotificationPreferences entities
 */
@Repository
public interface JpaNotificationPreferencesRepository extends JpaRepository<NotificationPreferencesEntity, UUID> {
    
    /**
     * Find users who have email notifications enabled
     */
    @Query("SELECT p FROM NotificationPreferencesEntity p WHERE p.emailEnabled = true AND p.emailAddress IS NOT NULL")
    List<NotificationPreferencesEntity> findUsersWithEmailEnabled();
    
    /**
     * Find users who have dashboard notifications enabled
     */
    @Query("SELECT p FROM NotificationPreferencesEntity p WHERE p.dashboardEnabled = true")
    List<NotificationPreferencesEntity> findUsersWithDashboardEnabled();
    
    /**
     * Find users who want email notifications for a specific type
     */
    @Query("SELECT p FROM NotificationPreferencesEntity p WHERE p.emailEnabled = true AND p.emailAddress IS NOT NULL AND " +
           "CASE :notificationType " +
           "WHEN 'TICKET_ASSIGNED' THEN p.ticketAssignedEmail = true " +
           "WHEN 'TICKET_REASSIGNED' THEN p.ticketReassignedEmail = true " +
           "WHEN 'TICKET_UPDATED' THEN p.ticketUpdatedEmail = true " +
           "WHEN 'ASSIGNMENT_FAILED' THEN p.assignmentFailedEmail = true " +
           "WHEN 'SLA_WARNING' THEN p.slaWarningEmail = true " +
           "WHEN 'TEAM_MEMBER_ADDED' THEN p.teamMemberAddedEmail = true " +
           "ELSE false END")
    List<NotificationPreferencesEntity> findUsersWantingEmailFor(@Param("notificationType") String notificationType);
    
    /**
     * Find users by email address
     */
    @Query("SELECT p FROM NotificationPreferencesEntity p WHERE p.emailAddress = :emailAddress")
    List<NotificationPreferencesEntity> findByEmailAddress(@Param("emailAddress") String emailAddress);
}
