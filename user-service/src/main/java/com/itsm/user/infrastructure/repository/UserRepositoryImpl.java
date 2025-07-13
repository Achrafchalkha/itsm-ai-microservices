package com.itsm.user.infrastructure.repository;

import com.itsm.user.domain.model.Role;
import com.itsm.user.domain.model.StatutTechnicien;
import com.itsm.user.domain.model.User;
import com.itsm.user.domain.repository.UserRepository;
import com.itsm.user.infrastructure.mapper.UserMapper;
import com.itsm.user.infrastructure.persistence.JpaUserEntity;
import com.itsm.user.infrastructure.persistence.JpaUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of UserRepository using Spring Data JPA
 * Bridges the domain layer with the infrastructure layer
 */
@Repository
@RequiredArgsConstructor
@Slf4j
public class UserRepositoryImpl implements UserRepository {
    
    private final JpaUserRepository jpaUserRepository;
    private final UserMapper userMapper;
    
    @Override
    public User save(User user) {
        log.debug("Saving user: {}", user.getEmail());
        
        JpaUserEntity entity;
        if (jpaUserRepository.existsById(user.getId())) {
            // Update existing entity
            entity = jpaUserRepository.findById(user.getId()).orElseThrow();
            userMapper.updateEntity(entity, user);
        } else {
            // Create new entity
            entity = userMapper.toEntity(user);
        }
        
        JpaUserEntity savedEntity = jpaUserRepository.save(entity);
        return userMapper.toDomain(savedEntity);
    }
    
    @Override
    public Optional<User> findById(UUID id) {
        log.debug("Finding user by ID: {}", id);
        return jpaUserRepository.findById(id)
                .map(userMapper::toDomain);
    }
    
    @Override
    public Optional<User> findByEmail(String email) {
        log.debug("Finding user by email: {}", email);
        return jpaUserRepository.findByEmail(email)
                .map(userMapper::toDomain);
    }
    
    @Override
    public List<User> findAll() {
        log.debug("Finding all users");
        return userMapper.toDomainList(jpaUserRepository.findAll());
    }
    
    @Override
    public List<User> findByRole(Role role) {
        log.debug("Finding users by role: {}", role);
        return userMapper.toDomainList(jpaUserRepository.findByRole(role));
    }
    
    @Override
    public List<User> findTechniciensByStatut(StatutTechnicien statut) {
        log.debug("Finding technicians by status: {}", statut);
        return userMapper.toDomainList(jpaUserRepository.findTechniciensByStatut(statut));
    }
    
    @Override
    public List<User> findTechniciensByEquipe(UUID equipeId) {
        log.debug("Finding technicians by team: {}", equipeId);
        return userMapper.toDomainList(jpaUserRepository.findTechniciensByEquipe(equipeId));
    }
    
    @Override
    public List<User> findTechniciensByLocalisation(String localisation) {
        log.debug("Finding technicians by location: {}", localisation);
        return userMapper.toDomainList(jpaUserRepository.findTechniciensByLocalisation(localisation));
    }
    
    @Override
    public List<User> findTechniciensByCompetence(String nomCompetence, int niveauMinimum) {
        log.debug("Finding technicians by competence: {} (level >= {})", nomCompetence, niveauMinimum);
        String niveauEnum = convertNiveauToEnum(niveauMinimum);
        return userMapper.toDomainList(
                jpaUserRepository.findTechniciensByCompetence(nomCompetence, niveauEnum));
    }

    @Override
    public List<User> findTechniciensDisponiblesAvecCompetence(String nomCompetence, int niveauMinimum) {
        log.debug("Finding available technicians with competence: {} (level >= {})", nomCompetence, niveauMinimum);
        String niveauEnum = convertNiveauToEnum(niveauMinimum);
        return userMapper.toDomainList(
                jpaUserRepository.findTechniciensDisponiblesAvecCompetence(nomCompetence, niveauEnum));
    }

    /**
     * Convert numeric level to enum string for database queries
     */
    private String convertNiveauToEnum(int niveau) {
        switch (niveau) {
            case 1: return "JUNIOR";
            case 2: return "SENIOR";
            case 3: return "EXPERT";
            default: return "JUNIOR";
        }
    }
    
    @Override
    public List<User> findTechniciensDisponiblesDansLocalisation(String localisation) {
        log.debug("Finding available technicians in location: {}", localisation);
        return userMapper.toDomainList(
                jpaUserRepository.findTechniciensDisponiblesDansLocalisation(localisation));
    }
    
    @Override
    public List<User> findTechniciensDisponiblesDansEquipe(UUID equipeId) {
        log.debug("Finding available technicians in team: {}", equipeId);
        return userMapper.toDomainList(
                jpaUserRepository.findTechniciensDisponiblesDansEquipe(equipeId));
    }
    
    @Override
    public boolean existsById(UUID id) {
        return jpaUserRepository.existsById(id);
    }
    
    @Override
    public boolean existsByEmail(String email) {
        return jpaUserRepository.existsByEmail(email);
    }
    
    @Override
    public void deleteById(UUID id) {
        log.debug("Deleting user by ID: {}", id);
        jpaUserRepository.deleteById(id);
    }
    
    @Override
    public long countByRole(Role role) {
        return jpaUserRepository.countByRole(role);
    }
    
    @Override
    public long countTechniciensByStatut(StatutTechnicien statut) {
        return jpaUserRepository.countTechniciensByStatut(statut);
    }
}
