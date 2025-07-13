package com.itsm.auth.domain.model;

/**
 * Enum representing user roles in the ITSM platform
 * Hierarchical structure: ADMIN > MANAGER > TECHNICIEN, UTILISATEUR
 */
public enum Role {
    ADMIN,          // System administrator - highest privileges
    MANAGER,        // Team manager - can manage technicians
    TECHNICIEN,     // Support technician - handles tickets
    UTILISATEUR     // End user - creates tickets
}
