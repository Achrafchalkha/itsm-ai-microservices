package com.itsm.auth.domain.event;

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
