package com.itsm.analytics.presentation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for global KPI metrics
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GlobalKPIDTO {
    
    private Integer totalTickets;
    private Integer ticketsResolved;
    private BigDecimal resolutionRate;
    private BigDecimal slaComplianceRate;
    private BigDecimal averageSatisfactionScore;
}
