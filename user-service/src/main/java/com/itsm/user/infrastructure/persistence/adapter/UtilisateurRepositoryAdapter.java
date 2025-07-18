package com.itsm.user.infrastructure.persistence.adapter;

import com.itsm.user.domain.model.Role;
import com.itsm.user.domain.model.Utilisateur;
import com.itsm.user.domain.repository.UtilisateurRepository;
import com.itsm.user.infrastructure.persistence.entity.UtilisateurEntity;
import com.itsm.user.infrastructure.persistence.repository.JpaUtilisateurRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Adapter that implements UtilisateurRepository using JPA
 * Bridges between domain model and persistence layer
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class UtilisateurRepositoryAdapter implements UtilisateurRepository {
    
    private final JpaUtilisateurRepository jpaRepository;

    @Override
    public Utilisateur save(Utilisateur utilisateur) {
        log.debug("Saving utilisateur: {}", utilisateur.getEmail());
        UtilisateurEntity entity = toEntity(utilisateur);
        UtilisateurEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Utilisateur> findById(UUID id) {
        log.debug("Finding utilisateur by ID: {}", id);
        return jpaRepository.findById(id)
                .map(this::toDomain);
    }

    @Override
    public Optional<Utilisateur> findByEmail(String email) {
        log.debug("Finding utilisateur by email: {}", email);
        return jpaRepository.findByEmail(email)
                .map(this::toDomain);
    }

    @Override
    public List<Utilisateur> findAll() {
        log.debug("Finding all utilisateurs");
        return jpaRepository.findAll().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Utilisateur> findByRole(Role role) {
        log.debug("Finding utilisateurs by role: {}", role);
        return jpaRepository.findByRole(role).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Utilisateur> findByRoleAndActif(Role role, boolean actif) {
        log.debug("Finding utilisateurs by role and active status: {} - {}", role, actif);
        return jpaRepository.findByRoleAndActif(role, actif).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Utilisateur> findByTeamId(UUID teamId) {
        log.debug("Finding utilisateurs by team ID: {}", teamId);
        return jpaRepository.findByTeamId(teamId).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Utilisateur> findByTeamIdAndRole(UUID teamId, Role role) {
        log.debug("Finding utilisateurs by team ID and role: {} - {}", teamId, role);
        return jpaRepository.findByTeamIdAndRole(teamId, role).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpaRepository.existsByEmail(email);
    }

    @Override
    public void deleteById(UUID id) {
        log.debug("Deleting utilisateur by ID: {}", id);
        jpaRepository.deleteById(id);
    }

    @Override
    public long countByRole(Role role) {
        return jpaRepository.countByRole(role);
    }

    @Override
    public long countByTeamId(UUID teamId) {
        return jpaRepository.countByTeamId(teamId);
    }

    /**
     * Convert domain model to entity
     */
    private UtilisateurEntity toEntity(Utilisateur utilisateur) {
        if (utilisateur == null) return null;
        
        return UtilisateurEntity.builder()
                .id(utilisateur.getId())
                .nom(utilisateur.getNom())
                .prenom(utilisateur.getPrenom())
                .email(utilisateur.getEmail())
                .role(utilisateur.getRole())
                .teamId(utilisateur.getTeamId())
                .localisation(utilisateur.getLocalisation())
                .telephone(utilisateur.getTelephone())
                .specialite(utilisateur.getSpecialite())
                .competencesJson(utilisateur.getCompetencesJson())
                .chargeActuelle(utilisateur.getChargeActuelle())
                .dateCreation(utilisateur.getDateCreation())
                .dateModification(utilisateur.getDateModification())
                .actif(utilisateur.isActif())
                .build();
    }

    /**
     * Convert entity to domain model
     */
    private Utilisateur toDomain(UtilisateurEntity entity) {
        if (entity == null) return null;
        
        return Utilisateur.builder()
                .id(entity.getId())
                .nom(entity.getNom())
                .prenom(entity.getPrenom())
                .email(entity.getEmail())
                .role(entity.getRole())
                .teamId(entity.getTeamId())
                .localisation(entity.getLocalisation())
                .telephone(entity.getTelephone())
                .specialite(entity.getSpecialite())
                .competencesJson(entity.getCompetencesJson())
                .chargeActuelle(entity.getChargeActuelle())
                .dateCreation(entity.getDateCreation())
                .dateModification(entity.getDateModification())
                .actif(entity.isActif())
                .build();
    }
}
