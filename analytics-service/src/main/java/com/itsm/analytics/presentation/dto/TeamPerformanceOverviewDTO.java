package com.itsm.analytics.presentation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO for team performance overview
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamPerformanceOverviewDTO {
    
    private UUID teamId;
    private String teamName;
    private Integer totalTickets;
    private Integer resolvedTickets;
    private BigDecimal resolutionRate;
    private BigDecimal averageSatisfaction;
    private BigDecimal slaComplianceRate;
}
