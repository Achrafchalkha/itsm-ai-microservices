package com.itsm.notifications.infrastructure.kafka.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Event received when a ticket is reassigned to a different technician
 * Triggers notifications for both old and new technicians
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentReassignedEvent {
    
    private UUID assignmentId;
    private UUID ticketId;
    private UUID previousTechnicianId;
    private UUID newTechnicianId;
    private UUID reassignedBy;
    private String reassignmentReason;
    private LocalDateTime reassignedAt;
    
    // Context information
    private String ticketTitre;
    private String previousTechnicianName;
    private String newTechnicianName;
    private String reassignedByName;
}
