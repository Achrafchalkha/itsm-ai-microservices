package com.itsm.user.interfaces.mapper;

import com.itsm.user.domain.model.Competence;
import com.itsm.user.domain.model.User;
import com.itsm.user.interfaces.dto.UserProfileResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper to convert between domain models and DTOs
 * Handles the translation between application and interface layers
 */
@Component
public class UserDtoMapper {
    
    /**
     * Convert User domain model to UserProfileResponse DTO
     */
    public UserProfileResponse toResponse(User user) {
        if (user == null) {
            return null;
        }
        
        List<UserProfileResponse.CompetenceDto> competenceDtos = user.getCompetences().stream()
                .map(this::competenceToDto)
                .collect(Collectors.toList());
        
        return UserProfileResponse.builder()
                .id(user.getId())
                .nom(user.getNom())
                .prenom(user.getPrenom())
                .email(user.getEmail())
                .role(user.getRole())
                .managerId(user.getManagerId())
                .teamId(user.getTeamId())
                .statutTechnicien(user.getStatutTechnicien())
                .localisation(user.getLocalisation())
                .competences(competenceDtos)
                .dateCreation(user.getDateCreation())
                .dateModification(user.getDateModification())
                .actif(user.isActif())
                .build();
    }
    
    /**
     * Convert Competence domain model to CompetenceDto
     */
    public UserProfileResponse.CompetenceDto competenceToDto(Competence competence) {
        if (competence == null) {
            return null;
        }
        
        return UserProfileResponse.CompetenceDto.builder()
                .id(competence.getId())
                .nom(competence.getNom())
                .description(competence.getDescription())
                .categorie(competence.getCategorie())
                .niveau(competence.getNiveau())
                .build();
    }
    
    /**
     * Convert list of User domain models to list of UserProfileResponse DTOs
     */
    public List<UserProfileResponse> toResponseList(List<User> users) {
        return users.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}
