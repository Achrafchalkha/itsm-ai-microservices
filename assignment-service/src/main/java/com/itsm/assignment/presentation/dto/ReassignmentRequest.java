package com.itsm.assignment.presentation.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Request DTO for ticket reassignment
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReassignmentRequest {
    
    @NotNull(message = "New technician ID is required")
    private UUID newTechnicianId;
    
    @NotNull(message = "Reassigned by user ID is required")
    private UUID reassignedBy;
    
    @NotNull(message = "Reassignment reason is required")
    private String reason;
}
