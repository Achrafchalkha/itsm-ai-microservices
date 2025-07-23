package com.itsm.analytics.presentation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for SLA alert information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SLAAlertDTO {
    
    private UUID ticketId;
    private String ticketTitle;
    private LocalDateTime slaDeadline;
    private String priority;
    private String status;
    private UUID technicianId;
    private Boolean isBreached;
}
