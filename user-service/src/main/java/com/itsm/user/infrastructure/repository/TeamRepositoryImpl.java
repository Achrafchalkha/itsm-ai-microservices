package com.itsm.user.infrastructure.repository;

import com.itsm.user.domain.model.Team;
import com.itsm.user.domain.repository.TeamRepository;
import com.itsm.user.infrastructure.mapper.TeamMapper;
import com.itsm.user.infrastructure.persistence.JpaTeamEntity;
import com.itsm.user.infrastructure.persistence.JpaTeamRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of TeamRepository using Spring Data JPA
 * Bridges the domain layer with the infrastructure layer
 */
@Repository
@RequiredArgsConstructor
@Slf4j
public class TeamRepositoryImpl implements TeamRepository {
    
    private final JpaTeamRepository jpaTeamRepository;
    private final TeamMapper teamMapper;
    
    @Override
    public Team save(Team team) {
        log.debug("Saving team: {}", team.getNom());
        
        JpaTeamEntity entity;
        if (jpaTeamRepository.existsById(team.getId())) {
            // Update existing entity
            entity = jpaTeamRepository.findById(team.getId()).orElseThrow();
            teamMapper.updateEntity(entity, team);
        } else {
            // Create new entity
            entity = teamMapper.toEntity(team);
        }
        
        JpaTeamEntity savedEntity = jpaTeamRepository.save(entity);
        return teamMapper.toDomain(savedEntity);
    }
    
    @Override
    public Optional<Team> findById(UUID id) {
        log.debug("Finding team by ID: {}", id);
        return jpaTeamRepository.findById(id)
                .map(teamMapper::toDomain);
    }
    
    @Override
    public List<Team> findAll() {
        log.debug("Finding all teams");
        return teamMapper.toDomainList(jpaTeamRepository.findAll());
    }
    
    @Override
    public List<Team> findByManagerId(UUID managerId) {
        log.debug("Finding teams by manager: {}", managerId);
        return teamMapper.toDomainList(jpaTeamRepository.findByManagerId(managerId));
    }
    
    @Override
    public List<Team> findByCategory(String category) {
        log.debug("Finding teams by category: {}", category);
        return teamMapper.toDomainList(jpaTeamRepository.findByCategory(category));
    }
    
    @Override
    public List<Team> findByActif(boolean actif) {
        log.debug("Finding teams by actif: {}", actif);
        return teamMapper.toDomainList(jpaTeamRepository.findByActif(actif));
    }
    
    @Override
    public boolean existsById(UUID id) {
        return jpaTeamRepository.existsById(id);
    }
    
    @Override
    public void deleteById(UUID id) {
        log.debug("Deleting team by ID: {}", id);
        jpaTeamRepository.deleteById(id);
    }
    
    @Override
    public long countByManagerId(UUID managerId) {
        return jpaTeamRepository.countByManagerId(managerId);
    }
}
