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
public class AuthResponse {
    private String token;
    @Builder.Default
    private String type = "Bearer";
    private UUID userId;
    private String email;
    private String nom;
    private String prenom;
    private Role role;
}
