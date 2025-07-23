package com.itsm.analytics.application.service;

import com.itsm.analytics.domain.model.SLAConfiguration;
import com.itsm.analytics.infrastructure.persistence.repository.JpaSLAConfigurationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for managing SLA configurations
 * Allows ADMIN to define and manage SLA deadlines by category/priority
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SLAConfigurationService {
    
    private final JpaSLAConfigurationRepository slaConfigRepository;
    
    /**
     * Create new SLA configuration
     */
    public SLAConfiguration creerConfiguration(String categorie, String priorite,
                                              int delaiPremiereReponse, int delaiResolution,
                                              int escaladeManager, int escaladeAdmin,
                                              UUID createdBy) {
        
        log.info("Creating SLA configuration for category: {}, priority: {}", categorie, priorite);
        
        // Check if configuration already exists
        Optional<SLAConfiguration> existing = slaConfigRepository.findByCategorieAndPriorite(categorie, priorite);
        if (existing.isPresent()) {
            throw new IllegalArgumentException("SLA configuration already exists for category: " + categorie + ", priority: " + priorite);
        }
        
        // Validate input
        validateSLAConfiguration(delaiPremiereReponse, delaiResolution, escaladeManager, escaladeAdmin);
        
        SLAConfiguration config = SLAConfiguration.creerConfiguration(
                categorie, priorite, delaiPremiereReponse, delaiResolution,
                escaladeManager, escaladeAdmin, createdBy);
        
        SLAConfiguration saved = slaConfigRepository.save(config);
        log.info("Created SLA configuration with ID: {}", saved.getId());
        
        return saved;
    }
    
    /**
     * Update existing SLA configuration
     */
    public SLAConfiguration mettreAJourConfiguration(UUID configId,
                                                    int delaiPremiereReponse, int delaiResolution,
                                                    int escaladeManager, int escaladeAdmin) {
        
        log.info("Updating SLA configuration: {}", configId);
        
        SLAConfiguration config = slaConfigRepository.findById(configId)
                .orElseThrow(() -> new IllegalArgumentException("SLA configuration not found: " + configId));
        
        // Validate input
        validateSLAConfiguration(delaiPremiereReponse, delaiResolution, escaladeManager, escaladeAdmin);
        
        config.mettreAJour(delaiPremiereReponse, delaiResolution, escaladeManager, escaladeAdmin);
        
        SLAConfiguration updated = slaConfigRepository.save(config);
        log.info("Updated SLA configuration: {}", configId);
        
        return updated;
    }
    
    /**
     * Get SLA configuration for specific category and priority
     */
    @Transactional(readOnly = true)
    public Optional<SLAConfiguration> getConfiguration(String categorie, String priorite) {
        return slaConfigRepository.findByCategorieAndPrioriteAndActif(categorie, priorite, true);
    }
    
    /**
     * Get all active SLA configurations
     */
    @Transactional(readOnly = true)
    public List<SLAConfiguration> getAllActiveConfigurations() {
        return slaConfigRepository.findByActifOrderByCategorieAscPrioriteAsc(true);
    }
    
    /**
     * Get all SLA configurations (including inactive)
     */
    @Transactional(readOnly = true)
    public List<SLAConfiguration> getAllConfigurations() {
        return slaConfigRepository.findAllOrderByCategorieAscPrioriteAsc();
    }
    
    /**
     * Get SLA configurations by category
     */
    @Transactional(readOnly = true)
    public List<SLAConfiguration> getConfigurationsByCategory(String categorie) {
        return slaConfigRepository.findByCategorieAndActifOrderByPrioriteAsc(categorie, true);
    }
    
    /**
     * Activate SLA configuration
     */
    public SLAConfiguration activerConfiguration(UUID configId) {
        log.info("Activating SLA configuration: {}", configId);
        
        SLAConfiguration config = slaConfigRepository.findById(configId)
                .orElseThrow(() -> new IllegalArgumentException("SLA configuration not found: " + configId));
        
        config.activer();
        
        SLAConfiguration activated = slaConfigRepository.save(config);
        log.info("Activated SLA configuration: {}", configId);
        
        return activated;
    }
    
    /**
     * Deactivate SLA configuration
     */
    public SLAConfiguration desactiverConfiguration(UUID configId) {
        log.info("Deactivating SLA configuration: {}", configId);
        
        SLAConfiguration config = slaConfigRepository.findById(configId)
                .orElseThrow(() -> new IllegalArgumentException("SLA configuration not found: " + configId));
        
        config.desactiver();
        
        SLAConfiguration deactivated = slaConfigRepository.save(config);
        log.info("Deactivated SLA configuration: {}", configId);
        
        return deactivated;
    }
    
    /**
     * Calculate SLA deadline for a ticket
     */
    @Transactional(readOnly = true)
    public LocalDateTime calculerDateLimiteSLA(String categorie, String priorite, LocalDateTime dateCreation) {
        Optional<SLAConfiguration> config = getConfiguration(categorie, priorite);
        
        if (config.isEmpty()) {
            log.warn("No SLA configuration found for category: {}, priority: {}", categorie, priorite);
            // Default to 24 hours if no configuration found
            return dateCreation.plusHours(24);
        }
        
        return dateCreation.plusHours(config.get().getDelaiResolutionHeures());
    }
    
    /**
     * Calculate first response deadline for a ticket
     */
    @Transactional(readOnly = true)
    public LocalDateTime calculerDateLimitePremiereReponse(String categorie, String priorite, LocalDateTime dateCreation) {
        Optional<SLAConfiguration> config = getConfiguration(categorie, priorite);
        
        if (config.isEmpty()) {
            log.warn("No SLA configuration found for category: {}, priority: {}", categorie, priorite);
            // Default to 4 hours if no configuration found
            return dateCreation.plusHours(4);
        }
        
        return dateCreation.plusHours(config.get().getDelaiPremiereReponseHeures());
    }
    
    /**
     * Check if ticket should be escalated to manager
     */
    @Transactional(readOnly = true)
    public boolean doitEscaladerManager(String categorie, String priorite, LocalDateTime dateCreation) {
        Optional<SLAConfiguration> config = getConfiguration(categorie, priorite);
        
        if (config.isEmpty()) {
            return false;
        }
        
        LocalDateTime escalationTime = dateCreation.plusHours(config.get().getEscaladeManagerHeures());
        return LocalDateTime.now().isAfter(escalationTime);
    }
    
    /**
     * Check if ticket should be escalated to admin
     */
    @Transactional(readOnly = true)
    public boolean doitEscaladerAdmin(String categorie, String priorite, LocalDateTime dateCreation) {
        Optional<SLAConfiguration> config = getConfiguration(categorie, priorite);
        
        if (config.isEmpty()) {
            return false;
        }
        
        LocalDateTime escalationTime = dateCreation.plusHours(config.get().getEscaladeAdminHeures());
        return LocalDateTime.now().isAfter(escalationTime);
    }
    
    /**
     * Validate SLA configuration parameters
     */
    private void validateSLAConfiguration(int delaiPremiereReponse, int delaiResolution,
                                         int escaladeManager, int escaladeAdmin) {
        if (delaiPremiereReponse <= 0) {
            throw new IllegalArgumentException("Délai première réponse doit être positif");
        }
        if (delaiResolution <= 0) {
            throw new IllegalArgumentException("Délai résolution doit être positif");
        }
        if (delaiPremiereReponse >= delaiResolution) {
            throw new IllegalArgumentException("Délai première réponse doit être inférieur au délai résolution");
        }
        if (escaladeManager <= 0 || escaladeManager >= delaiResolution) {
            throw new IllegalArgumentException("Escalade manager doit être entre 0 et délai résolution");
        }
        if (escaladeAdmin <= escaladeManager || escaladeAdmin >= delaiResolution) {
            throw new IllegalArgumentException("Escalade admin doit être entre escalade manager et délai résolution");
        }
    }
}
