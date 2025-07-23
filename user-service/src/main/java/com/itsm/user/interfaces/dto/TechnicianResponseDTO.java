package com.itsm.user.interfaces.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for technician information
 * Used by assignment-service for technician data
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TechnicianResponseDTO {
    
    private UUID id;
    private String nom;
    private String prenom;
    private String email;
    private String role;
    private UUID teamId;
    private String localisation;
    private String telephone;
    private String specialite;
    private String competencesJson;
    private Integer chargeActuelle;
    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;
    private Boolean actif;
}
