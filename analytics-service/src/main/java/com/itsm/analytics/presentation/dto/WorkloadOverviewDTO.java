package com.itsm.analytics.presentation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO for workload overview
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkloadOverviewDTO {
    
    private Integer totalTechnicians;
    private BigDecimal averageWorkload;
    private Integer maxWorkload;
    private Integer minWorkload;
    private List<TechnicianWorkloadDTO> technicianWorkloads;
}
