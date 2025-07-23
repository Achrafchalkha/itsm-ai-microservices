package com.itsm.analytics.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain model for Technician Performance Metrics
 * Represents performance analytics for individual technicians
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TechnicianPerformanceMetrics {
    
    private UUID id;
    private UUID technicianId;
    private UUID teamId;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private PeriodeType periodeType;
    
    // Volume metrics
    @Builder.Default
    private Integer totalTicketsAssigned = 0;
    @Builder.Default
    private Integer totalTicketsResolved = 0;
    @Builder.Default
    private Integer totalTicketsClosed = 0;
    
    // Performance metrics
    private BigDecimal averageResolutionTimeMinutes;
    private BigDecimal averageFirstResponseTimeMinutes;
    
    // SLA metrics
    @Builder.Default
    private Integer ticketsWithinSla = 0;
    @Builder.Default
    private Integer ticketsBreachedSla = 0;
    private BigDecimal slaComplianceRate;
    
    // Assignment metrics
    @Builder.Default
    private Integer totalAssignmentsReceived = 0;
    @Builder.Default
    private Integer totalReassignmentsFrom = 0;
    @Builder.Default
    private Integer totalReassignmentsTo = 0;
    private BigDecimal reassignmentRate;
    
    // Satisfaction metrics
    @Builder.Default
    private Integer totalSatisfactionResponses = 0;
    private BigDecimal averageSatisfactionScore;
    @Builder.Default
    private Integer positiveFeedbackCount = 0;
    @Builder.Default
    private Integer negativeFeedbackCount = 0;
    
    // Workload metrics
    @Builder.Default
    private Integer currentWorkload = 0;
    private BigDecimal averageWorkload;
    private Integer maxWorkload;
    private BigDecimal workloadEfficiency;
    
    // Activity metrics
    @Builder.Default
    private Integer activeDays = 0;
    @Builder.Default
    private Integer totalDays = 0;
    private BigDecimal activityRate;
    
    // AI assignment metrics
    @Builder.Default
    private Integer aiAssignmentsReceived = 0;
    private BigDecimal averageConfidenceScore;
    
    // Performance level and ranking
    private PerformanceLevel performanceLevel;
    private Integer teamRanking;
    private BigDecimal performanceScore;
    
    // Detailed metrics (JSON)
    private String categoryBreakdownJson;
    private String priorityBreakdownJson;
    private String competenceUtilizationJson;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Enums
    public enum PeriodeType {
        DAILY, WEEKLY, MONTHLY, QUARTERLY, YEARLY
    }
    
    public enum PerformanceLevel {
        EXCELLENT, BON, MOYEN, FAIBLE
    }
    
    // Factory method
    public static TechnicianPerformanceMetrics creerMetriques(UUID technicianId, UUID teamId, 
                                                             LocalDate dateDebut, LocalDate dateFin, 
                                                             PeriodeType periodeType) {
        return TechnicianPerformanceMetrics.builder()
                .id(UUID.randomUUID())
                .technicianId(technicianId)
                .teamId(teamId)
                .dateDebut(dateDebut)
                .dateFin(dateFin)
                .periodeType(periodeType)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
    
    // Business methods
    
    /**
     * Calculate SLA compliance rate
     */
    public void calculerTauxConformiteSLA() {
        int totalTicketsWithSLA = ticketsWithinSla + ticketsBreachedSla;
        if (totalTicketsWithSLA > 0) {
            this.slaComplianceRate = BigDecimal.valueOf(ticketsWithinSla)
                    .divide(BigDecimal.valueOf(totalTicketsWithSLA), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        } else {
            this.slaComplianceRate = BigDecimal.ZERO;
        }
    }
    
    /**
     * Calculate reassignment rate
     */
    public void calculerTauxReassignation() {
        if (totalAssignmentsReceived > 0) {
            this.reassignmentRate = BigDecimal.valueOf(totalReassignmentsFrom)
                    .divide(BigDecimal.valueOf(totalAssignmentsReceived), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        } else {
            this.reassignmentRate = BigDecimal.ZERO;
        }
    }
    
    /**
     * Calculate activity rate
     */
    public void calculerTauxActivite() {
        if (totalDays > 0) {
            this.activityRate = BigDecimal.valueOf(activeDays)
                    .divide(BigDecimal.valueOf(totalDays), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        } else {
            this.activityRate = BigDecimal.ZERO;
        }
    }
    
    /**
     * Calculate workload efficiency
     */
    public void calculerEfficaciteCharge() {
        if (averageWorkload != null && averageWorkload.compareTo(BigDecimal.ZERO) > 0 && totalTicketsResolved > 0) {
            this.workloadEfficiency = BigDecimal.valueOf(totalTicketsResolved)
                    .divide(averageWorkload, 4, RoundingMode.HALF_UP);
        } else {
            this.workloadEfficiency = BigDecimal.ZERO;
        }
    }
    
    /**
     * Calculate overall performance score
     */
    public void calculerScorePerformance() {
        BigDecimal score = BigDecimal.ZERO;
        int factorCount = 0;
        
        // SLA compliance (30% weight)
        if (slaComplianceRate != null) {
            score = score.add(slaComplianceRate.multiply(BigDecimal.valueOf(0.3)));
            factorCount++;
        }
        
        // Satisfaction score (25% weight)
        if (averageSatisfactionScore != null) {
            BigDecimal satisfactionPercent = averageSatisfactionScore
                    .divide(BigDecimal.valueOf(5), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
            score = score.add(satisfactionPercent.multiply(BigDecimal.valueOf(0.25)));
            factorCount++;
        }
        
        // Resolution efficiency (20% weight) - inverse of resolution time
        if (averageResolutionTimeMinutes != null && averageResolutionTimeMinutes.compareTo(BigDecimal.ZERO) > 0) {
            // Normalize resolution time (lower is better)
            BigDecimal maxReasonableTime = BigDecimal.valueOf(480); // 8 hours
            BigDecimal efficiency = maxReasonableTime.subtract(averageResolutionTimeMinutes.min(maxReasonableTime))
                    .divide(maxReasonableTime, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
            score = score.add(efficiency.multiply(BigDecimal.valueOf(0.2)));
            factorCount++;
        }
        
        // Activity rate (15% weight)
        if (activityRate != null) {
            score = score.add(activityRate.multiply(BigDecimal.valueOf(0.15)));
            factorCount++;
        }
        
        // Low reassignment rate (10% weight) - inverse of reassignment rate
        if (reassignmentRate != null) {
            BigDecimal reassignmentScore = BigDecimal.valueOf(100).subtract(reassignmentRate);
            score = score.add(reassignmentScore.multiply(BigDecimal.valueOf(0.1)));
            factorCount++;
        }
        
        if (factorCount > 0) {
            this.performanceScore = score.setScale(2, RoundingMode.HALF_UP);
        } else {
            this.performanceScore = BigDecimal.ZERO;
        }
    }
    
    /**
     * Determine performance level based on performance score
     */
    public void determinerNiveauPerformance() {
        if (performanceScore == null) {
            calculerScorePerformance();
        }
        
        if (performanceScore.compareTo(BigDecimal.valueOf(85)) >= 0) {
            this.performanceLevel = PerformanceLevel.EXCELLENT;
        } else if (performanceScore.compareTo(BigDecimal.valueOf(70)) >= 0) {
            this.performanceLevel = PerformanceLevel.BON;
        } else if (performanceScore.compareTo(BigDecimal.valueOf(50)) >= 0) {
            this.performanceLevel = PerformanceLevel.MOYEN;
        } else {
            this.performanceLevel = PerformanceLevel.FAIBLE;
        }
    }
    
    /**
     * Update metrics with new ticket data
     */
    public void mettreAJourAvecTicket(boolean resolu, boolean ferme, boolean slaRespecte, 
                                     BigDecimal tempsResolutionMinutes, BigDecimal tempsPremiereReponseMinutes) {
        this.totalTicketsAssigned++;
        
        if (resolu) {
            this.totalTicketsResolved++;
            
            if (tempsResolutionMinutes != null) {
                if (this.averageResolutionTimeMinutes == null) {
                    this.averageResolutionTimeMinutes = tempsResolutionMinutes;
                } else {
                    // Calculate running average
                    BigDecimal total = this.averageResolutionTimeMinutes.multiply(BigDecimal.valueOf(totalTicketsResolved - 1));
                    total = total.add(tempsResolutionMinutes);
                    this.averageResolutionTimeMinutes = total.divide(BigDecimal.valueOf(totalTicketsResolved), 2, RoundingMode.HALF_UP);
                }
            }
        }
        
        if (ferme) {
            this.totalTicketsClosed++;
        }
        
        if (slaRespecte) {
            this.ticketsWithinSla++;
        } else {
            this.ticketsBreachedSla++;
        }
        
        if (tempsPremiereReponseMinutes != null) {
            if (this.averageFirstResponseTimeMinutes == null) {
                this.averageFirstResponseTimeMinutes = tempsPremiereReponseMinutes;
            } else {
                // Calculate running average
                BigDecimal total = this.averageFirstResponseTimeMinutes.multiply(BigDecimal.valueOf(totalTicketsAssigned - 1));
                total = total.add(tempsPremiereReponseMinutes);
                this.averageFirstResponseTimeMinutes = total.divide(BigDecimal.valueOf(totalTicketsAssigned), 2, RoundingMode.HALF_UP);
            }
        }
        
        // Recalculate derived metrics
        calculerTauxConformiteSLA();
        calculerTauxReassignation();
        calculerEfficaciteCharge();
        calculerScorePerformance();
        determinerNiveauPerformance();
        
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Update satisfaction metrics
     */
    public void mettreAJourSatisfaction(int score) {
        this.totalSatisfactionResponses++;
        
        if (score >= 4) {
            this.positiveFeedbackCount++;
        } else if (score <= 2) {
            this.negativeFeedbackCount++;
        }
        
        if (this.averageSatisfactionScore == null) {
            this.averageSatisfactionScore = BigDecimal.valueOf(score);
        } else {
            // Calculate running average
            BigDecimal total = this.averageSatisfactionScore.multiply(BigDecimal.valueOf(totalSatisfactionResponses - 1));
            total = total.add(BigDecimal.valueOf(score));
            this.averageSatisfactionScore = total.divide(BigDecimal.valueOf(totalSatisfactionResponses), 2, RoundingMode.HALF_UP);
        }
        
        // Recalculate performance score
        calculerScorePerformance();
        determinerNiveauPerformance();
        
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Update assignment metrics
     */
    public void mettreAJourAssignation(boolean reassignationFrom, boolean reassignationTo, BigDecimal confidenceScore) {
        if (!reassignationFrom && !reassignationTo) {
            this.totalAssignmentsReceived++;
        }
        
        if (reassignationFrom) {
            this.totalReassignmentsFrom++;
        }
        
        if (reassignationTo) {
            this.totalReassignmentsTo++;
        }
        
        if (confidenceScore != null) {
            this.aiAssignmentsReceived++;
            
            if (this.averageConfidenceScore == null) {
                this.averageConfidenceScore = confidenceScore;
            } else {
                // Calculate running average
                BigDecimal total = this.averageConfidenceScore.multiply(BigDecimal.valueOf(aiAssignmentsReceived - 1));
                total = total.add(confidenceScore);
                this.averageConfidenceScore = total.divide(BigDecimal.valueOf(aiAssignmentsReceived), 2, RoundingMode.HALF_UP);
            }
        }
        
        // Recalculate derived metrics
        calculerTauxReassignation();
        calculerScorePerformance();
        determinerNiveauPerformance();
        
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Update workload metrics
     */
    public void mettreAJourCharge(int nouvelleCharge) {
        this.currentWorkload = nouvelleCharge;
        
        if (this.maxWorkload == null || nouvelleCharge > this.maxWorkload) {
            this.maxWorkload = nouvelleCharge;
        }
        
        // Update average workload (this would typically be calculated over time)
        if (this.averageWorkload == null) {
            this.averageWorkload = BigDecimal.valueOf(nouvelleCharge);
        }
        
        calculerEfficaciteCharge();
        calculerScorePerformance();
        determinerNiveauPerformance();
        
        this.updatedAt = LocalDateTime.now();
    }
}
