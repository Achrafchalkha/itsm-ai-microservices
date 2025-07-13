package com.itsm.auth.application.dto;

import com.itsm.auth.domain.model.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateManagerResponse {
    
    private String message;
    private UUID managerId;
    private String email;
    private String nom;
    private String prenom;
    private Role role;
    private String teamName;
    private String teamDescription;
    private boolean success;
}
