package com.itsm.auth.infrastructure.persistence.adapter;

import com.itsm.auth.domain.model.Utilisateur;
import com.itsm.auth.domain.repository.UtilisateurRepository;
import com.itsm.auth.infrastructure.persistence.mapper.UtilisateurMapper;
import com.itsm.auth.infrastructure.persistence.repository.JpaUtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UtilisateurRepositoryAdapter implements UtilisateurRepository {
    
    private final JpaUtilisateurRepository jpaRepository;
    private final UtilisateurMapper mapper;

    @Override
    public Utilisateur save(Utilisateur utilisateur) {
        var entity = mapper.toEntity(utilisateur);
        var savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Utilisateur> findById(UUID id) {
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<Utilisateur> findByEmail(String email) {
        return jpaRepository.findByEmail(email)
                .map(mapper::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpaRepository.existsByEmail(email);
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }
}
