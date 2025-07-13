package com.itsm.auth.application.dto;

import com.itsm.auth.domain.model.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTechnicianResponse {
    
    private String message;
    private UUID technicianId;
    private String email;
    private String nom;
    private String prenom;
    private Role role;
    private UUID teamId;
    private String teamName;
    private String localisation;
    private String telephone;
    private String specialite;
    private List<CompetenceInfo> competences;
    private boolean success;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CompetenceInfo {
        private String nom;
        private String description;
        private String categorie;
        private String niveau;
    }
}
