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
 * JPA Entity for Daily KPI
 * Maps to daily_kpis table in analytics_db
 */
@Entity
@Table(name = "daily_kpis", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"date_kpi"}))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyKPIEntity {
    
    @Id
    @Column(name = "id", columnDefinition = "UUID")
    private UUID id;
    
    @Column(name = "date_kpi", nullable = false, unique = true)
    private LocalDate dateKpi;
    
    // Global metrics
    @Column(name = "total_tickets_created")
    @Builder.Default
    private Integer totalTicketsCreated = 0;
    
    @Column(name = "total_tickets_resolved")
    @Builder.Default
    private Integer totalTicketsResolved = 0;
    
    @Column(name = "total_tickets_closed")
    @Builder.Default
    private Integer totalTicketsClosed = 0;
    
    // SLA metrics
    @Column(name = "tickets_within_sla")
    @Builder.Default
    private Integer ticketsWithinSla = 0;
    
    @Column(name = "tickets_breached_sla")
    @Builder.Default
    private Integer ticketsBreachedSla = 0;
    
    @Column(name = "average_resolution_time_minutes", precision = 10, scale = 2)
    private BigDecimal averageResolutionTimeMinutes;
    
    @Column(name = "average_first_response_time_minutes", precision = 10, scale = 2)
    private BigDecimal averageFirstResponseTimeMinutes;
    
    // Assignment metrics
    @Column(name = "total_assignments")
    @Builder.Default
    private Integer totalAssignments = 0;
    
    @Column(name = "total_reassignments")
    @Builder.Default
    private Integer totalReassignments = 0;
    
    @Column(name = "average_assignment_confidence", precision = 3, scale = 2)
    private BigDecimal averageAssignmentConfidence;
    
    // Satisfaction metrics
    @Column(name = "total_satisfaction_responses")
    @Builder.Default
    private Integer totalSatisfactionResponses = 0;
    
    @Column(name = "average_satisfaction_score", precision = 3, scale = 2)
    private BigDecimal averageSatisfactionScore;
    
    // Detailed metrics (JSON)
    @Column(name = "team_metrics_json", columnDefinition = "TEXT")
    private String teamMetricsJson;
    
    @Column(name = "technician_metrics_json", columnDefinition = "TEXT")
    private String technicianMetricsJson;
    
    @Column(name = "category_metrics_json", columnDefinition = "TEXT")
    private String categoryMetricsJson;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
