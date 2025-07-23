package com.itsm.analytics.application.service;

import com.itsm.analytics.domain.model.SLAConfiguration;
import com.itsm.analytics.infrastructure.persistence.repository.JpaSLAConfigurationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for SLAConfigurationService
 */
@ExtendWith(MockitoExtension.class)
class SLAConfigurationServiceTest {
    
    @Mock
    private JpaSLAConfigurationRepository slaConfigRepository;
    
    @InjectMocks
    private SLAConfigurationService slaConfigurationService;
    
    private UUID adminId;
    private SLAConfiguration testConfig;
    
    @BeforeEach
    void setUp() {
        adminId = UUID.randomUUID();
        testConfig = SLAConfiguration.builder()
                .id(UUID.randomUUID())
                .categorie("RESEAU")
                .priorite("HAUTE")
                .delaiPremiereReponseHeures(2)
                .delaiResolutionHeures(8)
                .escaladeManagerHeures(4)
                .escaladeAdminHeures(6)
                .actif(true)
                .createdBy(adminId)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
    
    @Test
    void testCreerConfiguration_Success() {
        // Given
        when(slaConfigRepository.findByCategorieAndPriorite("RESEAU", "HAUTE"))
                .thenReturn(Optional.empty());
        when(slaConfigRepository.save(any(SLAConfiguration.class)))
                .thenReturn(testConfig);
        
        // When
        SLAConfiguration result = slaConfigurationService.creerConfiguration(
                "RESEAU", "HAUTE", 2, 8, 4, 6, adminId);
        
        // Then
        assertNotNull(result);
        assertEquals("RESEAU", result.getCategorie());
        assertEquals("HAUTE", result.getPriorite());
        assertEquals(2, result.getDelaiPremiereReponseHeures());
        assertEquals(8, result.getDelaiResolutionHeures());
        assertTrue(result.isActif());
        
        verify(slaConfigRepository).findByCategorieAndPriorite("RESEAU", "HAUTE");
        verify(slaConfigRepository).save(any(SLAConfiguration.class));
    }
    
    @Test
    void testCreerConfiguration_AlreadyExists() {
        // Given
        when(slaConfigRepository.findByCategorieAndPriorite("RESEAU", "HAUTE"))
                .thenReturn(Optional.of(testConfig));
        
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                slaConfigurationService.creerConfiguration("RESEAU", "HAUTE", 2, 8, 4, 6, adminId));
        
        assertTrue(exception.getMessage().contains("already exists"));
        verify(slaConfigRepository, never()).save(any());
    }
    
    @Test
    void testCreerConfiguration_InvalidParameters() {
        // When & Then - First response time >= resolution time
        IllegalArgumentException exception1 = assertThrows(IllegalArgumentException.class, () ->
                slaConfigurationService.creerConfiguration("RESEAU", "HAUTE", 8, 8, 4, 6, adminId));
        
        assertTrue(exception1.getMessage().contains("première réponse doit être inférieur"));
        
        // When & Then - Negative values
        IllegalArgumentException exception2 = assertThrows(IllegalArgumentException.class, () ->
                slaConfigurationService.creerConfiguration("RESEAU", "HAUTE", -1, 8, 4, 6, adminId));
        
        assertTrue(exception2.getMessage().contains("doit être positif"));
    }
    
    @Test
    void testGetConfiguration_Found() {
        // Given
        when(slaConfigRepository.findByCategorieAndPrioriteAndActif("RESEAU", "HAUTE", true))
                .thenReturn(Optional.of(testConfig));
        
        // When
        Optional<SLAConfiguration> result = slaConfigurationService.getConfiguration("RESEAU", "HAUTE");
        
        // Then
        assertTrue(result.isPresent());
        assertEquals(testConfig, result.get());
    }
    
    @Test
    void testGetConfiguration_NotFound() {
        // Given
        when(slaConfigRepository.findByCategorieAndPrioriteAndActif("RESEAU", "HAUTE", true))
                .thenReturn(Optional.empty());
        
        // When
        Optional<SLAConfiguration> result = slaConfigurationService.getConfiguration("RESEAU", "HAUTE");
        
        // Then
        assertFalse(result.isPresent());
    }
    
    @Test
    void testCalculerDateLimiteSLA() {
        // Given
        LocalDateTime dateCreation = LocalDateTime.now();
        when(slaConfigRepository.findByCategorieAndPrioriteAndActif("RESEAU", "HAUTE", true))
                .thenReturn(Optional.of(testConfig));
        
        // When
        LocalDateTime deadline = slaConfigurationService.calculerDateLimiteSLA("RESEAU", "HAUTE", dateCreation);
        
        // Then
        assertEquals(dateCreation.plusHours(8), deadline);
    }
    
    @Test
    void testCalculerDateLimiteSLA_NoConfiguration() {
        // Given
        LocalDateTime dateCreation = LocalDateTime.now();
        when(slaConfigRepository.findByCategorieAndPrioriteAndActif("RESEAU", "HAUTE", true))
                .thenReturn(Optional.empty());
        
        // When
        LocalDateTime deadline = slaConfigurationService.calculerDateLimiteSLA("RESEAU", "HAUTE", dateCreation);
        
        // Then
        assertEquals(dateCreation.plusHours(24), deadline); // Default 24 hours
    }
    
    @Test
    void testMettreAJourConfiguration_Success() {
        // Given
        UUID configId = testConfig.getId();
        when(slaConfigRepository.findById(configId))
                .thenReturn(Optional.of(testConfig));
        when(slaConfigRepository.save(any(SLAConfiguration.class)))
                .thenReturn(testConfig);
        
        // When
        SLAConfiguration result = slaConfigurationService.mettreAJourConfiguration(
                configId, 1, 6, 3, 5);
        
        // Then
        assertNotNull(result);
        verify(slaConfigRepository).findById(configId);
        verify(slaConfigRepository).save(any(SLAConfiguration.class));
    }
    
    @Test
    void testMettreAJourConfiguration_NotFound() {
        // Given
        UUID configId = UUID.randomUUID();
        when(slaConfigRepository.findById(configId))
                .thenReturn(Optional.empty());
        
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                slaConfigurationService.mettreAJourConfiguration(configId, 1, 6, 3, 5));
        
        assertTrue(exception.getMessage().contains("not found"));
        verify(slaConfigRepository, never()).save(any());
    }
    
    @Test
    void testActiverConfiguration() {
        // Given
        UUID configId = testConfig.getId();
        testConfig.setActif(false);
        when(slaConfigRepository.findById(configId))
                .thenReturn(Optional.of(testConfig));
        when(slaConfigRepository.save(any(SLAConfiguration.class)))
                .thenReturn(testConfig);
        
        // When
        SLAConfiguration result = slaConfigurationService.activerConfiguration(configId);
        
        // Then
        assertNotNull(result);
        verify(slaConfigRepository).findById(configId);
        verify(slaConfigRepository).save(any(SLAConfiguration.class));
    }
    
    @Test
    void testDesactiverConfiguration() {
        // Given
        UUID configId = testConfig.getId();
        when(slaConfigRepository.findById(configId))
                .thenReturn(Optional.of(testConfig));
        when(slaConfigRepository.save(any(SLAConfiguration.class)))
                .thenReturn(testConfig);
        
        // When
        SLAConfiguration result = slaConfigurationService.desactiverConfiguration(configId);
        
        // Then
        assertNotNull(result);
        verify(slaConfigRepository).findById(configId);
        verify(slaConfigRepository).save(any(SLAConfiguration.class));
    }
    
    @Test
    void testGetAllActiveConfigurations() {
        // Given
        List<SLAConfiguration> configs = List.of(testConfig);
        when(slaConfigRepository.findByActifOrderByCategorieAscPrioriteAsc(true))
                .thenReturn(configs);
        
        // When
        List<SLAConfiguration> result = slaConfigurationService.getAllActiveConfigurations();
        
        // Then
        assertEquals(1, result.size());
        assertEquals(testConfig, result.get(0));
    }
    
    @Test
    void testDoitEscaladerManager() {
        // Given
        LocalDateTime dateCreation = LocalDateTime.now().minusHours(5);
        when(slaConfigRepository.findByCategorieAndPrioriteAndActif("RESEAU", "HAUTE", true))
                .thenReturn(Optional.of(testConfig));
        
        // When
        boolean shouldEscalate = slaConfigurationService.doitEscaladerManager("RESEAU", "HAUTE", dateCreation);
        
        // Then
        assertTrue(shouldEscalate); // 5 hours > 4 hours escalation threshold
    }
    
    @Test
    void testDoitEscaladerAdmin() {
        // Given
        LocalDateTime dateCreation = LocalDateTime.now().minusHours(7);
        when(slaConfigRepository.findByCategorieAndPrioriteAndActif("RESEAU", "HAUTE", true))
                .thenReturn(Optional.of(testConfig));
        
        // When
        boolean shouldEscalate = slaConfigurationService.doitEscaladerAdmin("RESEAU", "HAUTE", dateCreation);
        
        // Then
        assertTrue(shouldEscalate); // 7 hours > 6 hours escalation threshold
    }
}
