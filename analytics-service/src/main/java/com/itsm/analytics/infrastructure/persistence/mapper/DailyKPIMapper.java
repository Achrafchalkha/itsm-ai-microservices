package com.itsm.analytics.infrastructure.persistence.mapper;

import com.itsm.analytics.domain.model.DailyKPI;
import com.itsm.analytics.infrastructure.persistence.entity.DailyKPIEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper between Daily KPI domain model and JPA entity
 */
@Component
public class DailyKPIMapper {
    
    /**
     * Convert domain model to JPA entity
     */
    public DailyKPIEntity toEntity(DailyKPI domain) {
        if (domain == null) {
            return null;
        }
        
        return DailyKPIEntity.builder()
                .id(domain.getId())
                .dateKpi(domain.getDateKpi())
                .totalTicketsCreated(domain.getTotalTicketsCreated())
                .totalTicketsResolved(domain.getTotalTicketsResolved())
                .totalTicketsClosed(domain.getTotalTicketsClosed())
                .ticketsWithinSla(domain.getTicketsWithinSla())
                .ticketsBreachedSla(domain.getTicketsBreachedSla())
                .averageResolutionTimeMinutes(domain.getAverageResolutionTimeMinutes())
                .averageFirstResponseTimeMinutes(domain.getAverageFirstResponseTimeMinutes())
                .totalAssignments(domain.getTotalAssignments())
                .totalReassignments(domain.getTotalReassignments())
                .averageAssignmentConfidence(domain.getAverageAssignmentConfidence())
                .totalSatisfactionResponses(domain.getTotalSatisfactionResponses())
                .averageSatisfactionScore(domain.getAverageSatisfactionScore())
                .teamMetricsJson(domain.getTeamMetricsJson())
                .technicianMetricsJson(domain.getTechnicianMetricsJson())
                .categoryMetricsJson(domain.getCategoryMetricsJson())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }
    
    /**
     * Convert JPA entity to domain model
     */
    public DailyKPI toDomain(DailyKPIEntity entity) {
        if (entity == null) {
            return null;
        }
        
        return DailyKPI.builder()
                .id(entity.getId())
                .dateKpi(entity.getDateKpi())
                .totalTicketsCreated(entity.getTotalTicketsCreated())
                .totalTicketsResolved(entity.getTotalTicketsResolved())
                .totalTicketsClosed(entity.getTotalTicketsClosed())
                .ticketsWithinSla(entity.getTicketsWithinSla())
                .ticketsBreachedSla(entity.getTicketsBreachedSla())
                .averageResolutionTimeMinutes(entity.getAverageResolutionTimeMinutes())
                .averageFirstResponseTimeMinutes(entity.getAverageFirstResponseTimeMinutes())
                .totalAssignments(entity.getTotalAssignments())
                .totalReassignments(entity.getTotalReassignments())
                .averageAssignmentConfidence(entity.getAverageAssignmentConfidence())
                .totalSatisfactionResponses(entity.getTotalSatisfactionResponses())
                .averageSatisfactionScore(entity.getAverageSatisfactionScore())
                .teamMetricsJson(entity.getTeamMetricsJson())
                .technicianMetricsJson(entity.getTechnicianMetricsJson())
                .categoryMetricsJson(entity.getCategoryMetricsJson())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
