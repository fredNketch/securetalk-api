package com.securetalk.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entité représentant une session utilisateur
 */
@Entity
@Table(name = "user_sessions",
        uniqueConstraints = @UniqueConstraint(columnNames = "session_id"),
        indexes = {
                @Index(name = "idx_user_session_session_id", columnList = "session_id"),
                @Index(name = "idx_user_session_user", columnList = "user_id"),
                @Index(name = "idx_user_session_active", columnList = "is_active"),
                @Index(name = "idx_user_session_expires", columnList = "expires_at"),
                @Index(name = "idx_user_session_last_activity", columnList = "last_activity")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"user"})
public class UserSession extends BaseEntity {

    @Column(name = "session_id", nullable = false, unique = true)
    private String sessionId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_user_session_user"))
    private User user;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 1000)
    private String userAgent;

    @Column(name = "device_type", length = 50)
    private String deviceType; // DESKTOP, MOBILE, TABLET

    @Column(name = "device_name", length = 100)
    private String deviceName;

    @Column(name = "browser_name", length = 50)
    private String browserName;

    @Column(name = "browser_version", length = 20)
    private String browserVersion;

    @Column(name = "os_name", length = 50)
    private String osName;

    @Column(name = "os_version", length = 20)
    private String osVersion;

    @Column(name = "location_country", length = 3)
    private String locationCountry;

    @Column(name = "location_city", length = 100)
    private String locationCity;

    @Column(name = "login_time", nullable = false)
    @Builder.Default
    private LocalDateTime loginTime = LocalDateTime.now();

    @Column(name = "last_activity", nullable = false)
    @Builder.Default
    private LocalDateTime lastActivity = LocalDateTime.now();

    @Column(name = "logout_time")
    private LocalDateTime logoutTime;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "is_current", nullable = false)
    @Builder.Default
    private Boolean isCurrent = false;

    @Column(name = "session_data", columnDefinition = "TEXT")
    private String sessionData; // Données de session JSON

    @Column(name = "logout_reason", length = 50)
    private String logoutReason; // MANUAL, TIMEOUT, FORCE, SECURITY

    @Column(name = "login_method", length = 20)
    @Builder.Default
    private String loginMethod = "PASSWORD"; // PASSWORD, REFRESH_TOKEN, SSO

    @Column(name = "activity_count")
    @Builder.Default
    private Integer activityCount = 0;

    // Méthodes utilitaires
    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean isValid() {
        return isActive && !isExpired();
    }

    public boolean isInactive(int timeoutMinutes) {
        return lastActivity.isBefore(LocalDateTime.now().minusMinutes(timeoutMinutes));
    }

    public void updateActivity() {
        this.lastActivity = LocalDateTime.now();
        this.activityCount++;
    }

    public void logout(String reason) {
        this.isActive = false;
        this.isCurrent = false;
        this.logoutTime = LocalDateTime.now();
        this.logoutReason = reason;
    }

    public void expire() {
        logout("TIMEOUT");
    }

    public void forceLogout() {
        logout("FORCE");
    }

    public Long getDurationInMinutes() {
        LocalDateTime endTime = logoutTime != null ? logoutTime : LocalDateTime.now();
        return java.time.Duration.between(loginTime, endTime).toMinutes();
    }

    public String getDeviceFingerprint() {
        return String.format("%s_%s_%s_%s",
                deviceType, osName, browserName,
                ipAddress != null ? ipAddress.hashCode() : "unknown");
    }

    public boolean isSameDevice(UserSession other) {
        return getDeviceFingerprint().equals(other.getDeviceFingerprint());
    }

    public String getLocationInfo() {
        if (locationCountry != null && locationCity != null) {
            return locationCity + ", " + locationCountry;
        } else if (locationCountry != null) {
            return locationCountry;
        }
        return "Unknown";
    }

    @PrePersist
    public void prePersist() {
        if (loginTime == null) {
            loginTime = LocalDateTime.now();
        }
        if (lastActivity == null) {
            lastActivity = LocalDateTime.now();
        }
        if (expiresAt == null) {
            // Session expire après 24 heures par défaut
            expiresAt = LocalDateTime.now().plusHours(24);
        }
    }

    @PreUpdate
    public void preUpdate() {
        // Mise à jour automatique de lastActivity si la session est encore active
        if (isActive && lastActivity.isBefore(LocalDateTime.now().minusMinutes(1))) {
            updateActivity();
        }
    }
}
