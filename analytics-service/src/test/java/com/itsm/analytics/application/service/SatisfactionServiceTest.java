package com.itsm.analytics.application.service;

import com.itsm.analytics.domain.model.SatisfactionScore;
import com.itsm.analytics.infrastructure.persistence.repository.JpaSatisfactionScoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for SatisfactionService
 */
@ExtendWith(MockitoExtension.class)
class SatisfactionServiceTest {
    
    @Mock
    private JpaSatisfactionScoreRepository satisfactionScoreRepository;
    
    @InjectMocks
    private SatisfactionService satisfactionService;
    
    private UUID ticketId;
    private UUID utilisateurId;
    private UUID technicienId;
    private UUID teamId;
    private SatisfactionScore testScore;
    
    @BeforeEach
    void setUp() {
        ticketId = UUID.randomUUID();
        utilisateurId = UUID.randomUUID();
        technicienId = UUID.randomUUID();
        teamId = UUID.randomUUID();
        
        testScore = SatisfactionScore.builder()
                .id(UUID.randomUUID())
                .ticketId(ticketId)
                .utilisateurId(utilisateurId)
                .technicienId(technicienId)
                .teamId(teamId)
                .score(4)
                .commentaire("Très bon service")
                .createdAt(LocalDateTime.now())
                .build();
    }
    
    @Test
    void testCreateSatisfactionScore_Success() {
        // Given
        when(satisfactionScoreRepository.existsByTicketId(ticketId))
                .thenReturn(false);
        when(satisfactionScoreRepository.save(any(SatisfactionScore.class)))
                .thenReturn(testScore);
        
        // When
        SatisfactionScore result = satisfactionService.createSatisfactionScore(
                ticketId, utilisateurId, technicienId, teamId, 4, "Très bon service");
        
        // Then
        assertNotNull(result);
        assertEquals(4, result.getScore());
        assertEquals("Très bon service", result.getCommentaire());
        assertEquals(ticketId, result.getTicketId());
        
        verify(satisfactionScoreRepository).existsByTicketId(ticketId);
        verify(satisfactionScoreRepository).save(any(SatisfactionScore.class));
    }
    
    @Test
    void testCreateSatisfactionScore_AlreadyExists() {
        // Given
        when(satisfactionScoreRepository.existsByTicketId(ticketId))
                .thenReturn(true);
        
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                satisfactionService.createSatisfactionScore(
                        ticketId, utilisateurId, technicienId, teamId, 4, "Test"));
        
        assertTrue(exception.getMessage().contains("already exists"));
        verify(satisfactionScoreRepository, never()).save(any());
    }
    
    @Test
    void testCreateSatisfactionScore_InvalidScore() {
        // When & Then - Score too low
        IllegalArgumentException exception1 = assertThrows(IllegalArgumentException.class, () ->
                satisfactionService.createSatisfactionScore(
                        ticketId, utilisateurId, technicienId, teamId, 0, "Test"));
        
        assertTrue(exception1.getMessage().contains("between 1 and 5"));
        
        // When & Then - Score too high
        IllegalArgumentException exception2 = assertThrows(IllegalArgumentException.class, () ->
                satisfactionService.createSatisfactionScore(
                        ticketId, utilisateurId, technicienId, teamId, 6, "Test"));
        
        assertTrue(exception2.getMessage().contains("between 1 and 5"));
    }
    
    @Test
    void testUpdateSatisfactionScoreDetails_Success() {
        // Given
        UUID satisfactionId = testScore.getId();
        when(satisfactionScoreRepository.findById(satisfactionId))
                .thenReturn(Optional.of(testScore));
        when(satisfactionScoreRepository.save(any(SatisfactionScore.class)))
                .thenReturn(testScore);
        
        // When
        SatisfactionScore result = satisfactionService.updateSatisfactionScoreDetails(
                satisfactionId, true, 4, 5);
        
        // Then
        assertNotNull(result);
        verify(satisfactionScoreRepository).findById(satisfactionId);
        verify(satisfactionScoreRepository).save(any(SatisfactionScore.class));
    }
    
    @Test
    void testUpdateSatisfactionScoreDetails_NotFound() {
        // Given
        UUID satisfactionId = UUID.randomUUID();
        when(satisfactionScoreRepository.findById(satisfactionId))
                .thenReturn(Optional.empty());
        
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                satisfactionService.updateSatisfactionScoreDetails(satisfactionId, true, 4, 5));
        
        assertTrue(exception.getMessage().contains("not found"));
        verify(satisfactionScoreRepository, never()).save(any());
    }
    
    @Test
    void testGetSatisfactionScoresByTicket() {
        // Given
        List<SatisfactionScore> scores = List.of(testScore);
        when(satisfactionScoreRepository.findByTicketId(ticketId))
                .thenReturn(scores);
        
        // When
        List<SatisfactionScore> result = satisfactionService.getSatisfactionScoresByTicket(ticketId);
        
        // Then
        assertEquals(1, result.size());
        assertEquals(testScore, result.get(0));
    }
    
    @Test
    void testCalculateAverageSatisfactionForTechnician() {
        // Given
        LocalDate startDate = LocalDate.now().minusDays(30);
        LocalDate endDate = LocalDate.now();
        
        SatisfactionScore score1 = SatisfactionScore.builder().score(4).build();
        SatisfactionScore score2 = SatisfactionScore.builder().score(5).build();
        SatisfactionScore score3 = SatisfactionScore.builder().score(3).build();
        
        List<SatisfactionScore> scores = List.of(score1, score2, score3);
        when(satisfactionScoreRepository.findByTechnicienIdAndCreatedAtBetween(
                eq(technicienId), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(scores);
        
        // When
        BigDecimal result = satisfactionService.calculateAverageSatisfactionForTechnician(
                technicienId, startDate, endDate);
        
        // Then
        assertEquals(BigDecimal.valueOf(4.00).setScale(2), result);
    }
    
    @Test
    void testCalculateAverageSatisfactionForTechnician_NoScores() {
        // Given
        LocalDate startDate = LocalDate.now().minusDays(30);
        LocalDate endDate = LocalDate.now();
        
        when(satisfactionScoreRepository.findByTechnicienIdAndCreatedAtBetween(
                eq(technicienId), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of());
        
        // When
        BigDecimal result = satisfactionService.calculateAverageSatisfactionForTechnician(
                technicienId, startDate, endDate);
        
        // Then
        assertEquals(BigDecimal.ZERO, result);
    }
    
    @Test
    void testCalculateAverageSatisfactionForTeam() {
        // Given
        LocalDate startDate = LocalDate.now().minusDays(30);
        LocalDate endDate = LocalDate.now();
        
        SatisfactionScore score1 = SatisfactionScore.builder().score(4).build();
        SatisfactionScore score2 = SatisfactionScore.builder().score(5).build();
        
        List<SatisfactionScore> scores = List.of(score1, score2);
        when(satisfactionScoreRepository.findByTeamIdAndCreatedAtBetween(
                eq(teamId), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(scores);
        
        // When
        BigDecimal result = satisfactionService.calculateAverageSatisfactionForTeam(
                teamId, startDate, endDate);
        
        // Then
        assertEquals(BigDecimal.valueOf(4.50).setScale(2), result);
    }
    
    @Test
    void testGetSatisfactionDistribution() {
        // Given
        LocalDate startDate = LocalDate.now().minusDays(30);
        LocalDate endDate = LocalDate.now();
        
        SatisfactionScore score1 = SatisfactionScore.builder().score(1).build();
        SatisfactionScore score2 = SatisfactionScore.builder().score(4).build();
        SatisfactionScore score3 = SatisfactionScore.builder().score(4).build();
        SatisfactionScore score4 = SatisfactionScore.builder().score(5).build();
        
        List<SatisfactionScore> scores = List.of(score1, score2, score3, score4);
        when(satisfactionScoreRepository.findByCreatedAtBetween(
                any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(scores);
        
        // When
        var result = satisfactionService.getSatisfactionDistribution(startDate, endDate);
        
        // Then
        assertEquals(3, result.size());
        assertEquals(1L, result.get(1));
        assertEquals(2L, result.get(4));
        assertEquals(1L, result.get(5));
    }
    
    @Test
    void testAggregateDailySatisfactionScores() {
        // Given
        LocalDate date = LocalDate.now();
        
        SatisfactionScore score1 = SatisfactionScore.builder().score(4).build();
        SatisfactionScore score2 = SatisfactionScore.builder().score(5).build();
        
        List<SatisfactionScore> scores = List.of(score1, score2);
        when(satisfactionScoreRepository.findByCreatedAtBetween(
                any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(scores);
        
        // When
        satisfactionService.aggregateDailySatisfactionScores(date);
        
        // Then
        verify(satisfactionScoreRepository).findByCreatedAtBetween(
                any(LocalDateTime.class), any(LocalDateTime.class));
    }
}
