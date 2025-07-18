package com.itsm.user.interfaces.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request DTO for updating a technician
 * Allows updating all fields including password (optional)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTechnicianRequest {

    @Size(max = 100, message = "Le nom ne peut pas dépasser 100 caractères")
    private String nom;

    @Size(max = 100, message = "Le prénom ne peut pas dépasser 100 caractères")
    private String prenom;

    @Email(message = "L'email doit être valide")
    @Size(max = 255, message = "L'email ne peut pas dépasser 255 caractères")
    private String email;

    @Size(min = 6, max = 100, message = "Le mot de passe doit contenir entre 6 et 100 caractères")
    private String motDePasse;  // Optional - only if changing password

    @Size(max = 255, message = "La localisation ne peut pas dépasser 255 caractères")
    private String localisation;

    @Size(max = 20, message = "Le téléphone ne peut pas dépasser 20 caractères")
    private String telephone;

    @Size(max = 100, message = "La spécialité ne peut pas dépasser 100 caractères")
    private String specialite;

    // Competences list
    private List<CompetenceDto> competences;
}
