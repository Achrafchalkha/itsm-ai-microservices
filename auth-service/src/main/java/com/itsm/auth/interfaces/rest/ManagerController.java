package com.itsm.auth.interfaces.rest;

import com.itsm.auth.application.dto.CreateTechnicianRequest;
import com.itsm.auth.application.dto.CreateTechnicianResponse;
import com.itsm.auth.application.service.CreateTechnicianUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/manager")
@RequiredArgsConstructor
@Slf4j
public class ManagerController {

    private final CreateTechnicianUseCase createTechnicianUseCase;

    @PostMapping("/createtechnician")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<CreateTechnicianResponse> createTechnician(@Valid @RequestBody CreateTechnicianRequest request) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            log.info("MANAGER {} demande de création d'un technicien pour son équipe",
                    auth.getName());
            
            CreateTechnicianResponse response = createTechnicianUseCase.execute(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            log.error("Erreur lors de la création du technicien: {}", e.getMessage());
            throw e;
        }
    }

    @GetMapping("/teams")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<String> getManagerTeams() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        log.info("Manager {} requesting their teams", auth.getName());
        
        // TODO: Implement get manager teams logic
        return ResponseEntity.ok("Manager teams endpoint - to be implemented");
    }
}
