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
 * Domain model for Team Performance Metrics
 * Used for MANAGER dashboards and team analytics
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamPerformanceMetrics {
    
    private UUID id;
    private UUID teamId;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    
    // Ticket volume
    private Integer ticketsAssigned;
    private Integer ticketsResolved;
    private Integer ticketsInProgress;
    
    // SLA performance
    private BigDecimal slaComplianceRate;
    private BigDecimal averageResolutionTimeMinutes;
    private BigDecimal averageFirstResponseTimeMinutes;
    
    // Workload distribution
    private Integer totalWorkload;
    private BigDecimal averageWorkloadPerTechnician;
    private Integer maxWorkloadTechnician;
    private Integer minWorkloadTechnician;
    
    // Satisfaction
    private BigDecimal averageSatisfactionScore;
    private Integer totalSatisfactionResponses;
    
    // Reassignment rate
    private BigDecimal reassignmentRate;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    /**
     * Factory method to create team performance metrics for a period
     */
    public static TeamPerformanceMetrics creerMetriquesEquipe(UUID teamId, LocalDate dateDebut, LocalDate dateFin) {
        return TeamPerformanceMetrics.builder()
                .id(UUID.randomUUID())
                .teamId(teamId)
                .dateDebut(dateDebut)
                .dateFin(dateFin)
                .ticketsAssigned(0)
                .ticketsResolved(0)
                .ticketsInProgress(0)
                .totalWorkload(0)
                .totalSatisfactionResponses(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
    
    /**
     * Update ticket metrics
     */
    public void mettreAJourTickets(int assigned, int resolved, int inProgress) {
        this.ticketsAssigned = assigned;
        this.ticketsResolved = resolved;
        this.ticketsInProgress = inProgress;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Update SLA metrics
     */
    public void mettreAJourSLA(BigDecimal complianceRate, BigDecimal avgResolutionTime, BigDecimal avgFirstResponseTime) {
        this.slaComplianceRate = complianceRate;
        this.averageResolutionTimeMinutes = avgResolutionTime;
        this.averageFirstResponseTimeMinutes = avgFirstResponseTime;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Update workload metrics
     */
    public void mettreAJourCharge(int totalWorkload, BigDecimal avgWorkload, int maxWorkload, int minWorkload) {
        this.totalWorkload = totalWorkload;
        this.averageWorkloadPerTechnician = avgWorkload;
        this.maxWorkloadTechnician = maxWorkload;
        this.minWorkloadTechnician = minWorkload;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Update satisfaction metrics
     */
    public void mettreAJourSatisfaction(BigDecimal avgScore, int totalResponses) {
        this.averageSatisfactionScore = avgScore;
        this.totalSatisfactionResponses = totalResponses;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Update reassignment rate
     */
    public void mettreAJourReassignation(BigDecimal rate) {
        this.reassignmentRate = rate;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Calculate resolution rate
     */
    public BigDecimal getTauxResolution() {
        if (ticketsAssigned == null || ticketsAssigned == 0) return BigDecimal.ZERO;
        
        int resolved = ticketsResolved != null ? ticketsResolved : 0;
        return BigDecimal.valueOf(resolved)
                .divide(BigDecimal.valueOf(ticketsAssigned), 4, BigDecimal.ROUND_HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }
    
    /**
     * Calculate workload balance (lower is better)
     */
    public BigDecimal getEquilibreCharge() {
        if (maxWorkloadTechnician == null || minWorkloadTechnician == null || 
            averageWorkloadPerTechnician == null || averageWorkloadPerTechnician.equals(BigDecimal.ZERO)) {
            return BigDecimal.ZERO;
        }
        
        int difference = maxWorkloadTechnician - minWorkloadTechnician;
        return BigDecimal.valueOf(difference)
                .divide(averageWorkloadPerTechnician, 4, BigDecimal.ROUND_HALF_UP);
    }
    
    /**
     * Get performance level based on multiple metrics
     */
    public PerformanceLevel getNiveauPerformance() {
        // Simple scoring based on SLA compliance and satisfaction
        BigDecimal slaScore = slaComplianceRate != null ? slaComplianceRate : BigDecimal.ZERO;
        BigDecimal satisfactionScore = averageSatisfactionScore != null ? 
            averageSatisfactionScore.multiply(BigDecimal.valueOf(20)) : BigDecimal.ZERO; // Convert 1-5 to 0-100
        
        BigDecimal overallScore = slaScore.add(satisfactionScore).divide(BigDecimal.valueOf(2), 2, BigDecimal.ROUND_HALF_UP);
        
        if (overallScore.compareTo(BigDecimal.valueOf(90)) >= 0) return PerformanceLevel.EXCELLENT;
        if (overallScore.compareTo(BigDecimal.valueOf(80)) >= 0) return PerformanceLevel.BON;
        if (overallScore.compareTo(BigDecimal.valueOf(70)) >= 0) return PerformanceLevel.MOYEN;
        if (overallScore.compareTo(BigDecimal.valueOf(60)) >= 0) return PerformanceLevel.FAIBLE;
        return PerformanceLevel.CRITIQUE;
    }
    
    /**
     * Enum for performance levels
     */
    public enum PerformanceLevel {
        EXCELLENT("Excellent"),
        BON("Bon"),
        MOYEN("Moyen"),
        FAIBLE("Faible"),
        CRITIQUE("Critique");
        
        private final String description;
        
        PerformanceLevel(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
}
