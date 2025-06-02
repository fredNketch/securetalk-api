package com.securetalk.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Entité représentant une notification utilisateur
 */
@Entity
@Table(name = "notifications",
        indexes = {
                @Index(name = "idx_notification_user", columnList = "user_id"),
                @Index(name = "idx_notification_type", columnList = "type"),
                @Index(name = "idx_notification_timestamp", columnList = "timestamp"),
                @Index(name = "idx_notification_read", columnList = "is_read"),
                @Index(name = "idx_notification_priority", columnList = "priority"),
                @Index(name = "idx_notification_sender", columnList = "sender_id")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"user", "sender"})
public class Notification extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_notification_user"))
    private User user;

    @Column(name = "type", nullable = false, length = 50)
    private String type; // MESSAGE, SYSTEM, WARNING, ERROR, INFO, SUCCESS

    @Column(name = "title", length = 200)
    private String title;

    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", foreignKey = @ForeignKey(name = "fk_notification_sender"))
    private User sender;

    @Column(name = "timestamp", nullable = false)
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private Boolean isRead = false;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @Column(name = "priority", length = 20)
    @Builder.Default
    private String priority = "NORMAL"; // LOW, NORMAL, HIGH, URGENT

    @Column(name = "category", length = 50)
    private String category; // CHAT, SECURITY, SYSTEM, ADMIN, etc.

    @Column(name = "action_url", length = 500)
    private String actionUrl; // URL vers laquelle rediriger lors du clic

    @Column(name = "action_label", length = 100)
    private String actionLabel; // Texte du bouton d'action

    @ElementCollection
    @CollectionTable(name = "notification_metadata",
            joinColumns = @JoinColumn(name = "notification_id"))
    @MapKeyColumn(name = "meta_key")
    @Column(name = "meta_value", length = 1000)
    private Map<String, String> metadata;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private Boolean isDeleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "delivered", nullable = false)
    @Builder.Default
    private Boolean delivered = false;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    @Column(name = "push_sent", nullable = false)
    @Builder.Default
    private Boolean pushSent = false;

    @Column(name = "push_sent_at")
    private LocalDateTime pushSentAt;

    @Column(name = "email_sent", nullable = false)
    @Builder.Default
    private Boolean emailSent = false;

    @Column(name = "email_sent_at")
    private LocalDateTime emailSentAt;

    @Column(name = "related_entity_type", length = 50)
    private String relatedEntityType; // MESSAGE, USER, SYSTEM, etc.

    @Column(name = "related_entity_id")
    private Long relatedEntityId;

    // Méthodes utilitaires
    public void markAsRead() {
        this.isRead = true;
        this.readAt = LocalDateTime.now();
    }

    public void markAsDelivered() {
        this.delivered = true;
        this.deliveredAt = LocalDateTime.now();
    }

    public void markPushSent() {
        this.pushSent = true;
        this.pushSentAt = LocalDateTime.now();
    }

    public void markEmailSent() {
        this.emailSent = true;
        this.emailSentAt = LocalDateTime.now();
    }

    public void delete() {
        this.isDeleted = true;
        this.deletedAt = LocalDateTime.now();
    }

    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean isActive() {
        return !isDeleted && !isExpired();
    }

    public boolean isHighPriority() {
        return "HIGH".equals(priority) || "URGENT".equals(priority);
    }

    public boolean hasAction() {
        return actionUrl != null && !actionUrl.isEmpty();
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

    public void setRelatedEntity(String entityType, Long entityId) {
        this.relatedEntityType = entityType;
        this.relatedEntityId = entityId;
    }

    public boolean isFromSystem() {
        return sender == null;
    }

    public boolean isFromUser() {
        return sender != null;
    }

    public String getDisplayTitle() {
        if (title != null && !title.isEmpty()) {
            return title;
        }

        // Génération automatique du titre basé sur le type
        switch (type.toUpperCase()) {
            case "MESSAGE":
                return sender != null ? "Nouveau message de " + sender.getFullName() : "Nouveau message";
            case "SYSTEM":
                return "Notification système";
            case "WARNING":
                return "Attention";
            case "ERROR":
                return "Erreur";
            case "SUCCESS":
                return "Succès";
            default:
                return "Notification";
        }
    }

    @PrePersist
    public void prePersist() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }

        // Définir une expiration par défaut basée sur la priorité
        if (expiresAt == null) {
            switch (priority) {
                case "URGENT":
                    expiresAt = timestamp.plusDays(7);
                    break;
                case "HIGH":
                    expiresAt = timestamp.plusDays(14);
                    break;
                case "NORMAL":
                    expiresAt = timestamp.plusDays(30);
                    break;
                case "LOW":
                    expiresAt = timestamp.plusDays(60);
                    break;
                default:
                    expiresAt = timestamp.plusDays(30);
            }
        }
    }
}
