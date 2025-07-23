package com.itsm.assignment.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain model for Assignment
 * Represents the assignment of a ticket to a technician
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Assignment {
    
    private UUID id;
    private UUID ticketId;
    private UUID technicianId;
    private UUID teamId;
    private AssignmentStrategy strategy;
    private BigDecimal confidenceScore;
    private String assignmentReason;
    private String nlpAnalysisJson;
    private LocalDateTime assignedAt;
    private AssignmentStatus status;
    private UUID reassignedBy;
    private String reassignmentReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    /**
     * Factory method to create a new assignment
     */
    public static Assignment createAssignment(UUID ticketId, UUID technicianId, UUID teamId, 
                                            AssignmentStrategy strategy, BigDecimal confidenceScore,
                                            String reason, String nlpAnalysis) {
        return Assignment.builder()
                .id(UUID.randomUUID())
                .ticketId(ticketId)
                .technicianId(technicianId)
                .teamId(teamId)
                .strategy(strategy)
                .confidenceScore(confidenceScore)
                .assignmentReason(reason)
                .nlpAnalysisJson(nlpAnalysis)
                .assignedAt(LocalDateTime.now())
                .status(AssignmentStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
    
    /**
     * Reassign to another technician
     */
    public void reassign(UUID newTechnicianId, UUID reassignedBy, String reason) {
        this.technicianId = newTechnicianId;
        this.status = AssignmentStatus.REASSIGNED;
        this.reassignedBy = reassignedBy;
        this.reassignmentReason = reason;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Mark assignment as completed
     */
    public void complete() {
        this.status = AssignmentStatus.COMPLETED;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Cancel assignment
     */
    public void cancel(String reason) {
        this.status = AssignmentStatus.CANCELLED;
        this.reassignmentReason = reason;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Check if assignment is active
     */
    public boolean isActive() {
        return AssignmentStatus.ACTIVE.equals(this.status);
    }
    
    /**
     * Check if assignment has high confidence
     */
    public boolean hasHighConfidence() {
        return confidenceScore != null && confidenceScore.compareTo(BigDecimal.valueOf(0.7)) >= 0;
    }
}
