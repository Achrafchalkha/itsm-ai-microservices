package com.itsm.analytics.infrastructure.persistence.entity;

import com.itsm.analytics.domain.model.SLAAlert;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA Entity for SLA Alert
 * Maps to sla_alerts table in analytics_db
 */
@Entity
@Table(name = "sla_alerts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SLAAlertEntity {
    
    @Id
    @Column(name = "id", columnDefinition = "UUID")
    private UUID id;
    
    @Column(name = "ticket_id", nullable = false, columnDefinition = "UUID")
    private UUID ticketId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "alert_type", nullable = false, length = 20)
    private SLAAlert.AlertType alertType;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "alert_level", nullable = false, length = 20)
    private SLAAlert.AlertLevel alertLevel;
    
    // Alert details
    @Column(name = "sla_deadline", nullable = false)
    private LocalDateTime slaDeadline;
    
    @Column(name = "time_remaining_minutes")
    private Integer timeRemainingMinutes;
    
    @Column(name = "escalated_to", columnDefinition = "UUID")
    private UUID escalatedTo;
    
    @Column(name = "escalated_at")
    private LocalDateTime escalatedAt;
    
    // Resolution
    @Column(name = "resolved", nullable = false)
    @Builder.Default
    private Boolean resolved = false;
    
    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;
    
    @Column(name = "resolution_action", length = 100)
    private String resolutionAction;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
