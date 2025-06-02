package com.securetalk.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entité représentant le blocage d'un utilisateur par un autre
 */
@Entity
@Table(name = "blocked_users",
        uniqueConstraints = @UniqueConstraint(columnNames = {"blocker_id", "blocked_id"}),
        indexes = {
                @Index(name = "idx_blocked_user_blocker", columnList = "blocker_id"),
                @Index(name = "idx_blocked_user_blocked", columnList = "blocked_id"),
                @Index(name = "idx_blocked_user_active", columnList = "is_active"),
                @Index(name = "idx_blocked_user_expires", columnList = "expires_at")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"blocker", "blocked"})
public class BlockedUser extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "blocker_id", nullable = false, foreignKey = @ForeignKey(name = "fk_blocked_user_blocker"))
    private User blocker; // Utilisateur qui bloque

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "blocked_id", nullable = false, foreignKey = @ForeignKey(name = "fk_blocked_user_blocked"))
    private User blocked; // Utilisateur bloqué

    @Column(name = "reason", length = 500)
    private String reason; // Raison du blocage

    @Column(name = "blocked_at", nullable = false)
    @Builder.Default
    private LocalDateTime blockedAt = LocalDateTime.now();

    @Column(name = "expires_at")
    private LocalDateTime expiresAt; // Blocage temporaire

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "unblocked_at")
    private LocalDateTime unblockedAt;

    @Column(name = "unblocked_by")
    private Long unblockedBy; // ID de l'utilisateur qui a débloqué (admin ou blocker)

    @Column(name = "unblock_reason", length = 500)
    private String unblockReason;

    @Column(name = "block_type", length = 20)
    @Builder.Default
    private String blockType = "MANUAL"; // MANUAL, AUTOMATIC, ADMIN

    @Column(name = "severity", length = 20)
    @Builder.Default
    private String severity = "NORMAL"; // LOW, NORMAL, HIGH, CRITICAL

    @Column(name = "notification_sent", nullable = false)
    @Builder.Default
    private Boolean notificationSent = false;

    @Column(name = "admin_reviewed", nullable = false)
    @Builder.Default
    private Boolean adminReviewed = false;

    @Column(name = "admin_review_date")
    private LocalDateTime adminReviewDate;

    @Column(name = "admin_reviewer_id")
    private Long adminReviewerId;

    @Column(name = "admin_notes", length = 1000)
    private String adminNotes;

    // Méthodes utilitaires
    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean isCurrentlyActive() {
        return isActive && !isExpired();
    }

    public void unblock(Long unblockedByUserId, String reason) {
        this.isActive = false;
        this.unblockedAt = LocalDateTime.now();
        this.unblockedBy = unblockedByUserId;
        this.unblockReason = reason;
    }

    public void expire() {
        unblock(null, "EXPIRED");
    }

    public boolean isTemporary() {
        return expiresAt != null;
    }

    public boolean isPermanent() {
        return expiresAt == null;
    }

    public Long getDurationInHours() {
        if (!isTemporary()) {
            return null;
        }

        LocalDateTime endTime = unblockedAt != null ? unblockedAt :
                (isExpired() ? expiresAt : LocalDateTime.now());
        return java.time.Duration.between(blockedAt, endTime).toHours();
    }

    public boolean canBeUnblockedBy(User user) {
        // Le blocker original peut toujours débloquer
        if (blocker.getId().equals(user.getId())) {
            return true;
        }

        // Les admins peuvent débloquer
        if (user.isAdmin()) {
            return true;
        }

        return false;
    }

    public void markAdminReviewed(Long adminId, String notes) {
        this.adminReviewed = true;
        this.adminReviewDate = LocalDateTime.now();
        this.adminReviewerId = adminId;
        this.adminNotes = notes;
    }

    public boolean requiresAdminReview() {
        return "HIGH".equals(severity) || "CRITICAL".equals(severity) ||
                "ADMIN".equals(blockType);
    }

    public String getBlockDurationDescription() {
        if (isPermanent()) {
            return "Permanent";
        }

        Long hours = getDurationInHours();
        if (hours == null) {
            return "Unknown";
        }

        if (hours < 24) {
            return hours + " heure(s)";
        } else {
            long days = hours / 24;
            return days + " jour(s)";
        }
    }

    public boolean isUserBlocked(Long userId1, Long userId2) {
        return (blocker.getId().equals(userId1) && blocked.getId().equals(userId2)) ||
                (blocker.getId().equals(userId2) && blocked.getId().equals(userId1));
    }

    @PrePersist
    public void prePersist() {
        if (blockedAt == null) {
            blockedAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    public void preUpdate() {
        // Vérifier l'expiration automatiquement
        if (isTemporary() && isExpired() && isActive) {
            expire();
        }
    }
}
