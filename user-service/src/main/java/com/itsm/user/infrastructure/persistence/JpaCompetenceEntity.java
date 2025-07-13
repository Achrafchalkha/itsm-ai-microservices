package com.itsm.user.infrastructure.persistence;

import com.itsm.user.domain.model.NiveauCompetence;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * JPA Entity for Competence
 * Infrastructure layer representation of the Competence domain model
 */
@Entity
@Table(name = "competences")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JpaCompetenceEntity {

    @Id
    @Column(name = "id", columnDefinition = "UUID")
    private UUID id;

    @Column(name = "nom", nullable = false, length = 100)
    private String nom;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "categorie", length = 50)
    private String categorie;

    @Enumerated(EnumType.STRING)
    @Column(name = "niveau", nullable = false)
    private NiveauCompetence niveau;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private JpaUserEntity user;
}
