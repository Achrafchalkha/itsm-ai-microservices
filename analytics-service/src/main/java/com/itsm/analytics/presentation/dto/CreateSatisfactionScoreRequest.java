package com.itsm.analytics.presentation.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Request DTO for creating satisfaction score
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateSatisfactionScoreRequest {
    
    @NotNull(message = "Ticket ID est obligatoire")
    private UUID ticketId;
    
    @NotNull(message = "Technician ID est obligatoire")
    private UUID technicienId;
    
    @NotNull(message = "Team ID est obligatoire")
    private UUID teamId;
    
    @NotNull(message = "Score est obligatoire")
    @Min(value = 1, message = "Score doit être entre 1 et 5")
    @Max(value = 5, message = "Score doit être entre 1 et 5")
    private Integer score;
    
    private String commentaire;
}
