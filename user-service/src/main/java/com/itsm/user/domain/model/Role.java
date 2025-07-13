package com.itsm.user.domain.model;

/**
 * Enum representing user roles in the ITSM platform
 * Hierarchical structure: ADMIN -> MANAGER -> TECHNICIEN, UTILISATEUR (self-register)
 */
public enum Role {
    ADMIN,          // System administrator (DSI ALTEN) - creates MANAGERs
    MANAGER,        // Team manager - creates TECHNICIENs and manages teams
    TECHNICIEN,     // Support technician - created by MANAGER
    UTILISATEUR     // End user (client) - self-registers
}
