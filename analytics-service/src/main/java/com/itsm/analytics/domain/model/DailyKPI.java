package com.itsm.analytics.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain model for Daily KPI aggregations
 * Contains daily performance metrics for analytics
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyKPI {
    
    private UUID id;
    private LocalDate dateKpi;
    
    // Global metrics
    private Integer totalTicketsCreated;
    private Integer totalTicketsResolved;
    private Integer totalTicketsClosed;
    
    // SLA metrics
    private Integer ticketsWithinSla;
    private Integer ticketsBreachedSla;
    private BigDecimal averageResolutionTimeMinutes;
    private BigDecimal averageFirstResponseTimeMinutes;
    
    // Assignment metrics
    private Integer totalAssignments;
    private Integer totalReassignments;
    private BigDecimal averageAssignmentConfidence;
    
    // Satisfaction metrics
    private Integer totalSatisfactionResponses;
    private BigDecimal averageSatisfactionScore;
    
    // Detailed metrics (JSON)
    private String teamMetricsJson;
    private String technicianMetricsJson;
    private String categoryMetricsJson;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    /**
     * Factory method to create daily KPI for a specific date
     */
    public static DailyKPI creerKPIJournalier(LocalDate date) {
        return DailyKPI.builder()
                .id(UUID.randomUUID())
                .dateKpi(date)
                .totalTicketsCreated(0)
                .totalTicketsResolved(0)
                .totalTicketsClosed(0)
                .ticketsWithinSla(0)
                .ticketsBreachedSla(0)
                .totalAssignments(0)
                .totalReassignments(0)
                .totalSatisfactionResponses(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
    
    /**
     * Update ticket creation metrics
     */
    public void incrementerTicketsCreated(int count) {
        this.totalTicketsCreated = (this.totalTicketsCreated != null ? this.totalTicketsCreated : 0) + count;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Update ticket resolution metrics
     */
    public void incrementerTicketsResolus(int count) {
        this.totalTicketsResolved = (this.totalTicketsResolved != null ? this.totalTicketsResolved : 0) + count;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Update SLA compliance metrics
     */
    public void mettreAJourSLA(int ticketsRespectantSLA, int ticketsEnRetard) {
        this.ticketsWithinSla = (this.ticketsWithinSla != null ? this.ticketsWithinSla : 0) + ticketsRespectantSLA;
        this.ticketsBreachedSla = (this.ticketsBreachedSla != null ? this.ticketsBreachedSla : 0) + ticketsEnRetard;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Update assignment metrics
     */
    public void mettreAJourAssignations(int assignations, int reassignations, BigDecimal confidenceMoyenne) {
        this.totalAssignments = (this.totalAssignments != null ? this.totalAssignments : 0) + assignations;
        this.totalReassignments = (this.totalReassignments != null ? this.totalReassignments : 0) + reassignations;
        this.averageAssignmentConfidence = confidenceMoyenne;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Update satisfaction metrics
     */
    public void mettreAJourSatisfaction(int reponses, BigDecimal scoreMoyen) {
        this.totalSatisfactionResponses = (this.totalSatisfactionResponses != null ? this.totalSatisfactionResponses : 0) + reponses;
        this.averageSatisfactionScore = scoreMoyen;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Calculate SLA compliance rate
     */
    public BigDecimal getTauxConformiteSLA() {
        int totalSLA = (ticketsWithinSla != null ? ticketsWithinSla : 0) + 
                      (ticketsBreachedSla != null ? ticketsBreachedSla : 0);
        if (totalSLA == 0) return BigDecimal.ZERO;
        
        return BigDecimal.valueOf(ticketsWithinSla)
                .divide(BigDecimal.valueOf(totalSLA), 4, BigDecimal.ROUND_HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }
    
    /**
     * Calculate resolution rate
     */
    public BigDecimal getTauxResolution() {
        if (totalTicketsCreated == null || totalTicketsCreated == 0) return BigDecimal.ZERO;
        
        int resolved = totalTicketsResolved != null ? totalTicketsResolved : 0;
        return BigDecimal.valueOf(resolved)
                .divide(BigDecimal.valueOf(totalTicketsCreated), 4, BigDecimal.ROUND_HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }
    
    /**
     * Calculate reassignment rate
     */
    public BigDecimal getTauxReassignation() {
        if (totalAssignments == null || totalAssignments == 0) return BigDecimal.ZERO;
        
        int reassignments = totalReassignments != null ? totalReassignments : 0;
        return BigDecimal.valueOf(reassignments)
                .divide(BigDecimal.valueOf(totalAssignments), 4, BigDecimal.ROUND_HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }
}
