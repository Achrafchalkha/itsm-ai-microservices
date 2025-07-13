package com.itsm.user.infrastructure.mapper;

import com.itsm.user.domain.model.Team;
import com.itsm.user.infrastructure.persistence.JpaTeamEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper to convert between Team domain models and JPA entities
 * Handles the translation between domain and infrastructure layers
 */
@Component
public class TeamMapper {
    
    /**
     * Convert JPA entity to domain model
     */
    public Team toDomain(JpaTeamEntity entity) {
        if (entity == null) {
            return null;
        }
        
        return Team.builder()
                .id(entity.getId())
                .nom(entity.getNom())
                .description(entity.getDescription())
                .managerId(entity.getManagerId())
                .categories(entity.getCategories())
                .memberIds(entity.getMemberIds())
                .dateCreation(entity.getDateCreation())
                .dateModification(entity.getDateModification())
                .actif(entity.isActif())
                .build();
    }
    
    /**
     * Convert domain model to JPA entity
     */
    public JpaTeamEntity toEntity(Team team) {
        if (team == null) {
            return null;
        }
        
        return JpaTeamEntity.builder()
                .id(team.getId())
                .nom(team.getNom())
                .description(team.getDescription())
                .managerId(team.getManagerId())
                .categories(team.getCategories())
                .memberIds(team.getMemberIds())
                .dateCreation(team.getDateCreation())
                .dateModification(team.getDateModification())
                .actif(team.isActif())
                .build();
    }
    
    /**
     * Convert list of JPA entities to domain models
     */
    public List<Team> toDomainList(List<JpaTeamEntity> entities) {
        return entities.stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }
    
    /**
     * Convert list of domain models to JPA entities
     */
    public List<JpaTeamEntity> toEntityList(List<Team> teams) {
        return teams.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }
    
    /**
     * Update existing entity with domain model data
     * Useful for updates to preserve JPA managed state
     */
    public void updateEntity(JpaTeamEntity entity, Team team) {
        entity.setNom(team.getNom());
        entity.setDescription(team.getDescription());
        entity.setManagerId(team.getManagerId());
        entity.setCategories(team.getCategories());
        entity.setMemberIds(team.getMemberIds());
        entity.setActif(team.isActif());
        entity.setDateModification(team.getDateModification());
    }
}
