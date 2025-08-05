package com.itsm.ticket.application.dto;

import java.util.List;
import java.util.UUID;

/**
 * DTO for team dashboard response
 */
public class TeamDashboardDto {
    private long totalTickets;
    private long openTickets;
    private long inProgressTickets;
    private long resolvedTickets;
    private long closedTickets;
    private double averageResolutionTime;
    private List<TechnicianStatsDto> technicianStats;
    
    // Constructors
    public TeamDashboardDto() {}
    
    public TeamDashboardDto(long totalTickets, long openTickets, long inProgressTickets, 
                           long resolvedTickets, long closedTickets, double averageResolutionTime,
                           List<TechnicianStatsDto> technicianStats) {
        this.totalTickets = totalTickets;
        this.openTickets = openTickets;
        this.inProgressTickets = inProgressTickets;
        this.resolvedTickets = resolvedTickets;
        this.closedTickets = closedTickets;
        this.averageResolutionTime = averageResolutionTime;
        this.technicianStats = technicianStats;
    }
    
    // Getters and setters
    public long getTotalTickets() { return totalTickets; }
    public void setTotalTickets(long totalTickets) { this.totalTickets = totalTickets; }
    
    public long getOpenTickets() { return openTickets; }
    public void setOpenTickets(long openTickets) { this.openTickets = openTickets; }
    
    public long getInProgressTickets() { return inProgressTickets; }
    public void setInProgressTickets(long inProgressTickets) { this.inProgressTickets = inProgressTickets; }
    
    public long getResolvedTickets() { return resolvedTickets; }
    public void setResolvedTickets(long resolvedTickets) { this.resolvedTickets = resolvedTickets; }
    
    public long getClosedTickets() { return closedTickets; }
    public void setClosedTickets(long closedTickets) { this.closedTickets = closedTickets; }
    
    public double getAverageResolutionTime() { return averageResolutionTime; }
    public void setAverageResolutionTime(double averageResolutionTime) { this.averageResolutionTime = averageResolutionTime; }
    
    public List<TechnicianStatsDto> getTechnicianStats() { return technicianStats; }
    public void setTechnicianStats(List<TechnicianStatsDto> technicianStats) { this.technicianStats = technicianStats; }
}
