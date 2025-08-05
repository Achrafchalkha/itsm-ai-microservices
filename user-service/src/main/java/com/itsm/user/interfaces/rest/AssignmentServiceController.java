package com.itsm.user.interfaces.rest;

import com.itsm.user.application.service.TechnicianService;
import com.itsm.user.application.service.TeamService;
import com.itsm.user.application.service.ManagerService;
import com.itsm.user.domain.repository.UtilisateurRepository;
import com.itsm.user.domain.model.Utilisateur;
import com.itsm.user.domain.model.Role;
import com.itsm.user.interfaces.dto.TechnicianResponseDTO;
import com.itsm.user.interfaces.dto.TeamResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.Optional;

/**
 * Public endpoints for assignment-service integration
 * These endpoints don't require JWT authentication for service-to-service communication
 */
@RestController
@RequestMapping("/api/public/assignment")
@RequiredArgsConstructor
@Slf4j
public class AssignmentServiceController {
    
    private final TechnicianService technicianService;
    private final TeamService teamService;
    private final ManagerService managerService;
    private final UtilisateurRepository utilisateurRepository;
    
    /**
     * Get all active teams with their categories
     * Used by assignment-service to filter teams by ticket category
     */
    @GetMapping("/teams")
    public ResponseEntity<List<TeamResponseDTO>> getAllActiveTeams() {
        log.info("Assignment-service requesting all active teams");
        
        try {
            List<TeamResponseDTO> teams = teamService.getAllActiveTeams();
            log.debug("Found {} active teams for assignment", teams.size());
            return ResponseEntity.ok(teams);
            
        } catch (Exception e) {
            log.error("Error retrieving teams for assignment-service: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get teams by category
     * Used by assignment-service to find teams that handle specific ticket categories
     */
    @GetMapping("/teams/category/{category}")
    public ResponseEntity<List<TeamResponseDTO>> getTeamsByCategory(@PathVariable String category) {
        log.info("Assignment-service requesting teams for category: {}", category);
        
        try {
            List<TeamResponseDTO> teams = teamService.getTeamsByCategory(category);
            log.debug("Found {} teams for category: {}", teams.size(), category);
            return ResponseEntity.ok(teams);
            
        } catch (Exception e) {
            log.error("Error retrieving teams by category {} for assignment-service: {}", category, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get all active technicians
     * Used by assignment-service to get available technicians
     */
    @GetMapping("/technicians")
    public ResponseEntity<List<TechnicianResponseDTO>> getAllActiveTechnicians() {
        log.info("Assignment-service requesting all active technicians");
        
        try {
            List<TechnicianResponseDTO> technicians = technicianService.getAllActiveTechnicians();
            log.debug("Found {} active technicians for assignment", technicians.size());
            return ResponseEntity.ok(technicians);
            
        } catch (Exception e) {
            log.error("Error retrieving technicians for assignment-service: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get active technicians by team IDs
     * Used by assignment-service to get technicians from specific teams
     */
    @PostMapping("/technicians/by-teams")
    public ResponseEntity<List<TechnicianResponseDTO>> getTechniciansByTeams(@RequestBody List<UUID> teamIds) {
        log.info("Assignment-service requesting technicians for teams: {}", teamIds);
        
        try {
            List<TechnicianResponseDTO> technicians = technicianService.getTechniciansByTeams(teamIds);
            log.debug("Found {} technicians for teams: {}", technicians.size(), teamIds);
            return ResponseEntity.ok(technicians);
            
        } catch (Exception e) {
            log.error("Error retrieving technicians by teams for assignment-service: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get technicians by category (through their team's category)
     * Used by assignment-service for category-based filtering
     */
    @GetMapping("/technicians/category/{category}")
    public ResponseEntity<List<TechnicianResponseDTO>> getTechniciansByCategory(@PathVariable String category) {
        log.info("Assignment-service requesting technicians for category: {}", category);

        try {
            List<TechnicianResponseDTO> technicians = technicianService.getTechniciansByCategory(category);
            log.debug("Found {} technicians for category: {}", technicians.size(), category);
            return ResponseEntity.ok(technicians);

        } catch (Exception e) {
            log.error("Error retrieving technicians by category {} for assignment-service: {}", category, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get technicians who have competences in a specific category
     * Used by assignment-service to find technicians based on their actual competences
     */
    @GetMapping("/technicians/competence-category/{category}")
    public ResponseEntity<List<TechnicianResponseDTO>> getTechniciansByCompetenceCategory(@PathVariable String category) {
        log.info("Assignment-service requesting technicians with competences in category: {}", category);

        try {
            List<TechnicianResponseDTO> technicians = technicianService.getTechniciansByCompetenceCategory(category);
            log.debug("Found {} technicians with competences in category: {}", technicians.size(), category);
            return ResponseEntity.ok(technicians);

        } catch (Exception e) {
            log.error("Error getting technicians by competence category {} by assignment-service: {}", category, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get technician by ID
     * Used by assignment-service to get specific technician details
     */
    @GetMapping("/technicians/{technicianId}")
    public ResponseEntity<TechnicianResponseDTO> getTechnicianById(@PathVariable UUID technicianId) {
        log.info("Assignment-service requesting technician: {}", technicianId);
        
        try {
            TechnicianResponseDTO technician = technicianService.getTechnicianDTOById(technicianId);
            return ResponseEntity.ok(technician);
            
        } catch (Exception e) {
            log.error("Error retrieving technician {} for assignment-service: {}", technicianId, e.getMessage(), e);
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Update technician workload
     * Used by assignment-service to increment/decrement workload after assignment
     */
    @PutMapping("/technicians/{technicianId}/workload")
    public ResponseEntity<Void> updateTechnicianWorkload(
            @PathVariable UUID technicianId, 
            @RequestParam int increment) {
        
        log.info("Assignment-service updating workload for technician {} by {}", technicianId, increment);
        
        try {
            technicianService.updateWorkload(technicianId, increment);
            log.debug("Successfully updated workload for technician: {}", technicianId);
            return ResponseEntity.ok().build();
            
        } catch (Exception e) {
            log.error("Error updating workload for technician {} by assignment-service: {}", technicianId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get team by ID
     * Used by assignment-service to get team details
     */
    @GetMapping("/teams/{teamId}")
    public ResponseEntity<TeamResponseDTO> getTeamById(@PathVariable UUID teamId) {
        log.info("Assignment-service requesting team: {}", teamId);

        try {
            TeamResponseDTO team = teamService.getTeamByIdForAssignment(teamId);
            return ResponseEntity.ok(team);

        } catch (Exception e) {
            log.error("Error retrieving team {} for assignment-service: {}", teamId, e.getMessage(), e);
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Get manager's team ID by manager ID (simple approach)
     * Used by ticket-service to get manager's team for filtering tickets
     * Directly queries utilisateurs table with manager ID to get team_id
     */
    @GetMapping("/managers/{managerId}/team")
    public ResponseEntity<UUID> getManagerTeamId(@PathVariable UUID managerId) {
        log.info("Ticket-service requesting team ID for manager: {}", managerId);

        try {
            // Simple approach: get manager by ID from utilisateurs table and return team_id
            Optional<Utilisateur> managerOpt = utilisateurRepository.findById(managerId);
            if (managerOpt.isEmpty()) {
                log.warn("Manager not found in utilisateurs table: {}", managerId);
                return ResponseEntity.notFound().build();
            }

            Utilisateur manager = managerOpt.get();
            UUID teamId = manager.getTeamId();

            if (teamId == null) {
                log.warn("No team assigned to manager: {}", managerId);
                return ResponseEntity.notFound().build();
            }

            log.info("Found team {} for manager {}", teamId, managerId);
            return ResponseEntity.ok(teamId);

        } catch (Exception e) {
            log.error("Error retrieving team for manager {} by ticket-service: {}", managerId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get technician basic info by technician ID (simple approach)
     * Used by ticket-service to get technician details for ticket display
     * Directly queries utilisateurs table with technician ID
     */
    @GetMapping("/technicians/{technicianId}/info")
    public ResponseEntity<TechnicianBasicInfo> getTechnicianBasicInfo(@PathVariable UUID technicianId) {
        log.info("Ticket-service requesting basic info for technician: {}", technicianId);

        try {
            // Simple approach: get technician by ID from utilisateurs table
            Optional<Utilisateur> technicianOpt = utilisateurRepository.findById(technicianId);
            if (technicianOpt.isEmpty()) {
                log.warn("Technician not found in utilisateurs table: {}", technicianId);
                return ResponseEntity.notFound().build();
            }

            Utilisateur technician = technicianOpt.get();

            // Create basic info response
            TechnicianBasicInfo basicInfo = new TechnicianBasicInfo(
                technician.getId(),
                technician.getNom(),
                technician.getPrenom(),
                technician.getEmail(),
                technician.getSpecialite(),
                technician.getTeamId()
            );

            log.info("Found technician info for {}: {} {}", technicianId, technician.getPrenom(), technician.getNom());
            return ResponseEntity.ok(basicInfo);

        } catch (Exception e) {
            log.error("Error retrieving technician info for {} by ticket-service: {}", technicianId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Basic technician info DTO for ticket display
     */
    public static class TechnicianBasicInfo {
        private UUID id;
        private String nom;
        private String prenom;
        private String email;
        private String specialite;
        private UUID teamId;

        public TechnicianBasicInfo(UUID id, String nom, String prenom, String email, String specialite, UUID teamId) {
            this.id = id;
            this.nom = nom;
            this.prenom = prenom;
            this.email = email;
            this.specialite = specialite;
            this.teamId = teamId;
        }

        // Getters
        public UUID getId() { return id; }
        public String getNom() { return nom; }
        public String getPrenom() { return prenom; }
        public String getEmail() { return email; }
        public String getSpecialite() { return specialite; }
        public UUID getTeamId() { return teamId; }
    }

    /**
     * Get team manager information by team ID
     * Used by notifications-service to get manager for team notifications
     */
    @GetMapping("/teams/{teamId}/manager")
    public ResponseEntity<TeamManagerInfo> getTeamManager(@PathVariable UUID teamId) {
        log.info("🔔 MANAGER REQUEST: Notifications-service requesting manager info for team: {}", teamId);

        try {
            // Find manager by team ID
            log.info("🔍 SEARCHING: Looking for managers with teamId={} and role=MANAGER", teamId);
            List<Utilisateur> managers = utilisateurRepository.findByTeamIdAndRole(teamId, Role.MANAGER);
            log.info("🔍 SEARCH RESULT: Found {} managers for team {}", managers.size(), teamId);

            if (managers.isEmpty()) {
                log.warn("❌ NO MANAGER: No manager found for team: {}", teamId);
                return ResponseEntity.notFound().build();
            }

            Utilisateur manager = managers.get(0); // Get the first manager
            log.info("✅ MANAGER SELECTED: Using manager {} ({} {}) for team {}",
                    manager.getId(), manager.getPrenom(), manager.getNom(), teamId);

            // Create manager info response
            TeamManagerInfo managerInfo = new TeamManagerInfo(
                manager.getId(),
                manager.getPrenom() + " " + manager.getNom(),
                manager.getEmail()
            );

            log.info("✅ MANAGER RESPONSE: Returning manager {} ({}) for team {}",
                    manager.getId(), managerInfo.getManagerName(), teamId);
            return ResponseEntity.ok(managerInfo);

        } catch (Exception e) {
            log.error("❌ MANAGER ERROR: Error retrieving manager for team {}: {}", teamId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Team manager info DTO
     */
    public static class TeamManagerInfo {
        private UUID managerId;
        private String managerName;
        private String managerEmail;

        public TeamManagerInfo() {}

        public TeamManagerInfo(UUID managerId, String managerName, String managerEmail) {
            this.managerId = managerId;
            this.managerName = managerName;
            this.managerEmail = managerEmail;
        }

        public UUID getManagerId() {
            return managerId;
        }

        public void setManagerId(UUID managerId) {
            this.managerId = managerId;
        }

        public String getManagerName() {
            return managerName;
        }

        public void setManagerName(String managerName) {
            this.managerName = managerName;
        }

        public String getManagerEmail() {
            return managerEmail;
        }

        public void setManagerEmail(String managerEmail) {
            this.managerEmail = managerEmail;
        }
    }

    /**
     * Health check endpoint for assignment-service
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("User-Service assignment endpoints are healthy");
    }
}
