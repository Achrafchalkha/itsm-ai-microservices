package com.itsm.analytics.presentation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for ADMIN dashboard response
 * Contains all data needed for admin supervision
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardDTO {
    
    private GlobalKPIDTO globalKPIs;
    private SLAOverviewDTO slaOverview;
    private List<TeamPerformanceOverviewDTO> teamPerformance;
    private Integer criticalTicketsCount;
    private Integer approachingSLATicketsCount;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private LocalDateTime generatedAt;
}
