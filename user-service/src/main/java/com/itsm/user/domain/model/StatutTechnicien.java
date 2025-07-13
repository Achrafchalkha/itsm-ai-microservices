package com.itsm.user.domain.model;

/**
 * Enum representing the status of a technician
 * Used for intelligent assignment and workload management
 */
public enum StatutTechnicien {
    DISPONIBLE,     // Available for new assignments
    OCCUPE,         // Currently working on tickets
    ABSENT,         // Not available (vacation, sick leave, etc.)
    HORS_LIGNE      // Offline/not working
}
