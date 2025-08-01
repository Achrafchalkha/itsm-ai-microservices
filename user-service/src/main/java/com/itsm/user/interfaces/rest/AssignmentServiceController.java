package com.itsm.user.interfaces.rest;

import com.itsm.user.application.service.TechnicianService;
import com.itsm.user.application.service.TeamService;
import com.itsm.user.interfaces.dto.TechnicianResponseDTO;
import com.itsm.user.interfaces.dto.TeamResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

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
     * Health check endpoint for assignment-service
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("User-Service assignment endpoints are healthy");
    }
}
