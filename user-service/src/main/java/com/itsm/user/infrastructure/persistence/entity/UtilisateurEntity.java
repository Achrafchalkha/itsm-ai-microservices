package com.itsm.user.infrastructure.persistence.entity;

import com.itsm.user.domain.model.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA Entity for Utilisateur in user-service
 * Maps to utilisateurs table in user_db database
 * Contains business profile information (no password)
 */
@Entity
@Table(name = "utilisateurs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UtilisateurEntity {
    
    @Id
    @Column(name = "id", columnDefinition = "UUID")
    private UUID id;  // Same ID as in auth-service
    
    @Column(name = "nom", nullable = false, length = 100)
    private String nom;
    
    @Column(name = "prenom", nullable = false, length = 100)
    private String prenom;
    
    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private Role role;
    
    @Column(name = "team_id", columnDefinition = "UUID")
    private UUID teamId;
    
    @Column(name = "localisation", length = 255)
    private String localisation;
    
    @Column(name = "telephone", length = 20)
    private String telephone;
    
    @Column(name = "specialite", length = 100)
    private String specialite;
    
    @Column(name = "competences_json", columnDefinition = "TEXT")
    private String competencesJson;
    
    @CreationTimestamp
    @Column(name = "date_creation", nullable = false, updatable = false)
    private LocalDateTime dateCreation;
    
    @UpdateTimestamp
    @Column(name = "date_modification", nullable = false)
    private LocalDateTime dateModification;
    
    @Column(name = "actif", nullable = false)
    @Builder.Default
    private boolean actif = true;
}
