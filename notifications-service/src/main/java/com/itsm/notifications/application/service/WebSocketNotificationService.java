package com.itsm.notifications.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itsm.notifications.domain.model.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Service for sending real-time notifications via WebSocket
 * Handles dashboard notifications and real-time updates
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WebSocketNotificationService {
    
    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;
    
    @Value("${notifications.websocket.enabled:true}")
    private boolean webSocketEnabled;
    
    @Value("${notifications.websocket.topic:/topic/notifications}")
    private String notificationTopic;
    
    /**
     * Send notification to a specific user via WebSocket
     */
    public void sendNotificationToUser(Notification notification) {
        if (!webSocketEnabled) {
            log.debug("WebSocket notifications are disabled");
            return;
        }
        
        log.debug("Sending WebSocket notification {} to user: {}", 
                notification.getId(), notification.getUserId());
        
        try {
            // Create notification payload
            Map<String, Object> payload = createNotificationPayload(notification);
            
            // Send to user-specific topic
            String userTopic = "/topic/notifications/" + notification.getUserId();
            messagingTemplate.convertAndSend(userTopic, payload);
            
            log.debug("Successfully sent WebSocket notification: {}", notification.getId());
            
        } catch (Exception e) {
            log.error("Failed to send WebSocket notification {}: {}", 
                    notification.getId(), e.getMessage(), e);
        }
    }
    
    /**
     * Send notification to all users (broadcast)
     */
    public void broadcastNotification(Notification notification) {
        if (!webSocketEnabled) {
            log.debug("WebSocket notifications are disabled");
            return;
        }
        
        log.debug("Broadcasting WebSocket notification: {}", notification.getId());
        
        try {
            Map<String, Object> payload = createNotificationPayload(notification);
            messagingTemplate.convertAndSend(notificationTopic, payload);
            
            log.debug("Successfully broadcasted WebSocket notification: {}", notification.getId());
            
        } catch (Exception e) {
            log.error("Failed to broadcast WebSocket notification {}: {}", 
                    notification.getId(), e.getMessage(), e);
        }
    }
    
    /**
     * Send unread count update to user
     */
    public void sendUnreadCountUpdate(String userId, int unreadCount) {
        if (!webSocketEnabled) {
            return;
        }
        
        log.debug("Sending unread count update to user {}: {}", userId, unreadCount);
        
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("type", "UNREAD_COUNT_UPDATE");
            payload.put("unreadCount", unreadCount);
            payload.put("timestamp", java.time.LocalDateTime.now());
            
            String userTopic = "/topic/notifications/" + userId + "/count";
            messagingTemplate.convertAndSend(userTopic, payload);
            
        } catch (Exception e) {
            log.error("Failed to send unread count update to user {}: {}", userId, e.getMessage(), e);
        }
    }
    
    /**
     * Send notification read status update
     */
    public void sendReadStatusUpdate(String userId, String notificationId, boolean isRead) {
        if (!webSocketEnabled) {
            return;
        }
        
        log.debug("Sending read status update to user {}: notification {} marked as {}", 
                userId, notificationId, isRead ? "read" : "unread");
        
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("type", "READ_STATUS_UPDATE");
            payload.put("notificationId", notificationId);
            payload.put("isRead", isRead);
            payload.put("timestamp", java.time.LocalDateTime.now());
            
            String userTopic = "/topic/notifications/" + userId + "/status";
            messagingTemplate.convertAndSend(userTopic, payload);
            
        } catch (Exception e) {
            log.error("Failed to send read status update to user {}: {}", userId, e.getMessage(), e);
        }
    }
    
    /**
     * Send system alert to all connected users
     */
    public void sendSystemAlert(String title, String message, String priority) {
        if (!webSocketEnabled) {
            return;
        }
        
        log.info("Sending system alert: {}", title);
        
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("type", "SYSTEM_ALERT");
            payload.put("title", title);
            payload.put("message", message);
            payload.put("priority", priority);
            payload.put("timestamp", java.time.LocalDateTime.now());
            
            messagingTemplate.convertAndSend("/topic/alerts", payload);
            
            log.info("Successfully sent system alert: {}", title);
            
        } catch (Exception e) {
            log.error("Failed to send system alert: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Create notification payload for WebSocket transmission
     */
    private Map<String, Object> createNotificationPayload(Notification notification) {
        Map<String, Object> payload = new HashMap<>();
        
        payload.put("id", notification.getId());
        payload.put("type", notification.getType().name());
        payload.put("title", notification.getTitle());
        payload.put("message", notification.getMessage());
        payload.put("priority", notification.getPriority().name());
        payload.put("readStatus", notification.isReadStatus());
        payload.put("createdAt", notification.getCreatedAt());
        payload.put("expiresAt", notification.getExpiresAt());
        
        // Add context information
        if (notification.getTicketId() != null) {
            payload.put("ticketId", notification.getTicketId());
        }
        if (notification.getAssignmentId() != null) {
            payload.put("assignmentId", notification.getAssignmentId());
        }
        if (notification.getRelatedUserId() != null) {
            payload.put("relatedUserId", notification.getRelatedUserId());
        }
        
        // Add additional data
        if (notification.getData() != null) {
            payload.put("data", notification.getData());
        }
        
        return payload;
    }
    
    /**
     * Test WebSocket connectivity
     */
    public void sendTestMessage(String userId) {
        if (!webSocketEnabled) {
            throw new IllegalStateException("WebSocket notifications are disabled");
        }
        
        log.info("Sending test WebSocket message to user: {}", userId);
        
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("type", "TEST_MESSAGE");
            payload.put("message", "This is a test WebSocket message");
            payload.put("timestamp", java.time.LocalDateTime.now());
            
            String userTopic = "/topic/notifications/" + userId;
            messagingTemplate.convertAndSend(userTopic, payload);
            
            log.info("Successfully sent test WebSocket message to user: {}", userId);
            
        } catch (Exception e) {
            log.error("Failed to send test WebSocket message to user {}: {}", userId, e.getMessage(), e);
            throw new RuntimeException("Failed to send test WebSocket message", e);
        }
    }
}
