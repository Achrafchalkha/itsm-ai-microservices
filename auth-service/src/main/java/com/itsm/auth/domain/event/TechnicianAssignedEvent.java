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
public class TechnicianAssignedEvent {
    
    private UUID technicianId;
    private String technicianEmail;
    private String technicianNom;
    private String technicianPrenom;
    private UUID teamId;
    private String teamName;
    private UUID managerId;
    private String managerEmail;
    private String managerNom;
    private String managerPrenom;
    private LocalDateTime dateAssignation;
}
