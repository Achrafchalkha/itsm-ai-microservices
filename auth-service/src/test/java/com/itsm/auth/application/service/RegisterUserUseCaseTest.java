package com.itsm.auth.application.service;

import com.itsm.auth.application.dto.AuthResponse;
import com.itsm.auth.application.dto.RegisterUserRequest;
import com.itsm.auth.domain.model.Role;
import com.itsm.auth.domain.model.Utilisateur;
import com.itsm.auth.domain.repository.UtilisateurRepository;
import com.itsm.auth.infrastructure.security.JwtProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegisterUserUseCaseTest {

    @Mock
    private UtilisateurRepository utilisateurRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private RegisterUserUseCase registerUserUseCase;

    private RegisterUserRequest request;

    @BeforeEach
    void setUp() {
        request = new RegisterUserRequest();
        request.setNom("Doe");
        request.setPrenom("John");
        request.setEmail("john.doe@example.com");
        request.setMotDePasse("password123");
    }

    @Test
    void execute_ShouldCreateUserSuccessfully() {
        // Given
        when(utilisateurRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(request.getMotDePasse())).thenReturn("hashedPassword");
        
        Utilisateur savedUser = Utilisateur.creerUtilisateur(
                request.getNom(), request.getPrenom(), request.getEmail(), "hashedPassword");
        when(utilisateurRepository.save(any(Utilisateur.class))).thenReturn(savedUser);
        when(jwtProvider.generateToken(any(Utilisateur.class))).thenReturn("jwt-token");

        // When
        AuthResponse response = registerUserUseCase.execute(request);

        // Then
        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        assertEquals(request.getEmail(), response.getEmail());
        assertEquals(request.getNom(), response.getNom());
        assertEquals(request.getPrenom(), response.getPrenom());
        assertEquals(Role.UTILISATEUR, response.getRole());

        verify(utilisateurRepository).existsByEmail(request.getEmail());
        verify(passwordEncoder).encode(request.getMotDePasse());
        verify(utilisateurRepository).save(any(Utilisateur.class));
        verify(jwtProvider).generateToken(any(Utilisateur.class));
        verify(kafkaTemplate).send(eq("user-created"), any());
    }

    @Test
    void execute_ShouldThrowException_WhenEmailAlreadyExists() {
        // Given
        when(utilisateurRepository.existsByEmail(request.getEmail())).thenReturn(true);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
                () -> registerUserUseCase.execute(request));
        
        assertEquals("Un utilisateur avec cet email existe déjà", exception.getMessage());
        verify(utilisateurRepository).existsByEmail(request.getEmail());
        verify(utilisateurRepository, never()).save(any());
    }
}
