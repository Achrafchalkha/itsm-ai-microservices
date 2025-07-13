package com.itsm.auth.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginUserRequest {
    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "L'email doit être valide")
    private String email;

    @NotBlank(message = "Le mot de passe est obligatoire")
    @JsonProperty(value = "motDePasse", access = JsonProperty.Access.WRITE_ONLY)
    private String motDePasse;

    // Support both "password" and "motDePasse" for convenience
    @JsonProperty("password")
    public void setPassword(String password) {
        this.motDePasse = password;
    }
}
