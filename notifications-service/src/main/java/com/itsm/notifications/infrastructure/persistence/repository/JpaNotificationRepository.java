package com.itsm.notifications.infrastructure.persistence.repository;

import com.itsm.notifications.domain.model.NotificationPriority;
import com.itsm.notifications.domain.model.NotificationType;
import com.itsm.notifications.infrastructure.persistence.entity.NotificationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * JPA Repository for Notification entities
 */
@Repository
public interface JpaNotificationRepository extends JpaRepository<NotificationEntity, UUID> {
    
    /**
     * Find notification by ID and user ID (for security)
     */
    Optional<NotificationEntity> findByIdAndUserId(UUID id, UUID userId);
    
    /**
     * Find all notifications for a user, ordered by creation date
     */
    List<NotificationEntity> findByUserIdOrderByCreatedAtDesc(UUID userId);
    
    /**
     * Find notifications for a user with pagination
     */
    Page<NotificationEntity> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);
    
    /**
     * Find unread notifications for a user
     */
    List<NotificationEntity> findByUserIdAndReadStatusOrderByCreatedAtDesc(UUID userId, boolean readStatus);
    
    /**
     * Find notifications by type for a user
     */
    List<NotificationEntity> findByUserIdAndTypeOrderByCreatedAtDesc(UUID userId, NotificationType type);
    
    /**
     * Find notifications by priority for a user
     */
    List<NotificationEntity> findByUserIdAndPriorityOrderByCreatedAtDesc(UUID userId, NotificationPriority priority);
    
    /**
     * Find notifications related to a specific ticket
     */
    List<NotificationEntity> findByTicketIdOrderByCreatedAtDesc(UUID ticketId);
    
    /**
     * Find notifications related to a specific assignment
     */
    List<NotificationEntity> findByAssignmentIdOrderByCreatedAtDesc(UUID assignmentId);
    
    /**
     * Count unread notifications for a user
     */
    @Query("SELECT COUNT(n) FROM NotificationEntity n WHERE n.userId = :userId AND n.readStatus = false")
    long countUnreadNotifications(@Param("userId") UUID userId);
    
    /**
     * Count notifications by type for a user
     */
    @Query("SELECT COUNT(n) FROM NotificationEntity n WHERE n.userId = :userId AND n.type = :type")
    long countNotificationsByType(@Param("userId") UUID userId, @Param("type") NotificationType type);
    
    /**
     * Find expired notifications
     */
    @Query("SELECT n FROM NotificationEntity n WHERE n.expiresAt IS NOT NULL AND n.expiresAt < :now")
    List<NotificationEntity> findExpiredNotifications(@Param("now") LocalDateTime now);
    
    /**
     * Find old read notifications for cleanup
     */
    @Query("SELECT n FROM NotificationEntity n WHERE n.readStatus = true AND n.createdAt < :cutoffDate")
    List<NotificationEntity> findOldReadNotifications(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    /**
     * Find recent notifications for a user (last N days)
     */
    @Query("SELECT n FROM NotificationEntity n WHERE n.userId = :userId AND n.createdAt >= :since ORDER BY n.createdAt DESC")
    List<NotificationEntity> findRecentNotifications(@Param("userId") UUID userId, @Param("since") LocalDateTime since);
    
    /**
     * Find high priority unread notifications for a user
     */
    @Query("SELECT n FROM NotificationEntity n WHERE n.userId = :userId AND n.readStatus = false AND n.priority IN ('HIGH', 'URGENT') ORDER BY n.priority DESC, n.createdAt DESC")
    List<NotificationEntity> findHighPriorityUnreadNotifications(@Param("userId") UUID userId);
    
    /**
     * Mark all notifications as read for a user
     */
    @Modifying
    @Query("UPDATE NotificationEntity n SET n.readStatus = true, n.readAt = :readAt WHERE n.userId = :userId AND n.readStatus = false")
    int markAllAsReadForUser(@Param("userId") UUID userId, @Param("readAt") LocalDateTime readAt);
    
    /**
     * Delete expired notifications
     */
    @Modifying
    @Query("DELETE FROM NotificationEntity n WHERE n.expiresAt IS NOT NULL AND n.expiresAt < :now")
    int deleteExpiredNotifications(@Param("now") LocalDateTime now);
    
    /**
     * Delete old read notifications
     */
    @Modifying
    @Query("DELETE FROM NotificationEntity n WHERE n.readStatus = true AND n.createdAt < :cutoffDate")
    int deleteOldReadNotifications(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    /**
     * Get notification statistics for a user
     */
    @Query("SELECT n.type, COUNT(n), SUM(CASE WHEN n.readStatus = false THEN 1 ELSE 0 END) FROM NotificationEntity n WHERE n.userId = :userId GROUP BY n.type")
    List<Object[]> getNotificationStatsByType(@Param("userId") UUID userId);
    
    /**
     * Find notifications that need attention (unread high priority or old unread)
     */
    @Query("SELECT n FROM NotificationEntity n WHERE n.userId = :userId AND n.readStatus = false AND " +
           "(n.priority IN ('HIGH', 'URGENT') OR n.createdAt < :oldDate) ORDER BY n.priority DESC, n.createdAt ASC")
    List<NotificationEntity> findNotificationsNeedingAttention(@Param("userId") UUID userId, @Param("oldDate") LocalDateTime oldDate);
    
    /**
     * Find notifications by multiple users (for bulk operations)
     */
    @Query("SELECT n FROM NotificationEntity n WHERE n.userId IN :userIds ORDER BY n.createdAt DESC")
    List<NotificationEntity> findByUserIds(@Param("userIds") List<UUID> userIds);
}
