package com.itsm.user.application.service;

import com.itsm.user.domain.model.Role;
import com.itsm.user.domain.model.Team;
import com.itsm.user.domain.model.User;
import com.itsm.user.domain.repository.TeamRepository;
import com.itsm.user.domain.repository.UserRepository;
import com.itsm.user.infrastructure.kafka.TeamCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Application service for Team management
 * Handles team creation, member assignment, and category management
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TeamService {
    
    private final TeamRepository teamRepository;
    private final UserRepository userRepository;

    /**
     * Create team profile from auth service event
     */
    @Transactional
    public Team creerEquipeDepuisAuth(TeamCreatedEvent event) {
        log.info("Creating team profile from auth service for teamId: {}, teamName: {}",
                event.getTeamId(), event.getTeamName());

        // Check if team already exists
        if (teamRepository.existsById(event.getTeamId())) {
            log.warn("Team profile already exists for teamId: {}", event.getTeamId());
            return teamRepository.findById(event.getTeamId()).orElseThrow();
        }

        // Create team with the same ID as in auth-service
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

        // Update manager's team assignment
        try {
            User manager = userRepository.findById(event.getManagerId()).orElse(null);
            if (manager != null) {
                manager.assignerEquipe(savedTeam.getId());
                userRepository.save(manager);
                log.info("Manager {} assigned to team {}", manager.getEmail(), savedTeam.getNom());
            }
        } catch (Exception e) {
            log.warn("Could not assign manager to team: {}", e.getMessage());
        }

        return savedTeam;
    }
    
    /**
     * Create a new team (only ADMIN can create teams and assign managers)
     */
    public Team creerEquipe(String nom, String description, UUID managerId) {
        log.info("Creating team: {} with manager: {}", nom, managerId);
        
        // Verify manager exists and has MANAGER role
        User manager = userRepository.findById(managerId)
                .orElseThrow(() -> new IllegalArgumentException("Manager non trouvé: " + managerId));
        
        if (manager.getRole() != Role.MANAGER) {
            throw new IllegalStateException("L'utilisateur doit avoir le rôle MANAGER pour gérer une équipe");
        }
        
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
     * Get all teams
     */
    @Transactional(readOnly = true)
    public List<Team> obtenirToutesLesEquipes() {
        log.debug("Getting all teams");
        return teamRepository.findAll();
    }
    
    /**
     * Get teams managed by a specific manager
     */
    @Transactional(readOnly = true)
    public List<Team> obtenirEquipesParManager(UUID managerId) {
        log.debug("Getting teams for manager: {}", managerId);
        return teamRepository.findByManagerId(managerId);
    }
    
    /**
     * Add a member to a team (only team manager can do this)
     */
    public Team ajouterMembreEquipe(UUID teamId, UUID membreId, UUID requesterId) {
        log.info("Adding member {} to team {} by requester {}", membreId, teamId, requesterId);
        
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("Équipe non trouvée: " + teamId));
        
        // Verify requester is the team manager
        if (!team.getManagerId().equals(requesterId)) {
            throw new IllegalStateException("Seul le manager de l'équipe peut ajouter des membres");
        }
        
        // Verify member exists and is a technician
        User membre = userRepository.findById(membreId)
                .orElseThrow(() -> new IllegalArgumentException("Membre non trouvé: " + membreId));
        
        if (membre.getRole() != Role.TECHNICIEN) {
            throw new IllegalStateException("Seuls les techniciens peuvent être ajoutés à une équipe");
        }
        
        // Add member to team
        team.ajouterMembre(membreId);
        
        // Update user's team assignment
        membre.assignerEquipe(teamId);
        membre.assignerManager(team.getManagerId());
        userRepository.save(membre);
        
        return teamRepository.save(team);
    }
    
    /**
     * Remove a member from a team
     */
    public Team supprimerMembreEquipe(UUID teamId, UUID membreId, UUID requesterId) {
        log.info("Removing member {} from team {} by requester {}", membreId, teamId, requesterId);
        
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("Équipe non trouvée: " + teamId));
        
        // Verify requester is the team manager
        if (!team.getManagerId().equals(requesterId)) {
            throw new IllegalStateException("Seul le manager de l'équipe peut supprimer des membres");
        }
        
        // Remove member from team
        team.supprimerMembre(membreId);
        
        // Update user's team assignment
        User membre = userRepository.findById(membreId).orElse(null);
        if (membre != null) {
            membre.assignerEquipe(null);
            membre.assignerManager(null);
            userRepository.save(membre);
        }
        
        return teamRepository.save(team);
    }
    
    /**
     * Add a category to a team
     */
    public Team ajouterCategorieEquipe(UUID teamId, String categorie, UUID requesterId) {
        log.info("Adding category {} to team {} by requester {}", categorie, teamId, requesterId);
        
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("Équipe non trouvée: " + teamId));
        
        // Verify requester is the team manager
        if (!team.getManagerId().equals(requesterId)) {
            throw new IllegalStateException("Seul le manager de l'équipe peut gérer les catégories");
        }
        
        team.ajouterCategorie(categorie);
        return teamRepository.save(team);
    }
    
    /**
     * Remove a category from a team
     */
    public Team supprimerCategorieEquipe(UUID teamId, String categorie, UUID requesterId) {
        log.info("Removing category {} from team {} by requester {}", categorie, teamId, requesterId);
        
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("Équipe non trouvée: " + teamId));
        
        // Verify requester is the team manager
        if (!team.getManagerId().equals(requesterId)) {
            throw new IllegalStateException("Seul le manager de l'équipe peut gérer les catégories");
        }
        
        team.supprimerCategorie(categorie);
        return teamRepository.save(team);
    }
    
    /**
     * Find teams that can handle a specific ticket category
     */
    @Transactional(readOnly = true)
    public List<Team> trouverEquipesPourCategorie(String categorie) {
        log.debug("Finding teams for category: {}", categorie);
        return teamRepository.findByCategory(categorie);
    }
    
    /**
     * Get team members
     */
    @Transactional(readOnly = true)
    public List<User> obtenirMembresEquipe(UUID teamId) {
        log.debug("Getting members for team: {}", teamId);
        return userRepository.findTechniciensByEquipe(teamId);
    }

    /**
     * Get the team ID managed by a specific manager (returns first team found)
     * Used for automatic technician assignment
     */
    @Transactional(readOnly = true)
    public UUID obtenirEquipeParManager(UUID managerId) {
        log.debug("Getting team ID for manager: {}", managerId);
        List<Team> teams = teamRepository.findByManagerId(managerId);

        if (teams.isEmpty()) {
            log.warn("No team found for manager: {}", managerId);
            return null;
        }

        if (teams.size() > 1) {
            log.warn("Multiple teams found for manager: {}. Using first team: {}",
                    managerId, teams.get(0).getId());
        }

        return teams.get(0).getId();
    }
}
