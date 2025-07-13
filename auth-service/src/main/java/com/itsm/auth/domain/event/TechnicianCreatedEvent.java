package com.itsm.auth.domain.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TechnicianCreatedEvent {
    
    private UUID technicianId;
    private String email;
    private String nom;
    private String prenom;
    private UUID teamId;
    private String teamName;
    private UUID managerId;
    private String managerEmail;
    private String localisation;
    private String telephone;
    private String specialite;
    private List<CompetenceInfo> competences;
    private LocalDateTime dateCreation;
    
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
