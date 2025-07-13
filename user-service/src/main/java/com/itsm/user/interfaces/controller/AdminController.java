package com.itsm.user.interfaces.controller;

import com.itsm.user.application.service.UserService;
import com.itsm.user.domain.model.Role;
import com.itsm.user.domain.model.User;
import com.itsm.user.interfaces.dto.UserProfileResponse;
import com.itsm.user.interfaces.mapper.UserDtoMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Admin controller for debugging and data management
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminController {
    
    private final UserService userService;
    private final UserDtoMapper userDtoMapper;
    
    /**
     * Debug endpoint to list all users (no authorization for debugging)
     */
    @GetMapping("/debug/users")
    public ResponseEntity<List<UserProfileResponse>> getAllUsers() {
        log.info("Debug: Getting all users");
        
        List<User> users = userService.obtenirTousLesUtilisateurs();
        List<UserProfileResponse> responses = users.stream()
                .map(userDtoMapper::toResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(responses);
    }
    
    /**
     * Debug endpoint to list all technicians (no authorization for debugging)
     */
    @GetMapping("/debug/technicians")
    public ResponseEntity<List<UserProfileResponse>> getAllTechnicians() {
        log.info("Debug: Getting all technicians");
        
        List<User> technicians = userService.obtenirUtilisateursParRole(Role.TECHNICIEN);
        List<UserProfileResponse> responses = technicians.stream()
                .map(userDtoMapper::toResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(responses);
    }
    
    /**
     * Debug endpoint to check service health
     */
    @GetMapping("/debug/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("User service is running - " + java.time.LocalDateTime.now());
    }

    /**
     * Remove all technicians from a team (for testing/cleanup)
     */
    @DeleteMapping("/debug/teams/{teamId}/technicians")
    public ResponseEntity<String> removeAllTechniciansFromTeam(@PathVariable UUID teamId) {
        log.info("Debug: Removing all technicians from team {}", teamId);

        try {
            List<User> technicians = userService.obtenirUtilisateursParRole(Role.TECHNICIEN);
            int removedCount = 0;

            for (User tech : technicians) {
                if (teamId.equals(tech.getTeamId())) {
                    // Remove team assignment
                    userService.assignerEquipe(tech.getId(), null);
                    removedCount++;
                    log.info("Removed technician {} from team {}", tech.getId(), teamId);
                }
            }

            return ResponseEntity.ok("Removed " + removedCount + " technicians from team " + teamId);

        } catch (Exception e) {
            log.error("Error removing technicians from team: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    /**
     * Delete a technician completely (for testing/cleanup)
     */
    @DeleteMapping("/debug/technicians/{technicianId}")
    public ResponseEntity<String> deleteTechnician(@PathVariable UUID technicianId) {
        log.info("Debug: Deleting technician {}", technicianId);

        try {
            userService.supprimerUtilisateur(technicianId);
            return ResponseEntity.ok("Deleted technician " + technicianId);

        } catch (Exception e) {
            log.error("Error deleting technician: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    /**
     * Manually assign technician to team (for testing/fixing)
     */
    @PutMapping("/debug/technicians/{technicianId}/assign-team/{teamId}")
    public ResponseEntity<String> assignTechnicianToTeam(
            @PathVariable UUID technicianId,
            @PathVariable UUID teamId) {
        log.info("Debug: Manually assigning technician {} to team {}", technicianId, teamId);

        try {
            userService.assignerEquipe(technicianId, teamId);
            return ResponseEntity.ok("Assigned technician " + technicianId + " to team " + teamId);

        } catch (Exception e) {
            log.error("Error assigning technician to team: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
}
