package com.itsm.user.interfaces.dto;

import com.itsm.user.domain.model.NiveauCompetence;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for adding competence to a technician
 * Used to receive competence addition requests via REST API
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddCompetenceRequest {

    @NotBlank(message = "Le nom de la compétence est obligatoire")
    @Size(max = 100, message = "Le nom ne peut pas dépasser 100 caractères")
    private String nom;

    @Size(max = 500, message = "La description ne peut pas dépasser 500 caractères")
    private String description;

    @Size(max = 50, message = "La catégorie ne peut pas dépasser 50 caractères")
    private String categorie;

    @NotNull(message = "Le niveau de compétence est obligatoire")
    private NiveauCompetence niveau;
}
