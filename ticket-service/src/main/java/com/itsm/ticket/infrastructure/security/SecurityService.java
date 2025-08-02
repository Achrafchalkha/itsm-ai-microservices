package com.itsm.ticket.infrastructure.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Service for security-related operations
 * Handles JWT token parsing and user context
 */
@Service
@Slf4j
public class SecurityService {

    /**
     * Get current user ID from JWT token
     */
    public UUID getCurrentUserId() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication != null && authentication.getDetails() instanceof JwtAuthenticationFilter.UserAuthenticationDetails details) {
                UUID userId = details.getUserId();
                log.debug("Found user ID from authentication details: {}", userId);
                return userId;
            }

            log.warn("No user ID found in security context. Authentication: {}, Details: {}",
                    authentication != null ? authentication.getClass().getSimpleName() : "null",
                    authentication != null ? authentication.getDetails() : "null");
            return null;

        } catch (Exception e) {
            log.error("Error extracting user ID from security context: {}", e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * Get current user email from JWT token
     */
    public String getCurrentUserEmail() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication != null && authentication.getDetails() instanceof JwtAuthenticationFilter.UserAuthenticationDetails details) {
                return details.getUsername(); // Username is email in our case
            }

            return null;

        } catch (Exception e) {
            log.error("Error extracting user email from security context: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Get current user role from JWT token
     */
    public String getCurrentUserRole() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication != null && authentication.getDetails() instanceof JwtAuthenticationFilter.UserAuthenticationDetails details) {
                return details.getRole();
            }

            return null;

        } catch (Exception e) {
            log.error("Error extracting user role from security context: {}", e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * Check if current user has specific role
     */
    public boolean hasRole(String role) {
        String currentRole = getCurrentUserRole();
        return currentRole != null && currentRole.equals(role);
    }

    /**
     * Check if the current user is the same as the provided user ID
     * Used in @PreAuthorize annotations
     */
    public boolean isCurrentUser(UUID userId) {
        try {
            UUID currentUserId = getCurrentUserId();
            boolean isCurrentUser = userId != null && userId.equals(currentUserId);
            log.debug("Checking if user {} is current user {}: {}", userId, currentUserId, isCurrentUser);
            return isCurrentUser;
        } catch (Exception e) {
            log.error("Error checking if current user: {}", e.getMessage(), e);
            return false;
        }
    }
}
