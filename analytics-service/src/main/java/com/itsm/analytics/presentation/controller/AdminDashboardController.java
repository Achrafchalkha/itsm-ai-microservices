package com.itsm.analytics.presentation.controller;

import com.itsm.analytics.application.service.KPICalculationEngine;
import com.itsm.analytics.application.service.SLAConfigurationService;
import com.itsm.analytics.infrastructure.client.TicketServiceClient;
import com.itsm.analytics.infrastructure.client.UserServiceClient;
import com.itsm.analytics.presentation.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * REST Controller for ADMIN dashboard
 * Provides global supervision and KPIs for administrators
 */
@RestController
@RequestMapping("/api/analytics/admin")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class AdminDashboardController {
    
    private final KPICalculationEngine kpiCalculationEngine;
    private final SLAConfigurationService slaConfigurationService;
    private final TicketServiceClient ticketServiceClient;
    private final UserServiceClient userServiceClient;
    
    /**
     * Get complete admin dashboard data
     */
    @GetMapping("/dashboard")
    public ResponseEntity<AdminDashboardDTO> getAdminDashboard(
            @RequestParam(defaultValue = "30") int days) {
        
        log.info("Getting admin dashboard for last {} days", days);
        
        try {
            LocalDate endDate = LocalDate.now();
            LocalDate startDate = endDate.minusDays(days);
            
            // Calculate global KPIs
            KPICalculationEngine.GlobalKPIResult globalKPIs = kpiCalculationEngine.calculateGlobalKPIs(startDate, endDate);
            
            // Get SLA overview
            SLAOverviewDTO slaOverview = getSLAOverview();
            
            // Get team performance overview
            List<TeamPerformanceOverviewDTO> teamPerformance = getTeamPerformanceOverview(startDate, endDate);
            
            // Get tickets requiring attention
            List<TicketServiceClient.TicketDTO> criticalTickets = ticketServiceClient.getTicketsBreachedSLA();
            List<TicketServiceClient.TicketDTO> approachingTickets = ticketServiceClient.getTicketsApproachingSLA(4);
            
            AdminDashboardDTO dashboard = AdminDashboardDTO.builder()
                    .globalKPIs(convertToGlobalKPIDTO(globalKPIs))
                    .slaOverview(slaOverview)
                    .teamPerformance(teamPerformance)
                    .criticalTicketsCount(criticalTickets.size())
                    .approachingSLATicketsCount(approachingTickets.size())
                    .periodStart(startDate)
                    .periodEnd(endDate)
                    .generatedAt(LocalDateTime.now())
                    .build();
            
            return ResponseEntity.ok(dashboard);
            
        } catch (Exception e) {
            log.error("Error getting admin dashboard: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get global KPIs for a specific period
     */
    @GetMapping("/kpis/global")
    public ResponseEntity<GlobalKPIDTO> getGlobalKPIs(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        log.info("Getting global KPIs from {} to {}", startDate, endDate);
        
        try {
            KPICalculationEngine.GlobalKPIResult result = kpiCalculationEngine.calculateGlobalKPIs(startDate, endDate);
            GlobalKPIDTO kpiDTO = convertToGlobalKPIDTO(result);
            
            return ResponseEntity.ok(kpiDTO);
            
        } catch (Exception e) {
            log.error("Error getting global KPIs: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get all tickets for supervision
     */
    @GetMapping("/tickets/all")
    public ResponseEntity<List<TicketServiceClient.TicketDTO>> getAllTickets(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) String category) {
        
        log.info("Getting all tickets for admin supervision - page: {}, size: {}", page, size);
        
        try {
            // This would need to be implemented in ticket-service
            // For now, get tickets from last 30 days
            LocalDateTime endDate = LocalDateTime.now();
            LocalDateTime startDate = endDate.minusDays(30);
            
            List<TicketServiceClient.TicketDTO> tickets = ticketServiceClient.getTicketsByPeriod(startDate, endDate);
            
            // Apply filters if provided
            if (status != null) {
                tickets = tickets.stream()
                        .filter(t -> status.equals(t.getStatut()))
                        .collect(Collectors.toList());
            }
            
            if (priority != null) {
                tickets = tickets.stream()
                        .filter(t -> priority.equals(t.getPriorite()))
                        .collect(Collectors.toList());
            }
            
            if (category != null) {
                tickets = tickets.stream()
                        .filter(t -> category.equals(t.getCategorie()))
                        .collect(Collectors.toList());
            }
            
            // Apply pagination
            int start = page * size;
            int end = Math.min(start + size, tickets.size());
            
            if (start >= tickets.size()) {
                return ResponseEntity.ok(List.of());
            }
            
            List<TicketServiceClient.TicketDTO> paginatedTickets = tickets.subList(start, end);
            
            return ResponseEntity.ok(paginatedTickets);
            
        } catch (Exception e) {
            log.error("Error getting all tickets: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get tickets that breached SLA
     */
    @GetMapping("/tickets/sla-breached")
    public ResponseEntity<List<TicketServiceClient.TicketDTO>> getSLABreachedTickets() {
        
        log.info("Getting SLA breached tickets");
        
        try {
            List<TicketServiceClient.TicketDTO> tickets = ticketServiceClient.getTicketsBreachedSLA();
            return ResponseEntity.ok(tickets);
            
        } catch (Exception e) {
            log.error("Error getting SLA breached tickets: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get tickets approaching SLA deadline
     */
    @GetMapping("/tickets/sla-approaching")
    public ResponseEntity<List<TicketServiceClient.TicketDTO>> getSLAApproachingTickets(
            @RequestParam(defaultValue = "4") int hoursBeforeDeadline) {
        
        log.info("Getting tickets approaching SLA deadline within {} hours", hoursBeforeDeadline);
        
        try {
            List<TicketServiceClient.TicketDTO> tickets = ticketServiceClient.getTicketsApproachingSLA(hoursBeforeDeadline);
            return ResponseEntity.ok(tickets);
            
        } catch (Exception e) {
            log.error("Error getting tickets approaching SLA: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get volume statistics by period
     */
    @GetMapping("/stats/volume")
    public ResponseEntity<Map<String, Object>> getVolumeStatistics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "daily") String groupBy) {
        
        log.info("Getting volume statistics from {} to {} grouped by {}", startDate, endDate, groupBy);
        
        try {
            // This would calculate volume statistics grouped by day/week/month
            // For now, return basic statistics
            LocalDateTime startDateTime = startDate.atStartOfDay();
            LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
            
            List<TicketServiceClient.TicketDTO> tickets = ticketServiceClient.getTicketsByPeriod(startDateTime, endDateTime);
            
            Map<String, Object> stats = Map.of(
                    "totalTickets", tickets.size(),
                    "resolvedTickets", tickets.stream().filter(t -> "RESOLU".equals(t.getStatut()) || "FERME".equals(t.getStatut())).count(),
                    "openTickets", tickets.stream().filter(t -> "OUVERT".equals(t.getStatut()) || "EN_COURS".equals(t.getStatut())).count(),
                    "period", Map.of("start", startDate, "end", endDate),
                    "groupBy", groupBy
            );
            
            return ResponseEntity.ok(stats);
            
        } catch (Exception e) {
            log.error("Error getting volume statistics: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get satisfaction statistics
     */
    @GetMapping("/stats/satisfaction")
    public ResponseEntity<Map<String, Object>> getSatisfactionStatistics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        log.info("Getting satisfaction statistics from {} to {}", startDate, endDate);
        
        try {
            // This would calculate satisfaction statistics from satisfaction_scores table
            // For now, return placeholder data
            Map<String, Object> stats = Map.of(
                    "averageScore", 4.2,
                    "totalResponses", 150,
                    "responseRate", 75.5,
                    "distribution", Map.of(
                            "5", 45,
                            "4", 60,
                            "3", 30,
                            "2", 10,
                            "1", 5
                    ),
                    "period", Map.of("start", startDate, "end", endDate)
            );
            
            return ResponseEntity.ok(stats);
            
        } catch (Exception e) {
            log.error("Error getting satisfaction statistics: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    // Private helper methods
    
    private SLAOverviewDTO getSLAOverview() {
        List<TicketServiceClient.TicketDTO> breachedTickets = ticketServiceClient.getTicketsBreachedSLA();
        List<TicketServiceClient.TicketDTO> approachingTickets = ticketServiceClient.getTicketsApproachingSLA(4);
        
        return SLAOverviewDTO.builder()
                .breachedTicketsCount(breachedTickets.size())
                .approachingDeadlineCount(approachingTickets.size())
                .totalActiveTickets(breachedTickets.size() + approachingTickets.size()) // Simplified
                .complianceRate(BigDecimal.valueOf(85.5)) // This would be calculated
                .build();
    }
    
    private List<TeamPerformanceOverviewDTO> getTeamPerformanceOverview(LocalDate startDate, LocalDate endDate) {
        List<UserServiceClient.TeamDTO> teams = userServiceClient.getAllTeams();
        
        return teams.stream()
                .map(team -> {
                    // Calculate basic metrics for each team
                    LocalDateTime startDateTime = startDate.atStartOfDay();
                    LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
                    
                    List<TicketServiceClient.TicketDTO> teamTickets = ticketServiceClient.getTicketsByTeamAndPeriod(
                            team.getId(), startDateTime, endDateTime);
                    
                    int resolved = (int) teamTickets.stream()
                            .filter(t -> "RESOLU".equals(t.getStatut()) || "FERME".equals(t.getStatut()))
                            .count();
                    
                    BigDecimal resolutionRate = teamTickets.isEmpty() ? BigDecimal.ZERO :
                            BigDecimal.valueOf(resolved)
                                    .divide(BigDecimal.valueOf(teamTickets.size()), 2, RoundingMode.HALF_UP)
                                    .multiply(BigDecimal.valueOf(100));
                    
                    return TeamPerformanceOverviewDTO.builder()
                            .teamId(team.getId())
                            .teamName(team.getNom())
                            .totalTickets(teamTickets.size())
                            .resolvedTickets(resolved)
                            .resolutionRate(resolutionRate)
                            .averageSatisfaction(BigDecimal.valueOf(4.0)) // Placeholder
                            .slaComplianceRate(BigDecimal.valueOf(80.0)) // Placeholder
                            .build();
                })
                .collect(Collectors.toList());
    }
    
    private GlobalKPIDTO convertToGlobalKPIDTO(KPICalculationEngine.GlobalKPIResult result) {
        BigDecimal resolutionRate = result.getTotalTickets() > 0 ?
                BigDecimal.valueOf(result.getTicketsResolved())
                        .divide(BigDecimal.valueOf(result.getTotalTickets()), 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100)) :
                BigDecimal.ZERO;
        
        return GlobalKPIDTO.builder()
                .totalTickets(result.getTotalTickets())
                .ticketsResolved(result.getTicketsResolved())
                .resolutionRate(resolutionRate)
                .slaComplianceRate(result.getSlaComplianceRate() != null ? result.getSlaComplianceRate() : BigDecimal.ZERO)
                .averageSatisfactionScore(result.getAverageSatisfactionScore() != null ? result.getAverageSatisfactionScore() : BigDecimal.ZERO)
                .build();
    }
}
