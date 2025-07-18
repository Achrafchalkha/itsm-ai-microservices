package com.itsm.user.interfaces.dto;

import com.itsm.user.domain.model.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Response DTO for manager creation
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateManagerResponse {

    private UUID managerId;
    private String email;
    private String nom;
    private String prenom;
    private Role role;
    private String message;
}
