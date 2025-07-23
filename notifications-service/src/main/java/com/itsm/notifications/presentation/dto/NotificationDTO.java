package com.itsm.notifications.presentation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * DTO for Notification responses
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO {
    
    private UUID id;
    private String type;
    private String title;
    private String message;
    private String priority;
    private String channel;
    private boolean readStatus;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;
    private LocalDateTime expiresAt;
    
    // Context information
    private UUID ticketId;
    private UUID assignmentId;
    private UUID relatedUserId;
    
    // Additional data
    private Map<String, Object> data;
}
