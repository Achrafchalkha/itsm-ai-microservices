package com.itsm.user.infrastructure.persistence;

import com.itsm.user.domain.model.Role;
import com.itsm.user.domain.model.StatutTechnicien;
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
 * JPA Entity for User
 * Infrastructure layer representation of the User domain model
 */
@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JpaUserEntity {
    
    @Id
    @Column(name = "id", columnDefinition = "UUID")
    private UUID id;
    
    @Column(name = "nom", nullable = false, length = 100)
    private String nom;
    
    @Column(name = "prenom", nullable = false, length = 100)
    private String prenom;
    
    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "statut_technicien")
    private StatutTechnicien statutTechnicien;
    
    @Column(name = "localisation", length = 255)
    private String localisation;

    @Column(name = "manager_id", columnDefinition = "UUID")
    private UUID managerId;

    @Column(name = "team_id", columnDefinition = "UUID")
    private UUID teamId;
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @Builder.Default
    private List<JpaCompetenceEntity> competences = new ArrayList<>();
    
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
