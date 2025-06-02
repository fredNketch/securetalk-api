package com.securetalk.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Entité pour l'audit et le tracking des actions dans l'application
 */
@Entity
@Table(name = "audit_logs",
        indexes = {
                @Index(name = "idx_audit_log_user", columnList = "user_id"),
                @Index(name = "idx_audit_log_action", columnList = "action"),
                @Index(name = "idx_audit_log_timestamp", columnList = "timestamp"),
                @Index(name = "idx_audit_log_entity", columnList = "entity_type, entity_id"),
                @Index(name = "idx_audit_log_severity", columnList = "severity"),
                @Index(name = "idx_audit_log_ip", columnList = "ip_address")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"user"})
public class AuditLog extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "fk_audit_log_user"))
    private User user; // Peut être null pour les actions système

    @Column(name = "action", nullable = false, length = 100)
    private String action; // LOGIN, LOGOUT, CREATE_USER, SEND_MESSAGE, etc.

    @Column(name = "entity_type", length = 50)
    private String entityType; // USER, MESSAGE, NOTIFICATION, etc.

    @Column(name = "entity_id")
    private Long entityId;

    @Column(name = "timestamp", nullable = false)
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 1000)
    private String userAgent;

    @Column(name = "session_id", length = 100)
    private String sessionId;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "severity", length = 20)
    @Builder.Default
    private String severity = "INFO"; // DEBUG, INFO, WARN, ERROR, CRITICAL

    @Column(name = "success", nullable = false)
    @Builder.Default
    private Boolean success = true;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "old_values", columnDefinition = "TEXT")
    private String oldValues; // JSON des anciennes valeurs

    @Column(name = "new_values", columnDefinition = "TEXT")
    private String newValues; // JSON des nouvelles valeurs

    @ElementCollection
    @CollectionTable(name = "audit_log_metadata",
            joinColumns = @JoinColumn(name = "audit_log_id"))
    @MapKeyColumn(name = "meta_key")
    @Column(name = "meta_value", length = 1000)
    private Map<String, String> metadata;

    @Column(name = "category", length = 50)
    @Builder.Default
    private String category = "GENERAL"; // SECURITY, ADMIN, USER, MESSAGE, SYSTEM

    @Column(name = "method", length = 20)
    private String method; // GET, POST, PUT, DELETE

    @Column(name = "endpoint", length = 500)
    private String endpoint; // URL endpoint appelé

    @Column(name = "duration_ms")
    private Long durationMs; // Durée de l'opération en millisecondes

    @Column(name = "request_size")
    private Long requestSize; // Taille de la requête en octets

    @Column(name = "response_size")
    private Long responseSize; // Taille de la réponse en octets

    @Column(name = "http_status")
    private Integer httpStatus;

    @Column(name = "device_info", length = 200)
    private String deviceInfo;

    @Column(name = "location_country", length = 3)
    private String locationCountry;

    @Column(name = "location_city", length = 100)
    private String locationCity;

    @Column(name = "risk_score")
    private Integer riskScore; // Score de risque de 0 à 100

    @Column(name = "flagged", nullable = false)
    @Builder.Default
    private Boolean flagged = false;

    @Column(name = "flagged_reason", length = 500)
    private String flaggedReason;

    @Column(name = "reviewed", nullable = false)
    @Builder.Default
    private Boolean reviewed = false;

    @Column(name = "reviewed_by")
    private Long reviewedBy;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    // Méthodes utilitaires
    public void flag(String reason) {
        this.flagged = true;
        this.flaggedReason = reason;
    }

    public void markReviewed(Long reviewerId) {
        this.reviewed = true;
        this.reviewedBy = reviewerId;
        this.reviewedAt = LocalDateTime.now();
    }

    public void addMetadata(String key, String value) {
        if (metadata == null) {
            metadata = new java.util.HashMap<>();
        }
        metadata.put(key, value);
    }

    public String getMetadata(String key) {
        return metadata != null ? metadata.get(key) : null;
    }

    public boolean isSecurityRelated() {
        return "SECURITY".equals(category) ||
                action.contains("LOGIN") ||
                action.contains("LOGOUT") ||
                action.contains("PASSWORD") ||
                action.contains("PERMISSION");
    }

    public boolean isHighRisk() {
        return riskScore != null && riskScore >= 70;
    }

    public boolean requiresReview() {
        return flagged || isHighRisk() || "CRITICAL".equals(severity);
    }

    public String getUserIdentifier() {
        if (user != null) {
            return user.getUsername() + " (" + user.getId() + ")";
        }
        return "System";
    }

    public String getEntityIdentifier() {
        if (entityType != null && entityId != null) {
            return entityType + ":" + entityId;
        }
        return null;
    }

    public boolean isFailedOperation() {
        return !success || (httpStatus != null && httpStatus >= 400);
    }

    public boolean isSlowOperation() {
        return durationMs != null && durationMs > 5000; // Plus de 5 secondes
    }

    public String getDurationDescription() {
        if (durationMs == null) {
            return "Unknown";
        }

        if (durationMs < 1000) {
            return durationMs + "ms";
        } else {
            return String.format("%.2fs", durationMs / 1000.0);
        }
    }

    public String getRiskLevel() {
        if (riskScore == null) {
            return "Unknown";
        }

        if (riskScore >= 80) {
            return "Critical";
        } else if (riskScore >= 60) {
            return "High";
        } else if (riskScore >= 40) {
            return "Medium";
        } else if (riskScore >= 20) {
            return "Low";
        } else {
            return "Minimal";
        }
    }

    @PrePersist
    public void prePersist() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }

        // Calcul automatique du score de risque pour certaines actions
        if (riskScore == null) {
            calculateRiskScore();
        }
    }

    private void calculateRiskScore() {
        int score = 0;

        // Actions sensibles
        if (action.contains("DELETE") || action.contains("ADMIN")) {
            score += 30;
        } else if (action.contains("UPDATE") || action.contains("MODIFY")) {
            score += 15;
        } else if (action.contains("LOGIN") || action.contains("ACCESS")) {
            score += 10;
        }

        // Échecs
        if (!success) {
            score += 20;
        }

        // Statuts HTTP d'erreur
        if (httpStatus != null && httpStatus >= 400) {
            score += 15;
        }

        // Opérations lentes (possibles attaques)
        if (isSlowOperation()) {
            score += 10;
        }

        // Catégorie sécurité
        if (isSecurityRelated()) {
            score += 20;
        }

        this.riskScore = Math.min(100, score);

        // Flag automatique pour les scores élevés
        if (riskScore >= 70) {
            flag("High risk score: " + riskScore);
        }
    }
}
