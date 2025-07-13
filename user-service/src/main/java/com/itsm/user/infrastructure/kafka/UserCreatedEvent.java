package com.itsm.user.infrastructure.kafka;

import com.itsm.user.domain.model.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Event representing a user creation from auth-service
 * This is the infrastructure representation of the event received via Kafka
 */
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
