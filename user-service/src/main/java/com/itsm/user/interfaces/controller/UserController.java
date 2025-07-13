package com.itsm.user.interfaces.controller;

import com.itsm.user.application.service.TechnicianAssignmentService;
import com.itsm.user.application.service.UserService;
import com.itsm.user.domain.model.Role;
import com.itsm.user.domain.model.StatutTechnicien;
import com.itsm.user.domain.model.User;
import com.itsm.user.interfaces.dto.*;
import com.itsm.user.interfaces.mapper.UserDtoMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * REST Controller for User management
 * Provides endpoints for user profile management and technician queries
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    
    private final UserService userService;
    private final TechnicianAssignmentService technicianAssignmentService;
    private final UserDtoMapper userDtoMapper;
    
    /**
     * Get user profile by ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER') or @userSecurityService.isCurrentUser(#id)")
    public ResponseEntity<UserProfileResponse> getUserProfile(@PathVariable UUID id) {
        log.info("Getting user profile for ID: {}", id);
        
        Optional<User> user = userService.obtenirProfilUtilisateur(id);
        if (user.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        UserProfileResponse response = userDtoMapper.toResponse(user.get());
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get current user profile (from JWT token)
     */
    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getCurrentUserProfile() {
        // This would typically extract user ID from JWT token
        // For now, we'll return a placeholder response
        log.info("Getting current user profile from JWT token");
        
        // TODO: Extract user ID from SecurityContext/JWT
        // UUID currentUserId = getCurrentUserIdFromJWT();
        // return getUserProfile(currentUserId);
        
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }
    
    /**
     * Update user profile
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER') or @userSecurityService.isCurrentUser(#id)")
    public ResponseEntity<UserProfileResponse> updateUserProfile(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUserProfileRequest request) {
        
        log.info("Updating user profile for ID: {}", id);
        
        try {
            User updatedUser = userService.mettreAJourProfil(
                    id, 
                    request.getNom(), 
                    request.getPrenom(), 
                    request.getLocalisation()
            );
            
            // Update technician status if provided and user is technician
            if (request.getStatutTechnicien() != null && updatedUser.getRole() == Role.TECHNICIEN) {
                updatedUser = userService.changerStatutTechnicien(id, request.getStatutTechnicien());
            }
            
            // Update team assignment if provided
            if (request.getEquipeId() != null) {
                updatedUser = userService.assignerEquipe(id, request.getEquipeId());
            }
            
            UserProfileResponse response = userDtoMapper.toResponse(updatedUser);
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            log.error("Error updating user profile: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Add competence to technician
     */
    @PostMapping("/{id}/competences")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<UserProfileResponse> addCompetence(
            @PathVariable UUID id,
            @Valid @RequestBody AddCompetenceRequest request) {
        
        log.info("Adding competence {} to user {}", request.getNom(), id);
        
        try {
            User updatedUser = userService.ajouterCompetence(
                    id,
                    request.getNom(),
                    request.getDescription(),
                    request.getCategorie(),
                    request.getNiveau()
            );
            
            UserProfileResponse response = userDtoMapper.toResponse(updatedUser);
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            log.error("Error adding competence: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (IllegalStateException e) {
            log.error("Error adding competence: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Remove competence from technician
     */
    @DeleteMapping("/{id}/competences/{competenceId}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<UserProfileResponse> removeCompetence(
            @PathVariable UUID id,
            @PathVariable UUID competenceId) {
        
        log.info("Removing competence {} from user {}", competenceId, id);
        
        try {
            User updatedUser = userService.supprimerCompetence(id, competenceId);
            UserProfileResponse response = userDtoMapper.toResponse(updatedUser);
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            log.error("Error removing competence: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Get users by role
     */
    @GetMapping("/role/{role}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<List<UserProfileResponse>> getUsersByRole(@PathVariable Role role) {
        log.info("Getting users by role: {}", role);
        
        List<User> users = userService.obtenirUtilisateursParRole(role);
        List<UserProfileResponse> responses = users.stream()
                .map(userDtoMapper::toResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(responses);
    }
    
    /**
     * Change technician status
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('MANAGER') or @userSecurityService.isCurrentUser(#id)")
    public ResponseEntity<UserProfileResponse> changeStatus(
            @PathVariable UUID id,
            @RequestParam StatutTechnicien status) {
        
        log.info("Changing status for user {} to {}", id, status);
        
        try {
            User updatedUser = userService.changerStatutTechnicien(id, status);
            UserProfileResponse response = userDtoMapper.toResponse(updatedUser);
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            log.error("Error changing status: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            log.error("Error changing status: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Assign user to team
     */
    @PutMapping("/{id}/team")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<UserProfileResponse> assignToTeam(
            @PathVariable UUID id,
            @RequestParam UUID teamId) {
        
        log.info("Assigning user {} to team {}", id, teamId);
        
        try {
            User updatedUser = userService.assignerEquipe(id, teamId);
            UserProfileResponse response = userDtoMapper.toResponse(updatedUser);
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            log.error("Error assigning to team: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
}
