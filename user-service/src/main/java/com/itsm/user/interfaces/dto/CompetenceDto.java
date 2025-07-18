package com.itsm.user.interfaces.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for Competence
 * Matches the example: nom, description, categorie, niveau
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompetenceDto {

    @NotBlank(message = "Le nom de la compétence est obligatoire")
    @Size(max = 100, message = "Le nom ne peut pas dépasser 100 caractères")
    private String nom;

    @Size(max = 500, message = "La description ne peut pas dépasser 500 caractères")
    private String description;

    @NotBlank(message = "La catégorie est obligatoire")
    @Size(max = 50, message = "La catégorie ne peut pas dépasser 50 caractères")
    private String categorie;  // DEVOPS, CLOUD, DEVELOPPEMENT, etc.

    @NotBlank(message = "Le niveau est obligatoire")
    @Size(max = 20, message = "Le niveau ne peut pas dépasser 20 caractères")
    private String niveau;     // JUNIOR, AVANCE, SENIOR, EXPERT
}
