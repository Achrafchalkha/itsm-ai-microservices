package com.itsm.ticket.domain.model;

/**
 * Enum representing ticket status
 */
public enum StatutTicket {
    OUVERT,         // Newly created ticket
    EN_ATTENTE,     // Waiting for assignment or response
    EN_COURS,       // Being worked on by technician
    RESOLU,         // Resolved by technician
    FERME,          // Closed by user or system
    ANNULE          // Cancelled
}
