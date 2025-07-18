package com.itsm.user.interfaces.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating a manager
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateManagerRequest {

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

    @Size(max = 100, message = "La localisation ne peut pas dépasser 100 caractères")
    private String localisation;

    @Size(max = 20, message = "Le téléphone ne peut pas dépasser 20 caractères")
    private String telephone;

    @Size(max = 100, message = "La spécialité ne peut pas dépasser 100 caractères")
    private String specialite;

    @NotBlank(message = "Le nom de l'équipe est obligatoire")
    @Size(max = 100, message = "Le nom de l'équipe ne peut pas dépasser 100 caractères")
    private String teamName;

    @Size(max = 500, message = "La description de l'équipe ne peut pas dépasser 500 caractères")
    private String teamDescription;
}
