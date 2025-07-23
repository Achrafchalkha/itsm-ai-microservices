package com.itsm.notifications.infrastructure.kafka.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Event received when automatic assignment fails
 * Triggers notification to manager for manual intervention
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentFailedEvent {
    
    private UUID ticketId;
    private String failureReason;
    private LocalDateTime failedAt;
    private String ticketCategory;
    private String ticketPriority;
    
    // Context for fallback actions
    private UUID fallbackManagerId;
    private String fallbackManagerEmail;
    private UUID ticketCreatorId;
}
