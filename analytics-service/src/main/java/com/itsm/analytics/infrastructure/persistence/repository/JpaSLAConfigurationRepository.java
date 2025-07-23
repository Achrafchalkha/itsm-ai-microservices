package com.itsm.analytics.infrastructure.persistence.repository;

import com.itsm.analytics.domain.model.SLAConfiguration;
import com.itsm.analytics.infrastructure.persistence.entity.SLAConfigurationEntity;
import com.itsm.analytics.infrastructure.persistence.mapper.SLAConfigurationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * JPA Repository for SLA Configuration
 * Provides data access layer for SLA configurations
 */
@Repository
@RequiredArgsConstructor
public class JpaSLAConfigurationRepository {
    
    private final SLAConfigurationJpaRepository jpaRepository;
    private final SLAConfigurationMapper mapper;
    
    public SLAConfiguration save(SLAConfiguration slaConfiguration) {
        SLAConfigurationEntity entity = mapper.toEntity(slaConfiguration);
        SLAConfigurationEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }
    
    public Optional<SLAConfiguration> findById(UUID id) {
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }
    
    public Optional<SLAConfiguration> findByCategorieAndPriorite(String categorie, String priorite) {
        return jpaRepository.findByCategorieAndPriorite(categorie, priorite)
                .map(mapper::toDomain);
    }
    
    public Optional<SLAConfiguration> findByCategorieAndPrioriteAndActif(String categorie, String priorite, boolean actif) {
        return jpaRepository.findByCategorieAndPrioriteAndActif(categorie, priorite, actif)
                .map(mapper::toDomain);
    }
    
    public List<SLAConfiguration> findByActifOrderByCategorieAscPrioriteAsc(boolean actif) {
        return jpaRepository.findByActifOrderByCategorieAscPrioriteAsc(actif)
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
    
    public List<SLAConfiguration> findAllOrderByCategorieAscPrioriteAsc() {
        return jpaRepository.findAllOrderByCategorieAscPrioriteAsc()
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
    
    public List<SLAConfiguration> findByCategorieAndActifOrderByPrioriteAsc(String categorie, boolean actif) {
        return jpaRepository.findByCategorieAndActifOrderByPrioriteAsc(categorie, actif)
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
    
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }
    
    public boolean existsByCategorieAndPriorite(String categorie, String priorite) {
        return jpaRepository.existsByCategorieAndPriorite(categorie, priorite);
    }
    
    public long countByActif(boolean actif) {
        return jpaRepository.countByActif(actif);
    }
}

/**
 * Spring Data JPA Repository interface
 */
interface SLAConfigurationJpaRepository extends JpaRepository<SLAConfigurationEntity, UUID> {
    
    Optional<SLAConfigurationEntity> findByCategorieAndPriorite(String categorie, String priorite);
    
    Optional<SLAConfigurationEntity> findByCategorieAndPrioriteAndActif(String categorie, String priorite, Boolean actif);
    
    List<SLAConfigurationEntity> findByActifOrderByCategorieAscPrioriteAsc(Boolean actif);
    
    @Query("SELECT s FROM SLAConfigurationEntity s ORDER BY s.categorie ASC, s.priorite ASC")
    List<SLAConfigurationEntity> findAllOrderByCategorieAscPrioriteAsc();
    
    List<SLAConfigurationEntity> findByCategorieAndActifOrderByPrioriteAsc(String categorie, Boolean actif);
    
    boolean existsByCategorieAndPriorite(String categorie, String priorite);
    
    long countByActif(Boolean actif);
    
    @Query("SELECT DISTINCT s.categorie FROM SLAConfigurationEntity s WHERE s.actif = true ORDER BY s.categorie")
    List<String> findDistinctActiveCategories();
    
    @Query("SELECT DISTINCT s.priorite FROM SLAConfigurationEntity s WHERE s.actif = true ORDER BY s.priorite")
    List<String> findDistinctActivePriorities();
}
