package com.itsm.user.infrastructure.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Security service for user-specific authorization checks
 * Used in @PreAuthorize annotations to check if current user can access resources
 */
@Service("userSecurityService")
@Slf4j
public class UserSecurityService {
    
    /**
     * Check if the current authenticated user is the same as the requested user ID
     * Used to ensure users can only access their own profile
     */
    public boolean isCurrentUser(UUID userId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            log.debug("No authenticated user found");
            return false;
        }
        
        Object details = authentication.getDetails();
        if (details instanceof JwtAuthenticationFilter.UserAuthenticationDetails) {
            JwtAuthenticationFilter.UserAuthenticationDetails userDetails = 
                    (JwtAuthenticationFilter.UserAuthenticationDetails) details;
            
            UUID currentUserId = userDetails.getUserId();
            boolean isCurrentUser = userId.equals(currentUserId);
            
            log.debug("Checking if user {} is current user {}: {}", userId, currentUserId, isCurrentUser);
            return isCurrentUser;
        }
        
        log.debug("No user details found in authentication");
        return false;
    }
    
    /**
     * Get the current authenticated user ID
     */
    public UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        
        Object details = authentication.getDetails();
        if (details instanceof JwtAuthenticationFilter.UserAuthenticationDetails) {
            JwtAuthenticationFilter.UserAuthenticationDetails userDetails = 
                    (JwtAuthenticationFilter.UserAuthenticationDetails) details;
            return userDetails.getUserId();
        }
        
        return null;
    }
    
    /**
     * Get the current authenticated username
     */
    public String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        
        return authentication.getName();
    }
    
    /**
     * Get the current authenticated user role
     */
    public String getCurrentUserRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        
        Object details = authentication.getDetails();
        if (details instanceof JwtAuthenticationFilter.UserAuthenticationDetails) {
            JwtAuthenticationFilter.UserAuthenticationDetails userDetails = 
                    (JwtAuthenticationFilter.UserAuthenticationDetails) details;
            return userDetails.getRole();
        }
        
        return null;
    }
    
    /**
     * Check if current user has a specific role
     */
    public boolean hasRole(String role) {
        String currentRole = getCurrentUserRole();
        return role.equals(currentRole);
    }
    
    /**
     * Check if current user is a manager
     */
    public boolean isManager() {
        return hasRole("MANAGER");
    }
    
    /**
     * Check if current user is a technician
     */
    public boolean isTechnician() {
        return hasRole("TECHNICIEN");
    }
}
