package com.itsm.user.interfaces.controller;

import com.itsm.user.application.service.TechnicianAssignmentService;
import com.itsm.user.domain.model.StatutTechnicien;
import com.itsm.user.domain.model.User;
import com.itsm.user.interfaces.dto.TechnicianSearchRequest;
import com.itsm.user.interfaces.dto.UserProfileResponse;
import com.itsm.user.interfaces.mapper.UserDtoMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * REST Controller for Technician queries and assignment
 * Provides endpoints for finding available technicians based on various criteria
 */
@RestController
@RequestMapping("/api/technicians")
@RequiredArgsConstructor
@Slf4j
public class TechnicianController {
    
    private final TechnicianAssignmentService technicianAssignmentService;
    private final UserDtoMapper userDtoMapper;
    
    /**
     * Get available technicians
     * Used by ticket-service for intelligent assignment
     */
    @GetMapping("/available")
    @PreAuthorize("hasRole('MANAGER') or hasRole('TECHNICIEN')")
    public ResponseEntity<List<UserProfileResponse>> getAvailableTechnicians(
            @RequestParam(required = false) String skill,
            @RequestParam(required = false, defaultValue = "1") int level,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) UUID teamId) {
        
        log.info("Getting available technicians - skill: {}, level: {}, location: {}, team: {}", 
                skill, level, location, teamId);
        
        List<User> technicians;
        
        if (skill != null) {
            technicians = technicianAssignmentService.trouverTechniciensDisponiblesAvecCompetence(skill, level);
        } else if (location != null) {
            technicians = technicianAssignmentService.trouverTechniciensDisponiblesDansLocalisation(location);
        } else if (teamId != null) {
            technicians = technicianAssignmentService.trouverTechniciensDisponiblesDansEquipe(teamId);
        } else {
            // Get all available technicians
            technicians = technicianAssignmentService.trouverTechniciensDisponiblesAvecCompetence(null, 0);
        }
        
        List<UserProfileResponse> responses = technicians.stream()
                .map(userDtoMapper::toResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(responses);
    }
    
    /**
     * Search technicians with multiple criteria
     */
    @PostMapping("/search")
    @PreAuthorize("hasRole('MANAGER') or hasRole('TECHNICIEN')")
    public ResponseEntity<List<UserProfileResponse>> searchTechnicians(
            @RequestBody TechnicianSearchRequest request) {
        
        log.info("Searching technicians with criteria: {}", request);
        
        TechnicianAssignmentService.TechnicianSearchCriteria criteria = 
                TechnicianAssignmentService.TechnicianSearchCriteria.builder()
                        .competenceRequise(request.getCompetence())
                        .niveauMinimum(request.getNiveauMinimumOrDefault())
                        .localisation(request.getLocalisation())
                        .equipeId(request.getEquipeId())
                        .statut(request.getStatutOrDefault())
                        .build();
        
        List<User> technicians = technicianAssignmentService.rechercherTechniciens(criteria);
        
        List<UserProfileResponse> responses = technicians.stream()
                .map(userDtoMapper::toResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(responses);
    }
    
    /**
     * Find best technician for assignment
     * Used by assignment-service for intelligent routing
     */
    @GetMapping("/best-match")
    @PreAuthorize("hasRole('MANAGER') or hasRole('TECHNICIEN')")
    public ResponseEntity<UserProfileResponse> findBestTechnician(
            @RequestParam String skill,
            @RequestParam(defaultValue = "1") int level,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) UUID teamId) {
        
        log.info("Finding best technician - skill: {}, level: {}, location: {}, team: {}", 
                skill, level, location, teamId);
        
        Optional<User> bestTechnician = technicianAssignmentService.trouverMeilleurTechnicienPourAssignation(
                skill, level, location, teamId);
        
        if (bestTechnician.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        UserProfileResponse response = userDtoMapper.toResponse(bestTechnician.get());
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get technicians by status
     */
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<List<UserProfileResponse>> getTechniciansByStatus(
            @PathVariable StatutTechnicien status) {
        
        log.info("Getting technicians by status: {}", status);
        
        List<User> technicians = technicianAssignmentService.trouverTechniciensDisponiblesAvecCompetence(null, 0)
                .stream()
                .filter(t -> t.getStatutTechnicien() == status)
                .collect(Collectors.toList());
        
        List<UserProfileResponse> responses = technicians.stream()
                .map(userDtoMapper::toResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(responses);
    }
    
    /**
     * Get technicians by location
     */
    @GetMapping("/location/{location}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<List<UserProfileResponse>> getTechniciansByLocation(
            @PathVariable String location) {
        
        log.info("Getting technicians by location: {}", location);
        
        List<User> technicians = technicianAssignmentService.trouverTechniciensDisponiblesDansLocalisation(location);
        
        List<UserProfileResponse> responses = technicians.stream()
                .map(userDtoMapper::toResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(responses);
    }
    
    /**
     * Get technicians by team
     */
    @GetMapping("/team/{teamId}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<List<UserProfileResponse>> getTechniciansByTeam(
            @PathVariable UUID teamId) {
        
        log.info("Getting technicians by team: {}", teamId);
        
        List<User> technicians = technicianAssignmentService.trouverTechniciensDisponiblesDansEquipe(teamId);
        
        List<UserProfileResponse> responses = technicians.stream()
                .map(userDtoMapper::toResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(responses);
    }
    
    /**
     * Get technician availability statistics
     */
    @GetMapping("/stats/availability")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<TechnicianAssignmentService.TechnicianAvailabilityStats> getAvailabilityStats() {
        log.info("Getting technician availability statistics");
        
        TechnicianAssignmentService.TechnicianAvailabilityStats stats = 
                technicianAssignmentService.obtenirStatistiquesDisponibilite();
        
        return ResponseEntity.ok(stats);
    }
}
