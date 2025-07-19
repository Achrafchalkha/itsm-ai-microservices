package com.itsm.user.application.service;

import com.itsm.user.domain.model.Team;
import com.itsm.user.domain.repository.TeamRepository;
import com.itsm.user.infrastructure.kafka.TeamCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for Team operations
 * Handles team management and business logic
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TeamService {
    
    private final TeamRepository teamRepository;

    /**
     * Create team profile from auth service event
     */
    @Transactional
    public Team creerEquipeDepuisAuth(TeamCreatedEvent event) {
        log.info("Creating team profile from auth service for teamId: {}, teamName: {}",
                event.getTeamId(), event.getTeamName());

        if (teamRepository.existsById(event.getTeamId())) {
            log.warn("Team profile already exists for teamId: {}", event.getTeamId());
            return teamRepository.findById(event.getTeamId()).orElseThrow();
        }

        Team team = Team.builder()
                .id(event.getTeamId())
                .nom(event.getTeamName())
                .description(event.getTeamDescription())
                .managerId(event.getManagerId())
                .dateCreation(event.getDateCreation())
                .dateModification(event.getDateCreation())
                .actif(true)
                .build();

        Team savedTeam = teamRepository.save(team);
        log.info("Team profile created successfully for teamId: {}, teamName: {}",
                savedTeam.getId(), savedTeam.getNom());

        return savedTeam;
    }
    
    /**
     * Create a new team
     */
    @Transactional
    public Team creerEquipe(String nom, String description, UUID managerId) {
        log.info("Creating team: {} with manager: {}", nom, managerId);
        
        Team team = Team.creerEquipe(nom, description, managerId);
        return teamRepository.save(team);
    }
    
    /**
     * Get team by ID
     */
    @Transactional(readOnly = true)
    public Optional<Team> obtenirEquipe(UUID teamId) {
        log.debug("Getting team: {}", teamId);
        return teamRepository.findById(teamId);
    }

    /**
     * Update existing team (does NOT create new team)
     */
    @Transactional
    public Team mettreAJourEquipe(Team team) {
        log.info("Updating existing team: {} (ID: {})", team.getNom(), team.getId());

        // Ensure the team exists
        if (!teamRepository.existsById(team.getId())) {
            throw new IllegalArgumentException("Equipe non trouvee pour mise à jour: " + team.getId());
        }

        // Update modification timestamp
        team.mettreAJour();

        // Save will update existing entity due to TeamRepositoryImpl logic
        Team updatedTeam = teamRepository.save(team);
        log.info("Team updated successfully: {} (ID: {})", updatedTeam.getNom(), updatedTeam.getId());

        return updatedTeam;
    }
    
    /**
     * Get all teams
     */
    @Transactional(readOnly = true)
    public List<Team> obtenirToutesLesEquipes() {
        log.debug("Getting all teams");
        return teamRepository.findAll();
    }
    
    /**
     * Get teams by manager
     */
    @Transactional(readOnly = true)
    public List<Team> obtenirEquipesParManager(UUID managerId) {
        log.debug("Getting teams for manager: {}", managerId);
        return teamRepository.findByManagerId(managerId);
    }
    
    /**
     * Add category to team
     */
    @Transactional
    public Team ajouterCategorieEquipe(UUID teamId, String categorie) {
        log.info("Adding category {} to team {}", categorie, teamId);
        
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("Equipe non trouvee: " + teamId));
        
        team.ajouterCategorie(categorie);
        return teamRepository.save(team);
    }
    
    /**
     * Remove category from team
     */
    @Transactional
    public Team supprimerCategorieEquipe(UUID teamId, String categorie) {
        log.info("Removing category {} from team {}", categorie, teamId);
        
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("Equipe non trouvee: " + teamId));
        
        team.supprimerCategorie(categorie);
        return teamRepository.save(team);
    }
    
    /**
     * Find teams for category
     */
    @Transactional(readOnly = true)
    public List<Team> trouverEquipesPourCategorie(String categorie) {
        log.debug("Finding teams for category: {}", categorie);
        return teamRepository.findByCategory(categorie);
    }

    /**
     * Get team ID for manager
     */
    @Transactional(readOnly = true)
    public UUID obtenirEquipeParManager(UUID managerId) {
        log.debug("Getting team ID for manager: {}", managerId);
        List<Team> teams = teamRepository.findByManagerId(managerId);

        if (teams.isEmpty()) {
            log.warn("No team found for manager: {}", managerId);
            return null;
        }

        UUID teamId = teams.get(0).getId();
        log.debug("Found team {} for manager {}", teamId, managerId);
        return teamId;
    }
}
