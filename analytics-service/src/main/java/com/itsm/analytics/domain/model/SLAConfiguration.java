package com.itsm.analytics.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain model for SLA Configuration
 * Defines SLA deadlines by category and priority
 * Managed by ADMIN role
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SLAConfiguration {
    
    private UUID id;
    private String categorie;
    private String priorite;
    private Integer delaiPremiereReponseHeures;
    private Integer delaiResolutionHeures;
    private Integer escaladeManagerHeures;
    private Integer escaladeAdminHeures;
    private boolean actif;
    private UUID createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    /**
     * Factory method to create new SLA configuration
     */
    public static SLAConfiguration creerConfiguration(String categorie, String priorite,
                                                     int delaiPremiereReponse, int delaiResolution,
                                                     int escaladeManager, int escaladeAdmin,
                                                     UUID createdBy) {
        return SLAConfiguration.builder()
                .id(UUID.randomUUID())
                .categorie(categorie)
                .priorite(priorite)
                .delaiPremiereReponseHeures(delaiPremiereReponse)
                .delaiResolutionHeures(delaiResolution)
                .escaladeManagerHeures(escaladeManager)
                .escaladeAdminHeures(escaladeAdmin)
                .actif(true)
                .createdBy(createdBy)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
    
    /**
     * Update SLA configuration
     */
    public void mettreAJour(int delaiPremiereReponse, int delaiResolution,
                           int escaladeManager, int escaladeAdmin) {
        this.delaiPremiereReponseHeures = delaiPremiereReponse;
        this.delaiResolutionHeures = delaiResolution;
        this.escaladeManagerHeures = escaladeManager;
        this.escaladeAdminHeures = escaladeAdmin;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Deactivate configuration
     */
    public void desactiver() {
        this.actif = false;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Activate configuration
     */
    public void activer() {
        this.actif = true;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Check if SLA deadline is approaching for escalation
     */
    public boolean doitEscaladerManager(int heuresEcoulees) {
        return heuresEcoulees >= this.escaladeManagerHeures;
    }
    
    /**
     * Check if SLA deadline is approaching for admin escalation
     */
    public boolean doitEscaladerAdmin(int heuresEcoulees) {
        return heuresEcoulees >= this.escaladeAdminHeures;
    }
    
    /**
     * Get unique key for category/priority combination
     */
    public String getCategoryPriorityKey() {
        return categorie + "_" + priorite;
    }
}
