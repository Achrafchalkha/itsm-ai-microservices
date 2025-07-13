package com.itsm.auth.domain.event;

import com.itsm.auth.domain.model.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCreatedEvent {
    private UUID userId;
    private String email;
    private String nom;
    private String prenom;
    private Role role;
    private LocalDateTime dateCreation;
}
