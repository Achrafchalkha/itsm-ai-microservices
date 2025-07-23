package com.itsm.analytics.infrastructure.persistence.mapper;

import com.itsm.analytics.domain.model.TechnicianPerformanceMetrics;
import com.itsm.analytics.infrastructure.persistence.entity.TechnicianPerformanceMetricsEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper between Technician Performance Metrics domain model and JPA entity
 */
@Component
public class TechnicianPerformanceMetricsMapper {
    
    /**
     * Convert domain model to JPA entity
     */
    public TechnicianPerformanceMetricsEntity toEntity(TechnicianPerformanceMetrics domain) {
        if (domain == null) {
            return null;
        }
        
        return TechnicianPerformanceMetricsEntity.builder()
                .id(domain.getId())
                .technicianId(domain.getTechnicianId())
                .teamId(domain.getTeamId())
                .dateDebut(domain.getDateDebut())
                .dateFin(domain.getDateFin())
                .periodeType(domain.getPeriodeType().name())
                .totalTicketsAssigned(domain.getTotalTicketsAssigned())
                .totalTicketsResolved(domain.getTotalTicketsResolved())
                .totalTicketsClosed(domain.getTotalTicketsClosed())
                .averageResolutionTimeMinutes(domain.getAverageResolutionTimeMinutes())
                .averageFirstResponseTimeMinutes(domain.getAverageFirstResponseTimeMinutes())
                .ticketsWithinSla(domain.getTicketsWithinSla())
                .ticketsBreachedSla(domain.getTicketsBreachedSla())
                .slaComplianceRate(domain.getSlaComplianceRate())
                .totalAssignmentsReceived(domain.getTotalAssignmentsReceived())
                .totalReassignmentsFrom(domain.getTotalReassignmentsFrom())
                .totalReassignmentsTo(domain.getTotalReassignmentsTo())
                .reassignmentRate(domain.getReassignmentRate())
                .totalSatisfactionResponses(domain.getTotalSatisfactionResponses())
                .averageSatisfactionScore(domain.getAverageSatisfactionScore())
                .positiveFeedbackCount(domain.getPositiveFeedbackCount())
                .negativeFeedbackCount(domain.getNegativeFeedbackCount())
                .currentWorkload(domain.getCurrentWorkload())
                .averageWorkload(domain.getAverageWorkload())
                .maxWorkload(domain.getMaxWorkload())
                .workloadEfficiency(domain.getWorkloadEfficiency())
                .activeDays(domain.getActiveDays())
                .totalDays(domain.getTotalDays())
                .activityRate(domain.getActivityRate())
                .aiAssignmentsReceived(domain.getAiAssignmentsReceived())
                .averageConfidenceScore(domain.getAverageConfidenceScore())
                .performanceLevel(domain.getPerformanceLevel() != null ? domain.getPerformanceLevel().name() : null)
                .teamRanking(domain.getTeamRanking())
                .performanceScore(domain.getPerformanceScore())
                .categoryBreakdownJson(domain.getCategoryBreakdownJson())
                .priorityBreakdownJson(domain.getPriorityBreakdownJson())
                .competenceUtilizationJson(domain.getCompetenceUtilizationJson())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }
    
    /**
     * Convert JPA entity to domain model
     */
    public TechnicianPerformanceMetrics toDomain(TechnicianPerformanceMetricsEntity entity) {
        if (entity == null) {
            return null;
        }
        
        return TechnicianPerformanceMetrics.builder()
                .id(entity.getId())
                .technicianId(entity.getTechnicianId())
                .teamId(entity.getTeamId())
                .dateDebut(entity.getDateDebut())
                .dateFin(entity.getDateFin())
                .periodeType(TechnicianPerformanceMetrics.PeriodeType.valueOf(entity.getPeriodeType()))
                .totalTicketsAssigned(entity.getTotalTicketsAssigned())
                .totalTicketsResolved(entity.getTotalTicketsResolved())
                .totalTicketsClosed(entity.getTotalTicketsClosed())
                .averageResolutionTimeMinutes(entity.getAverageResolutionTimeMinutes())
                .averageFirstResponseTimeMinutes(entity.getAverageFirstResponseTimeMinutes())
                .ticketsWithinSla(entity.getTicketsWithinSla())
                .ticketsBreachedSla(entity.getTicketsBreachedSla())
                .slaComplianceRate(entity.getSlaComplianceRate())
                .totalAssignmentsReceived(entity.getTotalAssignmentsReceived())
                .totalReassignmentsFrom(entity.getTotalReassignmentsFrom())
                .totalReassignmentsTo(entity.getTotalReassignmentsTo())
                .reassignmentRate(entity.getReassignmentRate())
                .totalSatisfactionResponses(entity.getTotalSatisfactionResponses())
                .averageSatisfactionScore(entity.getAverageSatisfactionScore())
                .positiveFeedbackCount(entity.getPositiveFeedbackCount())
                .negativeFeedbackCount(entity.getNegativeFeedbackCount())
                .currentWorkload(entity.getCurrentWorkload())
                .averageWorkload(entity.getAverageWorkload())
                .maxWorkload(entity.getMaxWorkload())
                .workloadEfficiency(entity.getWorkloadEfficiency())
                .activeDays(entity.getActiveDays())
                .totalDays(entity.getTotalDays())
                .activityRate(entity.getActivityRate())
                .aiAssignmentsReceived(entity.getAiAssignmentsReceived())
                .averageConfidenceScore(entity.getAverageConfidenceScore())
                .performanceLevel(entity.getPerformanceLevel() != null ? 
                    TechnicianPerformanceMetrics.PerformanceLevel.valueOf(entity.getPerformanceLevel()) : null)
                .teamRanking(entity.getTeamRanking())
                .performanceScore(entity.getPerformanceScore())
                .categoryBreakdownJson(entity.getCategoryBreakdownJson())
                .priorityBreakdownJson(entity.getPriorityBreakdownJson())
                .competenceUtilizationJson(entity.getCompetenceUtilizationJson())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
