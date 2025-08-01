package com.itsm.user.interfaces.dto;

import com.itsm.user.domain.model.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO for Manager with enriched team information
 * Used by admin endpoints to display complete manager data with team details
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ManagerWithTeamDto {

    private UUID id;
    private String nom;
    private String prenom;
    private String email;
    private Role role;
    private UUID teamId;
    private String teamName;
    private String teamDescription;
    private List<String> teamCategories;
    private String localisation;
    private String telephone;
    private String specialite;
    private String competencesJson;
    private Integer chargeActuelle;
    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;
    private boolean actif;
}
