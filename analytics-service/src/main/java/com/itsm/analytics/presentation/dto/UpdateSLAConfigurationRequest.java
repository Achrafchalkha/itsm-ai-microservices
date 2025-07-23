package com.itsm.analytics.presentation.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating SLA configuration
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateSLAConfigurationRequest {
    
    @NotNull(message = "Délai première réponse est obligatoire")
    @Min(value = 1, message = "Délai première réponse doit être positif")
    private Integer delaiPremiereReponseHeures;
    
    @NotNull(message = "Délai résolution est obligatoire")
    @Min(value = 1, message = "Délai résolution doit être positif")
    private Integer delaiResolutionHeures;
    
    @NotNull(message = "Escalade manager est obligatoire")
    @Min(value = 1, message = "Escalade manager doit être positif")
    private Integer escaladeManagerHeures;
    
    @NotNull(message = "Escalade admin est obligatoire")
    @Min(value = 1, message = "Escalade admin doit être positif")
    private Integer escaladeAdminHeures;
}
