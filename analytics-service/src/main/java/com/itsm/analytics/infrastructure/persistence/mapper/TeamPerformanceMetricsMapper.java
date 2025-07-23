package com.itsm.analytics.infrastructure.persistence.mapper;

import com.itsm.analytics.domain.model.TeamPerformanceMetrics;
import com.itsm.analytics.infrastructure.persistence.entity.TeamPerformanceMetricsEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper between Team Performance Metrics domain model and JPA entity
 */
@Component
public class TeamPerformanceMetricsMapper {
    
    /**
     * Convert domain model to JPA entity
     */
    public TeamPerformanceMetricsEntity toEntity(TeamPerformanceMetrics domain) {
        if (domain == null) {
            return null;
        }
        
        return TeamPerformanceMetricsEntity.builder()
                .id(domain.getId())
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
                .totalAssignments(domain.getTotalAssignments())
                .totalReassignments(domain.getTotalReassignments())
                .reassignmentRate(domain.getReassignmentRate())
                .totalSatisfactionResponses(domain.getTotalSatisfactionResponses())
                .averageSatisfactionScore(domain.getAverageSatisfactionScore())
                .totalTechnicians(domain.getTotalTechnicians())
                .activeTechnicians(domain.getActiveTechnicians())
                .averageWorkload(domain.getAverageWorkload())
                .maxWorkload(domain.getMaxWorkload())
                .minWorkload(domain.getMinWorkload())
                .performanceLevel(domain.getPerformanceLevel() != null ? domain.getPerformanceLevel().name() : null)
                .categoryBreakdownJson(domain.getCategoryBreakdownJson())
                .priorityBreakdownJson(domain.getPriorityBreakdownJson())
                .technicianMetricsJson(domain.getTechnicianMetricsJson())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }
    
    /**
     * Convert JPA entity to domain model
     */
    public TeamPerformanceMetrics toDomain(TeamPerformanceMetricsEntity entity) {
        if (entity == null) {
            return null;
        }
        
        return TeamPerformanceMetrics.builder()
                .id(entity.getId())
                .teamId(entity.getTeamId())
                .dateDebut(entity.getDateDebut())
                .dateFin(entity.getDateFin())
                .periodeType(TeamPerformanceMetrics.PeriodeType.valueOf(entity.getPeriodeType()))
                .totalTicketsAssigned(entity.getTotalTicketsAssigned())
                .totalTicketsResolved(entity.getTotalTicketsResolved())
                .totalTicketsClosed(entity.getTotalTicketsClosed())
                .averageResolutionTimeMinutes(entity.getAverageResolutionTimeMinutes())
                .averageFirstResponseTimeMinutes(entity.getAverageFirstResponseTimeMinutes())
                .ticketsWithinSla(entity.getTicketsWithinSla())
                .ticketsBreachedSla(entity.getTicketsBreachedSla())
                .slaComplianceRate(entity.getSlaComplianceRate())
                .totalAssignments(entity.getTotalAssignments())
                .totalReassignments(entity.getTotalReassignments())
                .reassignmentRate(entity.getReassignmentRate())
                .totalSatisfactionResponses(entity.getTotalSatisfactionResponses())
                .averageSatisfactionScore(entity.getAverageSatisfactionScore())
                .totalTechnicians(entity.getTotalTechnicians())
                .activeTechnicians(entity.getActiveTechnicians())
                .averageWorkload(entity.getAverageWorkload())
                .maxWorkload(entity.getMaxWorkload())
                .minWorkload(entity.getMinWorkload())
                .performanceLevel(entity.getPerformanceLevel() != null ? 
                    TeamPerformanceMetrics.PerformanceLevel.valueOf(entity.getPerformanceLevel()) : null)
                .categoryBreakdownJson(entity.getCategoryBreakdownJson())
                .priorityBreakdownJson(entity.getPriorityBreakdownJson())
                .technicianMetricsJson(entity.getTechnicianMetricsJson())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
