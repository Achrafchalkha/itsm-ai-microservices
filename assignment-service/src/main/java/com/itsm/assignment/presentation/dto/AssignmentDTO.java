package com.itsm.assignment.presentation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for Assignment responses
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentDTO {
    
    private UUID id;
    private UUID ticketId;
    private UUID technicianId;
    private UUID teamId;
    private String strategy;
    private BigDecimal confidenceScore;
    private String assignmentReason;
    private LocalDateTime assignedAt;
    private String status;
    private UUID reassignedBy;
    private String reassignmentReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Additional context information (optional)
    private String technicianName;
    private String teamName;
    private String ticketTitle;
    private String ticketCategory;
}
