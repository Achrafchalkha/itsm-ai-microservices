package com.itsm.assignment.presentation.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Request DTO for manual assignment
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ManualAssignmentRequest {
    
    @NotNull(message = "Ticket ID is required")
    private UUID ticketId;
    
    @NotNull(message = "Technician ID is required")
    private UUID technicianId;
    
    @NotNull(message = "Assigned by user ID is required")
    private UUID assignedBy;
    
    private String reason;
}
