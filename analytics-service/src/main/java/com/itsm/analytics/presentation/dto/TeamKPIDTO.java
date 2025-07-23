package com.itsm.analytics.presentation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for team KPI metrics
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamKPIDTO {
    
    private Integer totalTickets;
    private Integer resolvedTickets;
    private Integer inProgressTickets;
    private BigDecimal resolutionRate;
    private BigDecimal slaComplianceRate;
    private BigDecimal averageResolutionTime;
    private BigDecimal averageFirstResponseTime;
    private BigDecimal averageSatisfactionScore;
    private BigDecimal reassignmentRate;
    private String performanceLevel;
}
