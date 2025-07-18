package com.itsm.user.infrastructure.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Security service for authorization checks
 */
@Service
@Slf4j
public class SecurityService {

    /**
     * Check if the current user is the same as the provided user ID
     */
    public boolean isCurrentUser(UUID userId) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || auth.getDetails() == null) {
                log.debug("No authentication or details found");
                return false;
            }

            if (auth.getDetails() instanceof JwtAuthenticationFilter.UserAuthenticationDetails details) {
                UUID currentUserId = details.getUserId();
                boolean isCurrentUser = userId.equals(currentUserId);
                log.debug("Checking if user {} is current user {}: {}", userId, currentUserId, isCurrentUser);
                return isCurrentUser;
            }

            log.debug("Authentication details is not UserAuthenticationDetails");
            return false;
        } catch (Exception e) {
            log.error("Error checking if current user: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Get current user ID from security context
     */
    public UUID getCurrentUserId() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getDetails() instanceof JwtAuthenticationFilter.UserAuthenticationDetails details) {
                return details.getUserId();
            }
        } catch (Exception e) {
            log.error("Error getting current user ID: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Get current user role from security context
     */
    public String getCurrentUserRole() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getDetails() instanceof JwtAuthenticationFilter.UserAuthenticationDetails details) {
                return details.getRole();
            }
        } catch (Exception e) {
            log.error("Error getting current user role: {}", e.getMessage());
        }
        return null;
    }
}
