package com.itsm.analytics.presentation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for SLA overview metrics
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SLAOverviewDTO {
    
    private Integer breachedTicketsCount;
    private Integer approachingDeadlineCount;
    private Integer totalActiveTickets;
    private BigDecimal complianceRate;
}
