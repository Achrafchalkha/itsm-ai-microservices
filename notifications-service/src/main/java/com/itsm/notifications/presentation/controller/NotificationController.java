package com.itsm.notifications.presentation.controller;

import com.itsm.notifications.application.service.NotificationService;
import com.itsm.notifications.domain.model.Notification;
import com.itsm.notifications.presentation.dto.NotificationDTO;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * REST Controller for notification operations
 * Provides endpoints for retrieving and managing user notifications
 */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final NotificationService notificationService;

    @Value("${jwt.secret}")
    private String jwtSecret;
    
    /**
     * Get notifications for the current user
     */
    @GetMapping
    @PreAuthorize("hasRole('UTILISATEUR') or hasRole('TECHNICIEN') or hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<List<NotificationDTO>> getUserNotifications(
            Authentication authentication,
            @RequestParam(defaultValue = "false") boolean unreadOnly,
            @RequestParam(defaultValue = "50") int limit) {

        log.info("🔔 getUserNotifications called - Authentication: {}", authentication.getName());
        log.info("🔔 Authentication authorities: {}", authentication.getAuthorities());

        UUID userId = extractUserIdFromAuth(authentication);
        log.info("🔔 Extracted userId: {} for user: {}", userId, authentication.getName());
        log.debug("Getting notifications for user: {}, unreadOnly: {}, limit: {}", userId, unreadOnly, limit);
        
        try {
            List<Notification> notifications = notificationService.getUserNotifications(userId, unreadOnly, limit);
            List<NotificationDTO> response = notifications.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error getting notifications for user {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get unread notification count for the current user
     */
    @GetMapping("/count/unread")
    @PreAuthorize("hasRole('UTILISATEUR') or hasRole('TECHNICIEN') or hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<UnreadCountDTO> getUnreadCount(Authentication authentication) {
        UUID userId = extractUserIdFromAuth(authentication);
        log.debug("Getting unread count for user: {}", userId);
        
        try {
            List<Notification> unreadNotifications = notificationService.getUserNotifications(userId, true, Integer.MAX_VALUE);
            UnreadCountDTO response = UnreadCountDTO.builder()
                    .count(unreadNotifications.size())
                    .build();
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error getting unread count for user {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Mark a specific notification as read
     */
    @PutMapping("/{notificationId}/read")
    @PreAuthorize("hasRole('UTILISATEUR') or hasRole('TECHNICIEN') or hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<Void> markAsRead(
            @PathVariable UUID notificationId,
            Authentication authentication) {
        
        UUID userId = extractUserIdFromAuth(authentication);
        log.debug("Marking notification {} as read for user: {}", notificationId, userId);
        
        try {
            notificationService.markAsRead(notificationId, userId);
            return ResponseEntity.ok().build();
            
        } catch (IllegalArgumentException e) {
            log.warn("Notification not found: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error marking notification as read: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Mark all notifications as read for the current user
     */
    @PutMapping("/read-all")
    @PreAuthorize("hasRole('UTILISATEUR') or hasRole('TECHNICIEN') or hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<Void> markAllAsRead(Authentication authentication) {
        UUID userId = extractUserIdFromAuth(authentication);
        log.debug("Marking all notifications as read for user: {}", userId);
        
        try {
            notificationService.markAllAsRead(userId);
            return ResponseEntity.ok().build();
            
        } catch (Exception e) {
            log.error("Error marking all notifications as read for user {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get notifications with pagination
     */
    @GetMapping("/paginated")
    @PreAuthorize("hasRole('UTILISATEUR') or hasRole('TECHNICIEN') or hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<NotificationPageDTO> getNotificationsPaginated(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "false") boolean unreadOnly) {
        
        UUID userId = extractUserIdFromAuth(authentication);
        log.debug("Getting paginated notifications for user: {}, page: {}, size: {}", userId, page, size);
        
        try {
            // For simplicity, we'll use the existing service method with limit
            // In a real implementation, you'd want to add pagination to the service
            int offset = page * size;
            List<Notification> allNotifications = notificationService.getUserNotifications(userId, unreadOnly, offset + size);
            
            // Manual pagination (in production, implement proper pagination in service/repository)
            List<Notification> pageNotifications = allNotifications.stream()
                    .skip(offset)
                    .limit(size)
                    .collect(Collectors.toList());
            
            List<NotificationDTO> content = pageNotifications.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
            
            NotificationPageDTO response = NotificationPageDTO.builder()
                    .content(content)
                    .page(page)
                    .size(size)
                    .totalElements(allNotifications.size())
                    .totalPages((int) Math.ceil((double) allNotifications.size() / size))
                    .build();
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error getting paginated notifications for user {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get notifications by type
     */
    @GetMapping("/type/{type}")
    @PreAuthorize("hasRole('UTILISATEUR') or hasRole('TECHNICIEN') or hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<List<NotificationDTO>> getNotificationsByType(
            @PathVariable String type,
            Authentication authentication,
            @RequestParam(defaultValue = "20") int limit) {
        
        UUID userId = extractUserIdFromAuth(authentication);
        log.debug("Getting notifications by type {} for user: {}", type, userId);
        
        try {
            // Filter notifications by type from all user notifications
            List<Notification> allNotifications = notificationService.getUserNotifications(userId, false, Integer.MAX_VALUE);
            List<Notification> filteredNotifications = allNotifications.stream()
                    .filter(n -> n.getType().name().equals(type.toUpperCase()))
                    .limit(limit)
                    .collect(Collectors.toList());
            
            List<NotificationDTO> response = filteredNotifications.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error getting notifications by type for user {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Extract user ID from authentication
     * Extracts userId from JWT claims
     */
    private UUID extractUserIdFromAuth(Authentication authentication) {
        try {
            // Get the Authorization header from the current request
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
            String authHeader = request.getHeader("Authorization");

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);

                // Parse JWT token to extract userId
                SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
                Claims claims = Jwts.parserBuilder()
                        .setSigningKey(key)
                        .build()
                        .parseClaimsJws(token)
                        .getBody();

                String userIdStr = claims.get("userId", String.class);
                if (userIdStr != null) {
                    return UUID.fromString(userIdStr);
                }
            }

            log.warn("Could not extract userId from authentication");
            throw new RuntimeException("User ID not found in authentication");

        } catch (Exception e) {
            log.error("Error extracting user ID from authentication: {}", e.getMessage());
            throw new RuntimeException("Invalid authentication", e);
        }
    }
    
    /**
     * Convert Notification domain model to DTO
     */
    private NotificationDTO convertToDTO(Notification notification) {
        return NotificationDTO.builder()
                .id(notification.getId())
                .type(notification.getType().name())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .priority(notification.getPriority().name())
                .channel(notification.getChannel().name())
                .readStatus(notification.isReadStatus())
                .createdAt(notification.getCreatedAt())
                .readAt(notification.getReadAt())
                .expiresAt(notification.getExpiresAt())
                .ticketId(notification.getTicketId())
                .assignmentId(notification.getAssignmentId())
                .relatedUserId(notification.getRelatedUserId())
                .data(notification.getData())
                .build();
    }
    
    /**
     * DTO for unread count response
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class UnreadCountDTO {
        private int count;
    }
    
    /**
     * DTO for paginated notification response
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class NotificationPageDTO {
        private List<NotificationDTO> content;
        private int page;
        private int size;
        private long totalElements;
        private int totalPages;
    }
}
