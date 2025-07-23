package com.itsm.assignment.infrastructure.persistence.entity;

import com.itsm.assignment.domain.model.AssignmentStatus;
import com.itsm.assignment.domain.model.AssignmentStrategy;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA Entity for Assignment
 * Maps to assignments table in assignment_db database
 */
@Entity
@Table(name = "assignments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentEntity {
    
    @Id
    @Column(name = "id", columnDefinition = "UUID")
    private UUID id;
    
    @Column(name = "ticket_id", nullable = false, columnDefinition = "UUID")
    private UUID ticketId;
    
    @Column(name = "technician_id", nullable = false, columnDefinition = "UUID")
    private UUID technicianId;
    
    @Column(name = "team_id", nullable = false, columnDefinition = "UUID")
    private UUID teamId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "assignment_strategy", nullable = false, length = 20)
    private AssignmentStrategy strategy;
    
    @Column(name = "confidence_score", precision = 3, scale = 2)
    private BigDecimal confidenceScore;
    
    @Column(name = "assignment_reason", columnDefinition = "TEXT")
    private String assignmentReason;
    
    @Column(name = "nlp_analysis_json", columnDefinition = "TEXT")
    private String nlpAnalysisJson;
    
    @Column(name = "assigned_at", nullable = false)
    private LocalDateTime assignedAt;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private AssignmentStatus status = AssignmentStatus.ACTIVE;
    
    @Column(name = "reassigned_by", columnDefinition = "UUID")
    private UUID reassignedBy;
    
    @Column(name = "reassignment_reason", columnDefinition = "TEXT")
    private String reassignmentReason;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (assignedAt == null) {
            assignedAt = LocalDateTime.now();
        }
        if (status == null) {
            status = AssignmentStatus.ACTIVE;
        }
    }
}
