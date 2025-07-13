package com.itsm.user.infrastructure.persistence;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * JPA Entity for Team
 * Infrastructure layer representation of the Team domain model
 */
@Entity
@Table(name = "teams")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JpaTeamEntity {
    
    @Id
    @Column(name = "id", columnDefinition = "UUID")
    private UUID id;
    
    @Column(name = "nom", nullable = false, length = 100)
    private String nom;
    
    @Column(name = "description", length = 500)
    private String description;
    
    @Column(name = "manager_id", nullable = false, columnDefinition = "UUID")
    private UUID managerId;
    
    @ElementCollection
    @CollectionTable(name = "team_categories", joinColumns = @JoinColumn(name = "team_id"))
    @Column(name = "categorie")
    @Builder.Default
    private List<String> categories = new ArrayList<>();
    
    @ElementCollection
    @CollectionTable(name = "team_members", joinColumns = @JoinColumn(name = "team_id"))
    @Column(name = "member_id", columnDefinition = "UUID")
    @Builder.Default
    private List<UUID> memberIds = new ArrayList<>();
    
    @Column(name = "date_creation", nullable = false)
    private LocalDateTime dateCreation;
    
    @Column(name = "date_modification", nullable = false)
    private LocalDateTime dateModification;
    
    @Column(name = "actif", nullable = false)
    private boolean actif;
    
    @PrePersist
    protected void onCreate() {
        if (dateCreation == null) {
            dateCreation = LocalDateTime.now();
        }
        if (dateModification == null) {
            dateModification = LocalDateTime.now();
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        dateModification = LocalDateTime.now();
    }
}
