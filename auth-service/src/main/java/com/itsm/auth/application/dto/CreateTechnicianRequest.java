package com.itsm.auth.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateTechnicianRequest {
    
    @NotBlank(message = "Le nom est obligatoire")
    private String nom;

    @NotBlank(message = "Le prénom est obligatoire")
    private String prenom;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "L'email doit être valide")
    private String email;

    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caractères")
    private String motDePasse;

    // Additional technician profile details
    // Note: teamId is automatically determined from the logged-in manager's team
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
