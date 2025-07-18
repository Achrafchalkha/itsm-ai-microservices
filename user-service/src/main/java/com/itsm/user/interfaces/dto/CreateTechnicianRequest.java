package com.itsm.user.interfaces.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Request DTO for creating a technician
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTechnicianRequest {

    @NotBlank(message = "Le nom est obligatoire")
    @Size(max = 100, message = "Le nom ne peut pas dépasser 100 caractères")
    private String nom;

    @NotBlank(message = "Le prénom est obligatoire")
    @Size(max = 100, message = "Le prénom ne peut pas dépasser 100 caractères")
    private String prenom;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "L'email doit être valide")
    @Size(max = 255, message = "L'email ne peut pas dépasser 255 caractères")
    private String email;

    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 6, max = 100, message = "Le mot de passe doit contenir entre 6 et 100 caractères")
    private String password;

    @NotNull(message = "L'ID de l'équipe est obligatoire")
    private UUID teamId;

    @Size(max = 100, message = "La localisation ne peut pas dépasser 100 caractères")
    private String localisation;

    @Size(max = 20, message = "Le téléphone ne peut pas dépasser 20 caractères")
    private String telephone;

    @Size(max = 100, message = "La spécialité ne peut pas dépasser 100 caractères")
    private String specialite;

    private String competencesJson;
}
