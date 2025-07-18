package com.itsm.user.interfaces.dto;

import com.itsm.user.domain.model.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for Utilisateur entity
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UtilisateurDto {

    private UUID id;
    private String nom;
    private String prenom;
    private String email;
    private Role role;
    private UUID teamId;
    private String localisation;
    private String telephone;
    private String specialite;
    private String competencesJson;
    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;
    private boolean actif;
}
