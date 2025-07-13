package com.itsm.user.interfaces.dto;

import com.itsm.user.domain.model.StatutTechnicien;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO for technician search request
 * Used to search for available technicians with specific criteria
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TechnicianSearchRequest {
    
    private String competence;
    private Integer niveauMinimum;
    private String localisation;
    private UUID equipeId;
    private StatutTechnicien statut;
    
    // Default values
    public int getNiveauMinimumOrDefault() {
        return niveauMinimum != null ? niveauMinimum : 1;
    }
    
    public StatutTechnicien getStatutOrDefault() {
        return statut != null ? statut : StatutTechnicien.DISPONIBLE;
    }
}
