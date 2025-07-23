package com.itsm.analytics.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA Entity for SLA Configuration
 * Maps to sla_configurations table in analytics_db
 */
@Entity
@Table(name = "sla_configurations", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"categorie", "priorite"}))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SLAConfigurationEntity {
    
    @Id
    @Column(name = "id", columnDefinition = "UUID")
    private UUID id;
    
    @Column(name = "categorie", nullable = false, length = 50)
    private String categorie;
    
    @Column(name = "priorite", nullable = false, length = 20)
    private String priorite;
    
    @Column(name = "delai_premiere_reponse_heures", nullable = false)
    private Integer delaiPremiereReponseHeures;
    
    @Column(name = "delai_resolution_heures", nullable = false)
    private Integer delaiResolutionHeures;
    
    @Column(name = "escalade_manager_heures")
    private Integer escaladeManagerHeures;
    
    @Column(name = "escalade_admin_heures")
    private Integer escaladeAdminHeures;
    
    @Column(name = "actif", nullable = false)
    @Builder.Default
    private Boolean actif = true;
    
    @Column(name = "created_by", nullable = false, columnDefinition = "UUID")
    private UUID createdBy;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
