package com.itsm.analytics.presentation.controller;

import com.itsm.analytics.application.service.SLAConfigurationService;
import com.itsm.analytics.domain.model.SLAConfiguration;
import com.itsm.analytics.presentation.dto.SLAConfigurationDTO;
import com.itsm.analytics.presentation.dto.CreateSLAConfigurationRequest;
import com.itsm.analytics.presentation.dto.UpdateSLAConfigurationRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * REST Controller for SLA Configuration management
 * Only accessible by ADMIN role
 */
@RestController
@RequestMapping("/api/analytics/sla-configurations")
@RequiredArgsConstructor
@Slf4j
public class SLAConfigurationController {
    
    private final SLAConfigurationService slaConfigurationService;
    
    /**
     * Create new SLA configuration
     * Only ADMIN can create SLA configurations
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SLAConfigurationDTO> createSLAConfiguration(
            @Valid @RequestBody CreateSLAConfigurationRequest request,
            Authentication authentication) {
        
        log.info("Creating SLA configuration for category: {}, priority: {}", 
                request.getCategorie(), request.getPriorite());
        
        try {
            UUID adminId = extractUserIdFromAuth(authentication);
            
            SLAConfiguration config = slaConfigurationService.creerConfiguration(
                    request.getCategorie(),
                    request.getPriorite(),
                    request.getDelaiPremiereReponseHeures(),
                    request.getDelaiResolutionHeures(),
                    request.getEscaladeManagerHeures(),
                    request.getEscaladeAdminHeures(),
                    adminId
            );
            
            SLAConfigurationDTO response = convertToDTO(config);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (IllegalArgumentException e) {
            log.warn("Invalid SLA configuration request: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error creating SLA configuration: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Update existing SLA configuration
     * Only ADMIN can update SLA configurations
     */
    @PutMapping("/{configId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SLAConfigurationDTO> updateSLAConfiguration(
            @PathVariable UUID configId,
            @Valid @RequestBody UpdateSLAConfigurationRequest request) {
        
        log.info("Updating SLA configuration: {}", configId);
        
        try {
            SLAConfiguration config = slaConfigurationService.mettreAJourConfiguration(
                    configId,
                    request.getDelaiPremiereReponseHeures(),
                    request.getDelaiResolutionHeures(),
                    request.getEscaladeManagerHeures(),
                    request.getEscaladeAdminHeures()
            );
            
            SLAConfigurationDTO response = convertToDTO(config);
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            log.warn("Invalid SLA configuration update: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error updating SLA configuration: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get all SLA configurations
     * ADMIN can see all, MANAGER can see active ones
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<List<SLAConfigurationDTO>> getAllSLAConfigurations(
            @RequestParam(defaultValue = "true") boolean activeOnly,
            Authentication authentication) {
        
        log.debug("Getting SLA configurations, activeOnly: {}", activeOnly);
        
        try {
            List<SLAConfiguration> configs;
            
            // ADMIN can see all configurations, MANAGER only active ones
            if (hasRole(authentication, "ADMIN") && !activeOnly) {
                configs = slaConfigurationService.getAllConfigurations();
            } else {
                configs = slaConfigurationService.getAllActiveConfigurations();
            }
            
            List<SLAConfigurationDTO> response = configs.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error getting SLA configurations: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get SLA configurations by category
     */
    @GetMapping("/category/{categorie}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<List<SLAConfigurationDTO>> getSLAConfigurationsByCategory(
            @PathVariable String categorie) {
        
        log.debug("Getting SLA configurations for category: {}", categorie);
        
        try {
            List<SLAConfiguration> configs = slaConfigurationService.getConfigurationsByCategory(categorie);
            
            List<SLAConfigurationDTO> response = configs.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error getting SLA configurations for category {}: {}", categorie, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get specific SLA configuration
     */
    @GetMapping("/{configId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<SLAConfigurationDTO> getSLAConfiguration(@PathVariable UUID configId) {
        
        log.debug("Getting SLA configuration: {}", configId);
        
        try {
            // Note: This would need to be implemented in the service
            // For now, we'll return not found
            return ResponseEntity.notFound().build();
            
        } catch (Exception e) {
            log.error("Error getting SLA configuration {}: {}", configId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Activate SLA configuration
     * Only ADMIN can activate/deactivate configurations
     */
    @PutMapping("/{configId}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SLAConfigurationDTO> activateSLAConfiguration(@PathVariable UUID configId) {
        
        log.info("Activating SLA configuration: {}", configId);
        
        try {
            SLAConfiguration config = slaConfigurationService.activerConfiguration(configId);
            SLAConfigurationDTO response = convertToDTO(config);
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            log.warn("SLA configuration not found: {}", configId);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error activating SLA configuration {}: {}", configId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Deactivate SLA configuration
     * Only ADMIN can activate/deactivate configurations
     */
    @PutMapping("/{configId}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SLAConfigurationDTO> deactivateSLAConfiguration(@PathVariable UUID configId) {
        
        log.info("Deactivating SLA configuration: {}", configId);
        
        try {
            SLAConfiguration config = slaConfigurationService.desactiverConfiguration(configId);
            SLAConfigurationDTO response = convertToDTO(config);
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            log.warn("SLA configuration not found: {}", configId);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error deactivating SLA configuration {}: {}", configId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Convert domain model to DTO
     */
    private SLAConfigurationDTO convertToDTO(SLAConfiguration config) {
        return SLAConfigurationDTO.builder()
                .id(config.getId())
                .categorie(config.getCategorie())
                .priorite(config.getPriorite())
                .delaiPremiereReponseHeures(config.getDelaiPremiereReponseHeures())
                .delaiResolutionHeures(config.getDelaiResolutionHeures())
                .escaladeManagerHeures(config.getEscaladeManagerHeures())
                .escaladeAdminHeures(config.getEscaladeAdminHeures())
                .actif(config.isActif())
                .createdBy(config.getCreatedBy())
                .createdAt(config.getCreatedAt())
                .updatedAt(config.getUpdatedAt())
                .build();
    }
    
    /**
     * Extract user ID from authentication
     */
    private UUID extractUserIdFromAuth(Authentication authentication) {
        // This would extract the user ID from JWT token
        // For now, return a placeholder
        return UUID.randomUUID();
    }
    
    /**
     * Check if user has specific role
     */
    private boolean hasRole(Authentication authentication, String role) {
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_" + role));
    }
}
