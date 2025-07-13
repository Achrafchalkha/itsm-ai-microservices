package com.itsm.user.infrastructure.mapper;

import com.itsm.user.domain.model.Competence;
import com.itsm.user.domain.model.User;
import com.itsm.user.infrastructure.persistence.JpaCompetenceEntity;
import com.itsm.user.infrastructure.persistence.JpaUserEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper to convert between domain models and JPA entities
 * Handles the translation between domain and infrastructure layers
 */
@Component
public class UserMapper {
    
    /**
     * Convert JPA entity to domain model
     */
    public User toDomain(JpaUserEntity entity) {
        if (entity == null) {
            return null;
        }
        
        List<Competence> competences = entity.getCompetences().stream()
                .map(this::competenceToDomain)
                .collect(Collectors.toList());
        
        return User.builder()
                .id(entity.getId())
                .nom(entity.getNom())
                .prenom(entity.getPrenom())
                .email(entity.getEmail())
                .role(entity.getRole())
                .managerId(entity.getManagerId())
                .teamId(entity.getTeamId())
                .statutTechnicien(entity.getStatutTechnicien())
                .localisation(entity.getLocalisation())
                .competences(competences)
                .dateCreation(entity.getDateCreation())
                .dateModification(entity.getDateModification())
                .actif(entity.isActif())
                .build();
    }
    
    /**
     * Convert domain model to JPA entity
     */
    public JpaUserEntity toEntity(User user) {
        if (user == null) {
            return null;
        }
        
        JpaUserEntity entity = JpaUserEntity.builder()
                .id(user.getId())
                .nom(user.getNom())
                .prenom(user.getPrenom())
                .email(user.getEmail())
                .role(user.getRole())
                .managerId(user.getManagerId())
                .teamId(user.getTeamId())
                .statutTechnicien(user.getStatutTechnicien())
                .localisation(user.getLocalisation())
                .dateCreation(user.getDateCreation())
                .dateModification(user.getDateModification())
                .actif(user.isActif())
                .build();
        
        // Map competences and set bidirectional relationship
        List<JpaCompetenceEntity> competenceEntities = user.getCompetences().stream()
                .map(competence -> competenceToEntity(competence, entity))
                .collect(Collectors.toList());
        
        entity.setCompetences(competenceEntities);
        
        return entity;
    }
    
    /**
     * Convert JPA competence entity to domain model
     */
    public Competence competenceToDomain(JpaCompetenceEntity entity) {
        if (entity == null) {
            return null;
        }

        return Competence.builder()
                .id(entity.getId())
                .nom(entity.getNom())
                .description(entity.getDescription())
                .categorie(entity.getCategorie())
                .niveau(entity.getNiveau())
                .build();
    }
    
    /**
     * Convert domain competence to JPA entity
     */
    public JpaCompetenceEntity competenceToEntity(Competence competence, JpaUserEntity userEntity) {
        if (competence == null) {
            return null;
        }

        return JpaCompetenceEntity.builder()
                .id(competence.getId())
                .nom(competence.getNom())
                .description(competence.getDescription())
                .categorie(competence.getCategorie())
                .niveau(competence.getNiveau())
                .user(userEntity)
                .build();
    }
    
    /**
     * Convert list of JPA entities to domain models
     */
    public List<User> toDomainList(List<JpaUserEntity> entities) {
        return entities.stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }
    
    /**
     * Convert list of domain models to JPA entities
     */
    public List<JpaUserEntity> toEntityList(List<User> users) {
        return users.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }
    
    /**
     * Update existing entity with domain model data
     * Useful for updates to preserve JPA managed state
     */
    public void updateEntity(JpaUserEntity entity, User user) {
        entity.setNom(user.getNom());
        entity.setPrenom(user.getPrenom());
        entity.setEmail(user.getEmail());
        entity.setRole(user.getRole());
        entity.setManagerId(user.getManagerId());
        entity.setTeamId(user.getTeamId());
        entity.setStatutTechnicien(user.getStatutTechnicien());
        entity.setLocalisation(user.getLocalisation());
        entity.setActif(user.isActif());
        entity.setDateModification(user.getDateModification());

        // Update competences
        entity.getCompetences().clear();
        List<JpaCompetenceEntity> competenceEntities = user.getCompetences().stream()
                .map(competence -> competenceToEntity(competence, entity))
                .collect(Collectors.toList());
        entity.getCompetences().addAll(competenceEntities);
    }
}
