package com.itsm.analytics.presentation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * DTO for MANAGER dashboard response
 * Contains team-specific analytics and KPIs
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ManagerDashboardDTO {
    
    private UUID teamId;
    private String teamName;
    private TeamKPIDTO teamKPIs;
    private List<TechnicianPerformanceDTO> technicianPerformance;
    private Map<String, Integer> ticketDistribution;
    private WorkloadOverviewDTO workloadOverview;
    private List<SLAAlertDTO> slaAlerts;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private LocalDateTime generatedAt;
}
