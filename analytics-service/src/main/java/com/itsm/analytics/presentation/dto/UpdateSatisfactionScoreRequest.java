package com.itsm.analytics.presentation.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating satisfaction score details
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateSatisfactionScoreRequest {
    
    @NotNull(message = "Temps résolution satisfaisant est obligatoire")
    private Boolean tempsResolutionSatisfaisant;
    
    @NotNull(message = "Score qualité communication est obligatoire")
    @Min(value = 1, message = "Score qualité communication doit être entre 1 et 5")
    @Max(value = 5, message = "Score qualité communication doit être entre 1 et 5")
    private Integer qualiteCommunicationScore;
    
    @NotNull(message = "Score compétence technique est obligatoire")
    @Min(value = 1, message = "Score compétence technique doit être entre 1 et 5")
    @Max(value = 5, message = "Score compétence technique doit être entre 1 et 5")
    private Integer competenceTechniqueScore;
}
