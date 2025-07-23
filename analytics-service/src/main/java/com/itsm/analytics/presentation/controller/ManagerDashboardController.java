package com.itsm.analytics.presentation.controller;

import com.itsm.analytics.application.service.KPICalculationEngine;
import com.itsm.analytics.domain.model.TeamPerformanceMetrics;
import com.itsm.analytics.infrastructure.client.TicketServiceClient;
import com.itsm.analytics.infrastructure.client.UserServiceClient;
import com.itsm.analytics.presentation.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
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
 * REST Controller for MANAGER dashboard
 * Provides team-specific analytics and KPIs for managers
 */
@RestController
@RequestMapping("/api/analytics/manager")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('MANAGER')")
public class ManagerDashboardController {
    
    private final KPICalculationEngine kpiCalculationEngine;
    private final TicketServiceClient ticketServiceClient;
    private final UserServiceClient userServiceClient;
    
    /**
     * Get complete manager dashboard for their team
     */
    @GetMapping("/dashboard")
    public ResponseEntity<ManagerDashboardDTO> getManagerDashboard(
            @RequestParam(defaultValue = "30") int days,
            Authentication authentication) {
        
        log.info("Getting manager dashboard for last {} days", days);
        
        try {
            // Get manager's team ID (this would be extracted from JWT)
            UUID managerId = extractUserIdFromAuth(authentication);
            UUID teamId = getManagerTeamId(managerId);
            
            if (teamId == null) {
                log.warn("Manager {} has no team assigned", managerId);
                return ResponseEntity.badRequest().build();
            }
            
            LocalDate endDate = LocalDate.now();
            LocalDate startDate = endDate.minusDays(days);
            
            // Calculate team performance metrics
            TeamPerformanceMetrics teamMetrics = kpiCalculationEngine.calculateTeamPerformance(teamId, startDate, endDate);
            
            // Get team tickets
            LocalDateTime startDateTime = startDate.atStartOfDay();
            LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
            List<TicketServiceClient.TicketDTO> teamTickets = ticketServiceClient.getTicketsByTeamAndPeriod(
                    teamId, startDateTime, endDateTime);
            
            // Get team technicians
            List<UserServiceClient.TechnicianDTO> technicians = userServiceClient.getTechniciansByTeam(teamId);
            
            // Get team information
            UserServiceClient.TeamDTO team = userServiceClient.getTeamById(teamId);
            
            // Build dashboard
            ManagerDashboardDTO dashboard = ManagerDashboardDTO.builder()
                    .teamId(teamId)
                    .teamName(team != null ? team.getNom() : "Unknown Team")
                    .teamKPIs(convertToTeamKPIDTO(teamMetrics))
                    .technicianPerformance(getTechnicianPerformance(technicians, startDate, endDate))
                    .ticketDistribution(getTicketDistribution(teamTickets))
                    .workloadOverview(getWorkloadOverview(technicians))
                    .slaAlerts(getSLAAlerts(teamTickets))
                    .periodStart(startDate)
                    .periodEnd(endDate)
                    .generatedAt(LocalDateTime.now())
                    .build();
            
            return ResponseEntity.ok(dashboard);
            
        } catch (Exception e) {
            log.error("Error getting manager dashboard: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get team tickets with filtering
     */
    @GetMapping("/tickets")
    public ResponseEntity<List<TicketServiceClient.TicketDTO>> getTeamTickets(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) UUID technicianId,
            Authentication authentication) {
        
        log.info("Getting team tickets - page: {}, size: {}", page, size);
        
        try {
            UUID managerId = extractUserIdFromAuth(authentication);
            UUID teamId = getManagerTeamId(managerId);
            
            if (teamId == null) {
                return ResponseEntity.badRequest().build();
            }
            
            // Get tickets from last 30 days
            LocalDateTime endDate = LocalDateTime.now();
            LocalDateTime startDate = endDate.minusDays(30);
            
            List<TicketServiceClient.TicketDTO> tickets = ticketServiceClient.getTicketsByTeamAndPeriod(
                    teamId, startDate, endDate);
            
            // Apply filters
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
            
            if (technicianId != null) {
                tickets = tickets.stream()
                        .filter(t -> technicianId.equals(t.getTechnicienId()))
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
            log.error("Error getting team tickets: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get team KPIs for a specific period
     */
    @GetMapping("/kpis")
    public ResponseEntity<TeamKPIDTO> getTeamKPIs(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Authentication authentication) {
        
        log.info("Getting team KPIs from {} to {}", startDate, endDate);
        
        try {
            UUID managerId = extractUserIdFromAuth(authentication);
            UUID teamId = getManagerTeamId(managerId);
            
            if (teamId == null) {
                return ResponseEntity.badRequest().build();
            }
            
            TeamPerformanceMetrics metrics = kpiCalculationEngine.calculateTeamPerformance(teamId, startDate, endDate);
            TeamKPIDTO kpiDTO = convertToTeamKPIDTO(metrics);
            
            return ResponseEntity.ok(kpiDTO);
            
        } catch (Exception e) {
            log.error("Error getting team KPIs: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get technician performance metrics
     */
    @GetMapping("/technicians/performance")
    public ResponseEntity<List<TechnicianPerformanceDTO>> getTechnicianPerformance(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Authentication authentication) {
        
        log.info("Getting technician performance from {} to {}", startDate, endDate);
        
        try {
            UUID managerId = extractUserIdFromAuth(authentication);
            UUID teamId = getManagerTeamId(managerId);
            
            if (teamId == null) {
                return ResponseEntity.badRequest().build();
            }
            
            List<UserServiceClient.TechnicianDTO> technicians = userServiceClient.getTechniciansByTeam(teamId);
            List<TechnicianPerformanceDTO> performance = getTechnicianPerformance(technicians, startDate, endDate);
            
            return ResponseEntity.ok(performance);
            
        } catch (Exception e) {
            log.error("Error getting technician performance: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get workload distribution for team
     */
    @GetMapping("/workload")
    public ResponseEntity<WorkloadOverviewDTO> getWorkloadOverview(Authentication authentication) {
        
        log.info("Getting workload overview");
        
        try {
            UUID managerId = extractUserIdFromAuth(authentication);
            UUID teamId = getManagerTeamId(managerId);
            
            if (teamId == null) {
                return ResponseEntity.badRequest().build();
            }
            
            List<UserServiceClient.TechnicianDTO> technicians = userServiceClient.getTechniciansByTeam(teamId);
            WorkloadOverviewDTO workload = getWorkloadOverview(technicians);
            
            return ResponseEntity.ok(workload);
            
        } catch (Exception e) {
            log.error("Error getting workload overview: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    // Private helper methods
    
    private UUID extractUserIdFromAuth(Authentication authentication) {
        // This would extract the user ID from JWT token
        // For now, return a placeholder
        return UUID.randomUUID();
    }
    
    private UUID getManagerTeamId(UUID managerId) {
        // This would get the team ID for the manager
        // For now, return a placeholder
        UserServiceClient.ManagerDTO manager = userServiceClient.getManagerById(managerId);
        return manager != null ? manager.getTeamId() : null;
    }
    
    private TeamKPIDTO convertToTeamKPIDTO(TeamPerformanceMetrics metrics) {
        return TeamKPIDTO.builder()
                .totalTickets(metrics.getTicketsAssigned())
                .resolvedTickets(metrics.getTicketsResolved())
                .inProgressTickets(metrics.getTicketsInProgress())
                .resolutionRate(metrics.getTauxResolution())
                .slaComplianceRate(metrics.getSlaComplianceRate())
                .averageResolutionTime(metrics.getAverageResolutionTimeMinutes())
                .averageFirstResponseTime(metrics.getAverageFirstResponseTimeMinutes())
                .averageSatisfactionScore(metrics.getAverageSatisfactionScore())
                .reassignmentRate(metrics.getReassignmentRate())
                .performanceLevel(metrics.getNiveauPerformance().name())
                .build();
    }
    
    private List<TechnicianPerformanceDTO> getTechnicianPerformance(List<UserServiceClient.TechnicianDTO> technicians, 
                                                                   LocalDate startDate, LocalDate endDate) {
        return technicians.stream()
                .map(tech -> {
                    // Calculate individual technician metrics
                    LocalDateTime startDateTime = startDate.atStartOfDay();
                    LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
                    
                    // This would get tickets assigned to this technician
                    // For now, use placeholder data
                    return TechnicianPerformanceDTO.builder()
                            .technicianId(tech.getId())
                            .nom(tech.getNom())
                            .prenom(tech.getPrenom())
                            .currentWorkload(tech.getChargeActuelle())
                            .ticketsAssigned(5) // Placeholder
                            .ticketsResolved(4) // Placeholder
                            .averageResolutionTime(BigDecimal.valueOf(120)) // Placeholder
                            .slaComplianceRate(BigDecimal.valueOf(85)) // Placeholder
                            .averageSatisfactionScore(BigDecimal.valueOf(4.2)) // Placeholder
                            .build();
                })
                .collect(Collectors.toList());
    }
    
    private Map<String, Integer> getTicketDistribution(List<TicketServiceClient.TicketDTO> tickets) {
        return Map.of(
                "OUVERT", (int) tickets.stream().filter(t -> "OUVERT".equals(t.getStatut())).count(),
                "EN_COURS", (int) tickets.stream().filter(t -> "EN_COURS".equals(t.getStatut())).count(),
                "RESOLU", (int) tickets.stream().filter(t -> "RESOLU".equals(t.getStatut())).count(),
                "FERME", (int) tickets.stream().filter(t -> "FERME".equals(t.getStatut())).count()
        );
    }
    
    private WorkloadOverviewDTO getWorkloadOverview(List<UserServiceClient.TechnicianDTO> technicians) {
        if (technicians.isEmpty()) {
            return WorkloadOverviewDTO.builder()
                    .totalTechnicians(0)
                    .averageWorkload(BigDecimal.ZERO)
                    .maxWorkload(0)
                    .minWorkload(0)
                    .technicianWorkloads(List.of())
                    .build();
        }
        
        int totalWorkload = technicians.stream().mapToInt(UserServiceClient.TechnicianDTO::getChargeActuelle).sum();
        BigDecimal averageWorkload = BigDecimal.valueOf(totalWorkload)
                .divide(BigDecimal.valueOf(technicians.size()), 2, RoundingMode.HALF_UP);
        
        int maxWorkload = technicians.stream().mapToInt(UserServiceClient.TechnicianDTO::getChargeActuelle).max().orElse(0);
        int minWorkload = technicians.stream().mapToInt(UserServiceClient.TechnicianDTO::getChargeActuelle).min().orElse(0);
        
        List<TechnicianWorkloadDTO> workloads = technicians.stream()
                .map(tech -> TechnicianWorkloadDTO.builder()
                        .technicianId(tech.getId())
                        .nom(tech.getNom())
                        .prenom(tech.getPrenom())
                        .currentWorkload(tech.getChargeActuelle())
                        .build())
                .collect(Collectors.toList());
        
        return WorkloadOverviewDTO.builder()
                .totalTechnicians(technicians.size())
                .averageWorkload(averageWorkload)
                .maxWorkload(maxWorkload)
                .minWorkload(minWorkload)
                .technicianWorkloads(workloads)
                .build();
    }
    
    private List<SLAAlertDTO> getSLAAlerts(List<TicketServiceClient.TicketDTO> tickets) {
        return tickets.stream()
                .filter(t -> t.getDateLimiteSla() != null && 
                           LocalDateTime.now().isAfter(t.getDateLimiteSla().minusHours(4)))
                .map(t -> SLAAlertDTO.builder()
                        .ticketId(t.getId())
                        .ticketTitle(t.getTitre())
                        .slaDeadline(t.getDateLimiteSla())
                        .priority(t.getPriorite())
                        .status(t.getStatut())
                        .technicianId(t.getTechnicienId())
                        .isBreached(LocalDateTime.now().isAfter(t.getDateLimiteSla()))
                        .build())
                .collect(Collectors.toList());
    }
}
