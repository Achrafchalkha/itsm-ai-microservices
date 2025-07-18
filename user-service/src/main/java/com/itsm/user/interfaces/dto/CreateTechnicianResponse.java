package com.itsm.user.interfaces.dto;

import com.itsm.user.domain.model.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Response DTO for technician creation
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTechnicianResponse {

    private UUID technicianId;
    private String email;
    private String nom;
    private String prenom;
    private Role role;
    private UUID teamId;
    private String message;
}
