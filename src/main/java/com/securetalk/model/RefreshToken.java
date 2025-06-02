package com.securetalk.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entité représentant un token de rafraîchissement
 */
@Entity
@Table(name = "refresh_tokens",
        uniqueConstraints = @UniqueConstraint(columnNames = "token"),
        indexes = {
                @Index(name = "idx_refresh_token_token", columnList = "token"),
                @Index(name = "idx_refresh_token_user", columnList = "user_id"),
                @Index(name = "idx_refresh_token_expires", columnList = "expires_at"),
                @Index(name = "idx_refresh_token_active", columnList = "is_active")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"user"})
public class RefreshToken extends BaseEntity {

    @Column(name = "token", nullable = false, unique = true)
    private String token;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_refresh_token_user"))
    private User user;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "is_revoked", nullable = false)
    @Builder.Default
    private Boolean isRevoked = false;

    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

    @Column(name = "revoked_reason", length = 100)
    private String revokedReason;

    @Column(name = "device_info", length = 500)
    private String deviceInfo;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 1000)
    private String userAgent;

    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;

    @Column(name = "usage_count")
    @Builder.Default
    private Integer usageCount = 0;

    // Méthodes utilitaires
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean isValid() {
        return isActive && !isRevoked && !isExpired();
    }

    public void revoke(String reason) {
        this.isRevoked = true;
        this.isActive = false;
        this.revokedAt = LocalDateTime.now();
        this.revokedReason = reason;
    }

    public void deactivate() {
        this.isActive = false;
    }

    public void markAsUsed() {
        this.lastUsedAt = LocalDateTime.now();
        this.usageCount++;
    }

    public boolean isRecentlyUsed() {
        return lastUsedAt != null &&
                lastUsedAt.isAfter(LocalDateTime.now().minusMinutes(5));
    }

    @PrePersist
    public void prePersist() {
        if (expiresAt == null) {
            // Token valide pour 7 jours par défaut
            expiresAt = LocalDateTime.now().plusDays(7);
        }
    }
}
