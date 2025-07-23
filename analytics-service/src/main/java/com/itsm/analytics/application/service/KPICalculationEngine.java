package com.itsm.analytics.application.service;

import com.itsm.analytics.domain.model.DailyKPI;
import com.itsm.analytics.domain.model.TeamPerformanceMetrics;
import com.itsm.analytics.infrastructure.client.TicketServiceClient;
import com.itsm.analytics.infrastructure.client.UserServiceClient;
import com.itsm.analytics.infrastructure.client.AssignmentServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for calculating KPIs from existing data across services
 * Aggregates data from ticket-service, user-service, and assignment-service
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class KPICalculationEngine {
    
    private final TicketServiceClient ticketServiceClient;
    private final UserServiceClient userServiceClient;
    private final AssignmentServiceClient assignmentServiceClient;
    
    /**
     * Calculate daily KPIs for a specific date
     */
    public DailyKPI calculateDailyKPIs(LocalDate date) {
        log.info("Calculating daily KPIs for date: {}", date);
        
        try {
            DailyKPI dailyKPI = DailyKPI.creerKPIJournalier(date);
            
            // Get ticket data for the day
            LocalDateTime startOfDay = date.atStartOfDay();
            LocalDateTime endOfDay = date.atTime(23, 59, 59);
            
            // Calculate ticket metrics
            calculateTicketMetrics(dailyKPI, startOfDay, endOfDay);
            
            // Calculate SLA metrics
            calculateSLAMetrics(dailyKPI, startOfDay, endOfDay);
            
            // Calculate assignment metrics
            calculateAssignmentMetrics(dailyKPI, startOfDay, endOfDay);
            
            // Calculate satisfaction metrics
            calculateSatisfactionMetrics(dailyKPI, startOfDay, endOfDay);
            
            log.info("Completed daily KPI calculation for date: {}", date);
            return dailyKPI;
            
        } catch (Exception e) {
            log.error("Error calculating daily KPIs for date {}: {}", date, e.getMessage(), e);
            throw new RuntimeException("Failed to calculate daily KPIs", e);
        }
    }
    
    /**
     * Calculate team performance metrics for a period
     */
    public TeamPerformanceMetrics calculateTeamPerformance(UUID teamId, LocalDate startDate, LocalDate endDate) {
        log.info("Calculating team performance for team: {} from {} to {}", teamId, startDate, endDate);
        
        try {
            TeamPerformanceMetrics metrics = TeamPerformanceMetrics.creerMetriquesEquipe(teamId, startDate, endDate);
            
            LocalDateTime startDateTime = startDate.atStartOfDay();
            LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
            
            // Get team tickets for the period
            List<TicketServiceClient.TicketDTO> teamTickets = ticketServiceClient.getTicketsByTeamAndPeriod(
                    teamId, startDateTime, endDateTime);
            
            // Calculate ticket volume metrics
            calculateTeamTicketMetrics(metrics, teamTickets);
            
            // Calculate SLA performance
            calculateTeamSLAMetrics(metrics, teamTickets);
            
            // Calculate workload distribution
            calculateTeamWorkloadMetrics(metrics, teamId, startDateTime, endDateTime);
            
            // Calculate satisfaction metrics
            calculateTeamSatisfactionMetrics(metrics, teamTickets);
            
            // Calculate reassignment rate
            calculateTeamReassignmentMetrics(metrics, teamTickets);
            
            log.info("Completed team performance calculation for team: {}", teamId);
            return metrics;
            
        } catch (Exception e) {
            log.error("Error calculating team performance for team {}: {}", teamId, e.getMessage(), e);
            throw new RuntimeException("Failed to calculate team performance", e);
        }
    }
    
    /**
     * Calculate global KPIs for ADMIN dashboard
     */
    public GlobalKPIResult calculateGlobalKPIs(LocalDate startDate, LocalDate endDate) {
        log.info("Calculating global KPIs from {} to {}", startDate, endDate);
        
        try {
            LocalDateTime startDateTime = startDate.atStartOfDay();
            LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
            
            // Get all tickets for the period
            List<TicketServiceClient.TicketDTO> allTickets = ticketServiceClient.getTicketsByPeriod(
                    startDateTime, endDateTime);
            
            // Get all teams
            List<UserServiceClient.TeamDTO> allTeams = userServiceClient.getAllTeams();
            
            // Calculate global metrics
            GlobalKPIResult result = new GlobalKPIResult();
            
            // Volume metrics
            result.setTotalTickets(allTickets.size());
            result.setTicketsResolved((int) allTickets.stream()
                    .filter(t -> "RESOLU".equals(t.getStatut()) || "FERME".equals(t.getStatut()))
                    .count());
            
            // SLA metrics
            calculateGlobalSLAMetrics(result, allTickets);
            
            // Satisfaction metrics
            calculateGlobalSatisfactionMetrics(result, allTickets);
            
            // Team performance
            calculateGlobalTeamMetrics(result, allTeams, startDate, endDate);
            
            log.info("Completed global KPI calculation");
            return result;
            
        } catch (Exception e) {
            log.error("Error calculating global KPIs: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to calculate global KPIs", e);
        }
    }
    
    /**
     * Calculate MTTR (Mean Time To Resolution) for a team
     */
    public BigDecimal calculateMTTR(UUID teamId, LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
        
        List<TicketServiceClient.TicketDTO> resolvedTickets = ticketServiceClient.getResolvedTicketsByTeamAndPeriod(
                teamId, startDateTime, endDateTime);
        
        if (resolvedTickets.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        double averageResolutionTime = resolvedTickets.stream()
                .filter(t -> t.getTempsResolutionMinutes() != null)
                .mapToInt(TicketServiceClient.TicketDTO::getTempsResolutionMinutes)
                .average()
                .orElse(0.0);
        
        return BigDecimal.valueOf(averageResolutionTime).setScale(2, RoundingMode.HALF_UP);
    }
    
    /**
     * Calculate SLA compliance rate for a team
     */
    public BigDecimal calculateSLAComplianceRate(UUID teamId, LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
        
        List<TicketServiceClient.TicketDTO> teamTickets = ticketServiceClient.getTicketsByTeamAndPeriod(
                teamId, startDateTime, endDateTime);
        
        if (teamTickets.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        long ticketsWithinSLA = teamTickets.stream()
                .filter(t -> t.getSlaRespecte() != null && t.getSlaRespecte())
                .count();
        
        return BigDecimal.valueOf(ticketsWithinSLA)
                .divide(BigDecimal.valueOf(teamTickets.size()), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }
    
    // Private helper methods
    
    private void calculateTicketMetrics(DailyKPI dailyKPI, LocalDateTime start, LocalDateTime end) {
        List<TicketServiceClient.TicketDTO> createdTickets = ticketServiceClient.getTicketsCreatedBetween(start, end);
        List<TicketServiceClient.TicketDTO> resolvedTickets = ticketServiceClient.getTicketsResolvedBetween(start, end);
        List<TicketServiceClient.TicketDTO> closedTickets = ticketServiceClient.getTicketsClosedBetween(start, end);
        
        dailyKPI.incrementerTicketsCreated(createdTickets.size());
        dailyKPI.incrementerTicketsResolus(resolvedTickets.size());
        // Update closed tickets count
    }
    
    private void calculateSLAMetrics(DailyKPI dailyKPI, LocalDateTime start, LocalDateTime end) {
        List<TicketServiceClient.TicketDTO> tickets = ticketServiceClient.getTicketsByPeriod(start, end);
        
        int withinSLA = (int) tickets.stream()
                .filter(t -> t.getSlaRespecte() != null && t.getSlaRespecte())
                .count();
        
        int breachedSLA = (int) tickets.stream()
                .filter(t -> t.getSlaRespecte() != null && !t.getSlaRespecte())
                .count();
        
        dailyKPI.mettreAJourSLA(withinSLA, breachedSLA);
    }
    
    private void calculateAssignmentMetrics(DailyKPI dailyKPI, LocalDateTime start, LocalDateTime end) {
        // This would call assignment-service to get assignment metrics
        // For now, we'll use placeholder values
        dailyKPI.mettreAJourAssignations(0, 0, BigDecimal.ZERO);
    }
    
    private void calculateSatisfactionMetrics(DailyKPI dailyKPI, LocalDateTime start, LocalDateTime end) {
        // This would calculate satisfaction metrics from satisfaction_scores table
        // For now, we'll use placeholder values
        dailyKPI.mettreAJourSatisfaction(0, BigDecimal.ZERO);
    }
    
    private void calculateTeamTicketMetrics(TeamPerformanceMetrics metrics, List<TicketServiceClient.TicketDTO> tickets) {
        int assigned = tickets.size();
        int resolved = (int) tickets.stream()
                .filter(t -> "RESOLU".equals(t.getStatut()) || "FERME".equals(t.getStatut()))
                .count();
        int inProgress = (int) tickets.stream()
                .filter(t -> "EN_COURS".equals(t.getStatut()))
                .count();
        
        metrics.mettreAJourTickets(assigned, resolved, inProgress);
    }
    
    private void calculateTeamSLAMetrics(TeamPerformanceMetrics metrics, List<TicketServiceClient.TicketDTO> tickets) {
        if (tickets.isEmpty()) {
            metrics.mettreAJourSLA(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
            return;
        }
        
        // SLA compliance rate
        long withinSLA = tickets.stream()
                .filter(t -> t.getSlaRespecte() != null && t.getSlaRespecte())
                .count();
        
        BigDecimal complianceRate = BigDecimal.valueOf(withinSLA)
                .divide(BigDecimal.valueOf(tickets.size()), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
        
        // Average resolution time
        BigDecimal avgResolutionTime = BigDecimal.valueOf(tickets.stream()
                .filter(t -> t.getTempsResolutionMinutes() != null)
                .mapToInt(TicketServiceClient.TicketDTO::getTempsResolutionMinutes)
                .average()
                .orElse(0.0));
        
        // Average first response time
        BigDecimal avgFirstResponseTime = BigDecimal.valueOf(tickets.stream()
                .filter(t -> t.getTempsPremiereReponseMinutes() != null)
                .mapToInt(TicketServiceClient.TicketDTO::getTempsPremiereReponseMinutes)
                .average()
                .orElse(0.0));
        
        metrics.mettreAJourSLA(complianceRate, avgResolutionTime, avgFirstResponseTime);
    }
    
    private void calculateTeamWorkloadMetrics(TeamPerformanceMetrics metrics, UUID teamId, 
                                            LocalDateTime start, LocalDateTime end) {
        List<UserServiceClient.TechnicianDTO> technicians = userServiceClient.getTechniciansByTeam(teamId);
        
        if (technicians.isEmpty()) {
            metrics.mettreAJourCharge(0, BigDecimal.ZERO, 0, 0);
            return;
        }
        
        int totalWorkload = technicians.stream()
                .mapToInt(UserServiceClient.TechnicianDTO::getChargeActuelle)
                .sum();
        
        BigDecimal avgWorkload = BigDecimal.valueOf(totalWorkload)
                .divide(BigDecimal.valueOf(technicians.size()), 2, RoundingMode.HALF_UP);
        
        int maxWorkload = technicians.stream()
                .mapToInt(UserServiceClient.TechnicianDTO::getChargeActuelle)
                .max()
                .orElse(0);
        
        int minWorkload = technicians.stream()
                .mapToInt(UserServiceClient.TechnicianDTO::getChargeActuelle)
                .min()
                .orElse(0);
        
        metrics.mettreAJourCharge(totalWorkload, avgWorkload, maxWorkload, minWorkload);
    }
    
    private void calculateTeamSatisfactionMetrics(TeamPerformanceMetrics metrics, List<TicketServiceClient.TicketDTO> tickets) {
        // This would calculate satisfaction from satisfaction_scores table
        // For now, placeholder values
        metrics.mettreAJourSatisfaction(BigDecimal.ZERO, 0);
    }
    
    private void calculateTeamReassignmentMetrics(TeamPerformanceMetrics metrics, List<TicketServiceClient.TicketDTO> tickets) {
        if (tickets.isEmpty()) {
            metrics.mettreAJourReassignation(BigDecimal.ZERO);
            return;
        }
        
        long reassignedTickets = tickets.stream()
                .filter(t -> t.getNombreReassignations() != null && t.getNombreReassignations() > 0)
                .count();
        
        BigDecimal reassignmentRate = BigDecimal.valueOf(reassignedTickets)
                .divide(BigDecimal.valueOf(tickets.size()), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
        
        metrics.mettreAJourReassignation(reassignmentRate);
    }
    
    private void calculateGlobalSLAMetrics(GlobalKPIResult result, List<TicketServiceClient.TicketDTO> tickets) {
        // Implementation for global SLA metrics
    }
    
    private void calculateGlobalSatisfactionMetrics(GlobalKPIResult result, List<TicketServiceClient.TicketDTO> tickets) {
        // Implementation for global satisfaction metrics
    }
    
    private void calculateGlobalTeamMetrics(GlobalKPIResult result, List<UserServiceClient.TeamDTO> teams, 
                                          LocalDate startDate, LocalDate endDate) {
        // Implementation for global team metrics
    }
    
    /**
     * Result class for global KPIs
     */
    public static class GlobalKPIResult {
        private int totalTickets;
        private int ticketsResolved;
        private BigDecimal slaComplianceRate;
        private BigDecimal averageSatisfactionScore;
        private Map<String, Object> teamMetrics;
        
        // Getters and setters
        public int getTotalTickets() { return totalTickets; }
        public void setTotalTickets(int totalTickets) { this.totalTickets = totalTickets; }
        
        public int getTicketsResolved() { return ticketsResolved; }
        public void setTicketsResolved(int ticketsResolved) { this.ticketsResolved = ticketsResolved; }
        
        public BigDecimal getSlaComplianceRate() { return slaComplianceRate; }
        public void setSlaComplianceRate(BigDecimal slaComplianceRate) { this.slaComplianceRate = slaComplianceRate; }
        
        public BigDecimal getAverageSatisfactionScore() { return averageSatisfactionScore; }
        public void setAverageSatisfactionScore(BigDecimal averageSatisfactionScore) { this.averageSatisfactionScore = averageSatisfactionScore; }
        
        public Map<String, Object> getTeamMetrics() { return teamMetrics; }
        public void setTeamMetrics(Map<String, Object> teamMetrics) { this.teamMetrics = teamMetrics; }
    }
}
