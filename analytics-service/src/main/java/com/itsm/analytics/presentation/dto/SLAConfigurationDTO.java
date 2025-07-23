package com.itsm.analytics.presentation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for SLA Configuration responses
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SLAConfigurationDTO {
    
    private UUID id;
    private String categorie;
    private String priorite;
    private Integer delaiPremiereReponseHeures;
    private Integer delaiResolutionHeures;
    private Integer escaladeManagerHeures;
    private Integer escaladeAdminHeures;
    private Boolean actif;
    private UUID createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
