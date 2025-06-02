package com.securetalk.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entité représentant un message échangé entre utilisateurs
 */
@Entity
@Table(name = "messages",
        indexes = {
                @Index(name = "idx_message_sender", columnList = "sender_id"),
                @Index(name = "idx_message_recipient", columnList = "recipient_id"),
                @Index(name = "idx_message_timestamp", columnList = "timestamp"),
                @Index(name = "idx_message_conversation", columnList = "sender_id, recipient_id"),
                @Index(name = "idx_message_read", columnList = "is_read"),
                @Index(name = "idx_message_deleted", columnList = "is_deleted")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"sender", "recipient"})
public class Message extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sender_id", nullable = false, foreignKey = @ForeignKey(name = "fk_message_sender"))
    private User sender;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recipient_id", nullable = false, foreignKey = @ForeignKey(name = "fk_message_recipient"))
    private User recipient;

    @Column(name = "encrypted_content", nullable = false, columnDefinition = "TEXT")
    private String encryptedContent;

    @Transient
    private String content; // Contenu déchiffré (non persisté)

    @Column(name = "message_type", length = 50)
    @Builder.Default
    private String messageType = "TEXT";

    @Column(name = "timestamp", nullable = false)
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private Boolean isRead = false;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @Column(name = "is_edited", nullable = false)
    @Builder.Default
    private Boolean isEdited = false;

    @Column(name = "edited_at")
    private LocalDateTime editedAt;

    @Column(name = "original_content", columnDefinition = "TEXT")
    private String originalContent; // Contenu original avant modification

    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private Boolean isDeleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "deleted_by")
    private Long deletedBy; // ID de l'utilisateur qui a supprimé

    @Column(name = "priority", length = 20)
    @Builder.Default
    private String priority = "NORMAL"; // LOW, NORMAL, HIGH, URGENT

    @Column(name = "reply_to_message_id")
    private Long replyToMessageId; // Pour les réponses

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reply_to_message_id", insertable = false, updatable = false)
    private Message replyToMessage;

    @Column(name = "message_size")
    private Integer messageSize; // Taille du message en caractères

    @Column(name = "encryption_version", length = 10)
    @Builder.Default
    private String encryptionVersion = "1.0";

    // Métadonnées additionnelles
    @Column(name = "client_ip", length = 45)
    private String clientIp;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(name = "device_info", length = 200)
    private String deviceInfo;

    // Méthodes utilitaires
    public void markAsRead() {
        this.isRead = true;
        this.readAt = LocalDateTime.now();
    }

    public void markAsEdited() {
        this.isEdited = true;
        this.editedAt = LocalDateTime.now();
    }

    public void markAsDeleted(Long deletedByUserId) {
        this.isDeleted = true;
        this.deletedAt = LocalDateTime.now();
        this.deletedBy = deletedByUserId;
    }

    public boolean canBeEditedBy(User user) {
        return sender.getId().equals(user.getId()) &&
                !isDeleted &&
                timestamp.isAfter(LocalDateTime.now().minusHours(24));
    }

    public boolean canBeDeletedBy(User user) {
        return sender.getId().equals(user.getId()) ||
                recipient.getId().equals(user.getId()) ||
                user.isAdmin();
    }

    public boolean isFromUser(User user) {
        return sender.getId().equals(user.getId());
    }

    public boolean isToUser(User user) {
        return recipient.getId().equals(user.getId());
    }

    public boolean isInConversationWith(User user) {
        return isFromUser(user) || isToUser(user);
    }

    public User getOtherParticipant(User currentUser) {
        if (isFromUser(currentUser)) {
            return recipient;
        } else if (isToUser(currentUser)) {
            return sender;
        }
        return null;
    }

    public String getConversationId() {
        Long userId1 = Math.min(sender.getId(), recipient.getId());
        Long userId2 = Math.max(sender.getId(), recipient.getId());
        return userId1 + "_" + userId2;
    }

    public boolean isReply() {
        return replyToMessageId != null;
    }

    @PrePersist
    public void prePersist() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
        if (messageSize == null && content != null) {
            messageSize = content.length();
        }
    }

    @PreUpdate
    public void preUpdate() {
        if (content != null) {
            messageSize = content.length();
        }
    }
}
