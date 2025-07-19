package com.itsm.ticket.domain.model;

/**
 * Enum representing SLA status for analytics
 */
public enum StatutSLA {
    DANS_LES_TEMPS,     // Within SLA deadline
    EN_RETARD,          // Past SLA deadline
    CRITIQUE            // Critically overdue
}
