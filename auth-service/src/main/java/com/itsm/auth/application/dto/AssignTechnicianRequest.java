package com.itsm.auth.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssignTechnicianRequest {

    @NotNull(message = "L'ID du technicien est obligatoire")
    private UUID technicianId;

    @NotNull(message = "L'équipe est obligatoire")
    private UUID teamId;

    private String localisation;

    private String telephone;

    private String specialite;

    private List<CompetenceRequest> competences;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CompetenceRequest {
        @NotBlank(message = "Le nom de la compétence est obligatoire")
        private String nom;
        
        private String description;
        
        private String categorie;
        
        @NotBlank(message = "Le niveau est obligatoire")
        private String niveau; // DEBUTANT, INTERMEDIAIRE, EXPERT
    }
}
