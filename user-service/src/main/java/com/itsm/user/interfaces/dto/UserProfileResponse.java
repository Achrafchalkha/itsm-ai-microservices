package com.itsm.user.interfaces.dto;

import com.itsm.user.domain.model.NiveauCompetence;
import com.itsm.user.domain.model.Role;
import com.itsm.user.domain.model.StatutTechnicien;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO for user profile response
 * Used to return user profile information via REST API
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {
    private UUID id;
    private String nom;
    private String prenom;
    private String email;
    private Role role;
    private UUID managerId;                 // Manager reference (for TECHNICIEN)
    private UUID teamId;                    // Team assignment
    private StatutTechnicien statutTechnicien;
    private String localisation;
    private List<CompetenceDto> competences;
    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;
    private boolean actif;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CompetenceDto {
        private UUID id;
        private String nom;
        private String description;
        private String categorie;
        private NiveauCompetence niveau;

        // For backward compatibility
        public int getNiveauExpertise() {
            return niveau != null ? niveau.ordinal() + 1 : 1;
        }
    }
}
