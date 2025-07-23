package com.itsm.user.interfaces.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for team information
 * Used by assignment-service for team data
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamResponseDTO {
    
    private UUID id;
    private String nom;
    private String description;
    private UUID managerId;
    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;
    private Boolean actif;
}
