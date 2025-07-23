package com.itsm.analytics.presentation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO for individual technician workload
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TechnicianWorkloadDTO {
    
    private UUID technicianId;
    private String nom;
    private String prenom;
    private Integer currentWorkload;
}
