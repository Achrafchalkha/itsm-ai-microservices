package com.itsm.user.infrastructure.kafka;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Event representing a team creation from auth-service
 * This is the infrastructure representation of the event received via Kafka
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamCreatedEvent {
    
    private UUID teamId;
    private String teamName;
    private String teamDescription;
    private UUID managerId;
    private String managerEmail;
    private String managerNom;
    private String managerPrenom;
    private LocalDateTime dateCreation;
}
