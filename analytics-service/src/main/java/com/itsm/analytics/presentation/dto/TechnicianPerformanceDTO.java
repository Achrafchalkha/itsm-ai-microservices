package com.itsm.analytics.presentation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO for technician performance metrics
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TechnicianPerformanceDTO {
    
    private UUID technicianId;
    private String nom;
    private String prenom;
    private Integer currentWorkload;
    private Integer ticketsAssigned;
    private Integer ticketsResolved;
    private BigDecimal averageResolutionTime;
    private BigDecimal slaComplianceRate;
    private BigDecimal averageSatisfactionScore;
}
