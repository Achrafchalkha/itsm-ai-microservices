package com.itsm.user.application.service;

import com.itsm.user.domain.model.StatutTechnicien;
import com.itsm.user.domain.model.User;
import com.itsm.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Application service for technician assignment and availability queries
 * Provides intelligent assignment capabilities for the ticket system
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class TechnicianAssignmentService {
    
    private final UserRepository userRepository;
    
    /**
     * Find available technicians with specific competence
     */
    public List<User> trouverTechniciensDisponiblesAvecCompetence(String nomCompetence, int niveauMinimum) {
        log.debug("Finding available technicians with competence: {} (level >= {})", nomCompetence, niveauMinimum);
        return userRepository.findTechniciensDisponiblesAvecCompetence(nomCompetence, niveauMinimum);
    }
    
    /**
     * Find available technicians in a specific location
     */
    public List<User> trouverTechniciensDisponiblesDansLocalisation(String localisation) {
        log.debug("Finding available technicians in location: {}", localisation);
        return userRepository.findTechniciensDisponiblesDansLocalisation(localisation);
    }
    
    /**
     * Find available technicians in a team
     */
    public List<User> trouverTechniciensDisponiblesDansEquipe(UUID equipeId) {
        log.debug("Finding available technicians in team: {}", equipeId);
        return userRepository.findTechniciensDisponiblesDansEquipe(equipeId);
    }
    
    /**
     * Find the best technician for assignment based on multiple criteria
     * This is a simplified algorithm - in production, this could be much more sophisticated
     */
    public Optional<User> trouverMeilleurTechnicienPourAssignation(
            String competenceRequise, 
            int niveauMinimum, 
            String localisationPreferee, 
            UUID equipePreferee) {
        
        log.debug("Finding best technician for assignment - competence: {}, level: {}, location: {}, team: {}", 
                competenceRequise, niveauMinimum, localisationPreferee, equipePreferee);
        
        // Get all available technicians with the required competence
        List<User> candidats = userRepository.findTechniciensDisponiblesAvecCompetence(competenceRequise, niveauMinimum);
        
        if (candidats.isEmpty()) {
            log.warn("No available technicians found with competence: {} (level >= {})", competenceRequise, niveauMinimum);
            return Optional.empty();
        }
        
        // Apply scoring algorithm to find the best match
        return candidats.stream()
                .max((t1, t2) -> Integer.compare(
                        calculerScoreAssignation(t1, competenceRequise, localisationPreferee, equipePreferee),
                        calculerScoreAssignation(t2, competenceRequise, localisationPreferee, equipePreferee)
                ));
    }
    
    /**
     * Calculate assignment score for a technician
     * Higher score means better match
     */
    private int calculerScoreAssignation(User technicien, String competenceRequise, 
                                       String localisationPreferee, UUID equipePreferee) {
        int score = 0;
        
        // Base score for competence level
        score += technicien.getNiveauCompetence(competenceRequise) * 10;
        
        // Bonus for same location
        if (localisationPreferee != null && localisationPreferee.equals(technicien.getLocalisation())) {
            score += 20;
        }
        
        // Bonus for same team
        if (equipePreferee != null && equipePreferee.equals(technicien.getTeamId())) {
            score += 15;
        }
        
        // In a real system, you might also consider:
        // - Current workload (number of assigned tickets)
        // - Historical performance
        // - Availability schedule
        // - Customer preferences
        
        return score;
    }
    
    /**
     * Get technician availability statistics
     */
    public TechnicianAvailabilityStats obtenirStatistiquesDisponibilite() {
        log.debug("Getting technician availability statistics");
        
        long totalTechnicians = userRepository.countByRole(com.itsm.user.domain.model.Role.TECHNICIEN);
        long availableTechnicians = userRepository.countTechniciensByStatut(StatutTechnicien.DISPONIBLE);
        long busyTechnicians = userRepository.countTechniciensByStatut(StatutTechnicien.OCCUPE);
        long absentTechnicians = userRepository.countTechniciensByStatut(StatutTechnicien.ABSENT);
        long offlineTechnicians = userRepository.countTechniciensByStatut(StatutTechnicien.HORS_LIGNE);
        
        return TechnicianAvailabilityStats.builder()
                .totalTechnicians(totalTechnicians)
                .availableTechnicians(availableTechnicians)
                .busyTechnicians(busyTechnicians)
                .absentTechnicians(absentTechnicians)
                .offlineTechnicians(offlineTechnicians)
                .availabilityRate(totalTechnicians > 0 ? (double) availableTechnicians / totalTechnicians : 0.0)
                .build();
    }
    
    /**
     * Find technicians by multiple criteria
     */
    public List<User> rechercherTechniciens(TechnicianSearchCriteria criteria) {
        log.debug("Searching technicians with criteria: {}", criteria);
        
        List<User> results = userRepository.findTechniciensByStatut(StatutTechnicien.DISPONIBLE);
        
        // Filter by competence if specified
        if (criteria.getCompetenceRequise() != null) {
            results = results.stream()
                    .filter(t -> t.aCompetence(criteria.getCompetenceRequise(), criteria.getNiveauMinimum()))
                    .collect(Collectors.toList());
        }
        
        // Filter by location if specified
        if (criteria.getLocalisation() != null) {
            results = results.stream()
                    .filter(t -> criteria.getLocalisation().equals(t.getLocalisation()))
                    .collect(Collectors.toList());
        }
        
        // Filter by team if specified
        if (criteria.getEquipeId() != null) {
            results = results.stream()
                    .filter(t -> criteria.getEquipeId().equals(t.getTeamId()))
                    .collect(Collectors.toList());
        }
        
        return results;
    }
    
    /**
     * Data class for technician availability statistics
     */
    @lombok.Data
    @lombok.Builder
    public static class TechnicianAvailabilityStats {
        private long totalTechnicians;
        private long availableTechnicians;
        private long busyTechnicians;
        private long absentTechnicians;
        private long offlineTechnicians;
        private double availabilityRate;
    }
    
    /**
     * Data class for technician search criteria
     */
    @lombok.Data
    @lombok.Builder
    public static class TechnicianSearchCriteria {
        private String competenceRequise;
        private int niveauMinimum;
        private String localisation;
        private UUID equipeId;
        private StatutTechnicien statut;
    }
}
