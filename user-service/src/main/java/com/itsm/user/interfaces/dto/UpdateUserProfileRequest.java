package com.itsm.user.interfaces.dto;

import com.itsm.user.domain.model.StatutTechnicien;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO for updating user profile
 * Used to receive user profile update requests via REST API
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserProfileRequest {
    
    @NotBlank(message = "Le nom est obligatoire")
    @Size(max = 100, message = "Le nom ne peut pas dépasser 100 caractères")
    private String nom;
    
    @NotBlank(message = "Le prénom est obligatoire")
    @Size(max = 100, message = "Le prénom ne peut pas dépasser 100 caractères")
    private String prenom;
    
    @Size(max = 255, message = "La localisation ne peut pas dépasser 255 caractères")
    private String localisation;
    
    private StatutTechnicien statutTechnicien;
    
    private UUID equipeId;
}
