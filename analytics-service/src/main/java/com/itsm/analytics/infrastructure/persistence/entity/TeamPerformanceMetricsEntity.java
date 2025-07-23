package com.itsm.analytics.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA Entity for Team Performance Metrics
 * Maps to team_performance_metrics table in analytics_db
 */
@Entity
@Table(name = "team_performance_metrics")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamPerformanceMetricsEntity {
    
    @Id
    @Column(name = "id", columnDefinition = "UUID")
    private UUID id;
    
    @Column(name = "team_id", nullable = false, columnDefinition = "UUID")
    private UUID teamId;
    
    @Column(name = "date_debut", nullable = false)
    private LocalDate dateDebut;
    
    @Column(name = "date_fin", nullable = false)
    private LocalDate dateFin;
    
    @Column(name = "periode_type", nullable = false, length = 20)
    private String periodeType; // DAILY, WEEKLY, MONTHLY
    
    // Volume metrics
    @Column(name = "total_tickets_assigned")
    @Builder.Default
    private Integer totalTicketsAssigned = 0;
    
    @Column(name = "total_tickets_resolved")
    @Builder.Default
    private Integer totalTicketsResolved = 0;
    
    @Column(name = "total_tickets_closed")
    @Builder.Default
    private Integer totalTicketsClosed = 0;
    
    // Performance metrics
    @Column(name = "average_resolution_time_minutes", precision = 10, scale = 2)
    private BigDecimal averageResolutionTimeMinutes;
    
    @Column(name = "average_first_response_time_minutes", precision = 10, scale = 2)
    private BigDecimal averageFirstResponseTimeMinutes;
    
    // SLA metrics
    @Column(name = "tickets_within_sla")
    @Builder.Default
    private Integer ticketsWithinSla = 0;
    
    @Column(name = "tickets_breached_sla")
    @Builder.Default
    private Integer ticketsBreachedSla = 0;
    
    @Column(name = "sla_compliance_rate", precision = 5, scale = 2)
    private BigDecimal slaComplianceRate;
    
    // Assignment metrics
    @Column(name = "total_assignments")
    @Builder.Default
    private Integer totalAssignments = 0;
    
    @Column(name = "total_reassignments")
    @Builder.Default
    private Integer totalReassignments = 0;
    
    @Column(name = "reassignment_rate", precision = 5, scale = 2)
    private BigDecimal reassignmentRate;
    
    // Satisfaction metrics
    @Column(name = "total_satisfaction_responses")
    @Builder.Default
    private Integer totalSatisfactionResponses = 0;
    
    @Column(name = "average_satisfaction_score", precision = 3, scale = 2)
    private BigDecimal averageSatisfactionScore;
    
    // Team composition
    @Column(name = "total_technicians")
    @Builder.Default
    private Integer totalTechnicians = 0;
    
    @Column(name = "active_technicians")
    @Builder.Default
    private Integer activeTechnicians = 0;
    
    @Column(name = "average_workload", precision = 5, scale = 2)
    private BigDecimal averageWorkload;
    
    @Column(name = "max_workload")
    private Integer maxWorkload;
    
    @Column(name = "min_workload")
    private Integer minWorkload;
    
    // Performance level
    @Column(name = "performance_level", length = 20)
    private String performanceLevel; // EXCELLENT, BON, MOYEN, FAIBLE
    
    // Detailed metrics (JSON)
    @Column(name = "category_breakdown_json", columnDefinition = "TEXT")
    private String categoryBreakdownJson;
    
    @Column(name = "priority_breakdown_json", columnDefinition = "TEXT")
    private String priorityBreakdownJson;
    
    @Column(name = "technician_metrics_json", columnDefinition = "TEXT")
    private String technicianMetricsJson;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
