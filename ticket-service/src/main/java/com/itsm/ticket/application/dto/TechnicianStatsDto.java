package com.itsm.ticket.application.dto;

import java.util.UUID;

/**
 * DTO for technician statistics
 */
public class TechnicianStatsDto {
    private UUID technicianId;
    private String technicianName;
    private long assignedTickets;
    private long resolvedTickets;
    private double resolutionRate;
    
    public TechnicianStatsDto() {}
    
    public TechnicianStatsDto(UUID technicianId, String technicianName, long assignedTickets, 
                             long resolvedTickets, double resolutionRate) {
        this.technicianId = technicianId;
        this.technicianName = technicianName;
        this.assignedTickets = assignedTickets;
        this.resolvedTickets = resolvedTickets;
        this.resolutionRate = resolutionRate;
    }
    
    // Getters and setters
    public UUID getTechnicianId() { return technicianId; }
    public void setTechnicianId(UUID technicianId) { this.technicianId = technicianId; }
    
    public String getTechnicianName() { return technicianName; }
    public void setTechnicianName(String technicianName) { this.technicianName = technicianName; }
    
    public long getAssignedTickets() { return assignedTickets; }
    public void setAssignedTickets(long assignedTickets) { this.assignedTickets = assignedTickets; }
    
    public long getResolvedTickets() { return resolvedTickets; }
    public void setResolvedTickets(long resolvedTickets) { this.resolvedTickets = resolvedTickets; }
    
    public double getResolutionRate() { return resolutionRate; }
    public void setResolutionRate(double resolutionRate) { this.resolutionRate = resolutionRate; }
}
