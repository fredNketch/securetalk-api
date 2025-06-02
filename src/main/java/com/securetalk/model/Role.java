package com.securetalk.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entité pour la configuration système de l'application
 */
@Entity
@Table(name = "system_configs",
        uniqueConstraints = @UniqueConstraint(columnNames = "config_key"),
        indexes = {
                @Index(name = "idx_system_config_key", columnList = "config_key"),
                @Index(name = "idx_system_config_category", columnList = "category"),
                @Index(name = "idx_system_config_active", columnList = "is_active"),
                @Index(name = "idx_system_config_public", columnList = "is_public")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class SystemConfig extends AuditableEntity {

    @Column(name = "config_key", nullable = false, unique = true, length = 100)
    private String configKey;

    @Column(name = "config_value", columnDefinition = "TEXT")
    private String configValue;

    @Column(name = "default_value", columnDefinition = "TEXT")
    private String defaultValue;

    @Column(name = "category", length = 50)
    @Builder.Default
    private String category = "GENERAL"; // SECURITY, UI, EMAIL, SMS, API, DATABASE, etc.

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "data_type", length = 20)
    @Builder.Default
    private String dataType = "STRING"; // STRING, INTEGER, BOOLEAN, JSON, URL, EMAIL

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "is_public", nullable = false)
    @Builder.Default
    private Boolean isPublic = false; // Peut être exposé aux clients

    @Column(name = "is_sensitive", nullable = false)
    @Builder.Default
    private Boolean isSensitive = false; // Données sensibles (mots de passe, clés)

    @Column(name = "is_required", nullable = false)
    @Builder.Default
    private Boolean isRequired = false;

    @Column(name = "validation_regex", length = 500)
    private String validationRegex;

    @Column(name = "min_value")
    private Long minValue;

    @Column(name = "max_value")
    private Long maxValue;

    @Column(name = "possible_values", length = 1000)
    private String possibleValues; // Valeurs possibles séparées par des virgules

    @Column(name = "last_modified_by")
    private Long lastModifiedBy;

    @Column(name = "last_modified_at")
    private LocalDateTime lastModifiedAt;

    @Column(name = "environment", length = 20)
    @Builder.Default
    private String environment = "ALL"; // DEV, PROD, TEST, ALL

    @Column(name = "restart_required", nullable = false)
    @Builder.Default
    private Boolean restartRequired = false;

    @Column(name = "cache_ttl")
    private Integer cacheTtl; // Durée de cache en secondes

    @Column(name = "backup_value", columnDefinition = "TEXT")
    private String backupValue; // Valeur de sauvegarde avant modification

    @Column(name = "change_reason", length = 500)
    private String changeReason;

    @Column(name = "tags", length = 200)
    private String tags; // Tags séparés par des virgules pour le filtrage

    // Méthodes utilitaires
    public String getValue() {
        return isActive && configValue != null ? configValue : defaultValue;
    }

    public String getStringValue() {
        return getValue();
    }

    public Integer getIntegerValue() {
        String value = getValue();
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public Long getLongValue() {
        String value = getValue();
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public Boolean getBooleanValue() {
        String value = getValue();
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return Boolean.parseBoolean(value.trim());
    }

    public Double getDoubleValue() {
        String value = getValue();
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public void setValue(String newValue, Long modifiedBy, String reason) {
        this.backupValue = this.configValue;
        this.configValue = newValue;
        this.lastModifiedBy = modifiedBy;
        this.lastModifiedAt = LocalDateTime.now();
        this.changeReason = reason;
    }

    public void revertToPreviousValue(Long modifiedBy, String reason) {
        if (backupValue != null) {
            setValue(backupValue, modifiedBy, reason);
        }
    }

    public void resetToDefault(Long modifiedBy, String reason) {
        setValue(defaultValue, modifiedBy, reason);
    }

    public boolean isValid() {
        String value = getValue();

        if (isRequired && (value == null || value.trim().isEmpty())) {
            return false;
        }

        if (value == null || value.trim().isEmpty()) {
            return true; // Valeur vide acceptable si pas requis
        }

        // Validation par type
        switch (dataType.toUpperCase()) {
            case "INTEGER":
                try {
                    long longValue = Long.parseLong(value.trim());
                    if (minValue != null && longValue < minValue) return false;
                    if (maxValue != null && longValue > maxValue) return false;
                } catch (NumberFormatException e) {
                    return false;
                }
                break;

            case "BOOLEAN":
                if (!"true".equalsIgnoreCase(value.trim()) &&
                        !"false".equalsIgnoreCase(value.trim())) {
                    return false;
                }
                break;

            case "EMAIL":
                if (!value.contains("@") || !value.contains(".")) {
                    return false;
                }
                break;

            case "URL":
                if (!value.startsWith("http://") && !value.startsWith("https://")) {
                    return false;
                }
                break;
        }

        // Validation par regex
        if (validationRegex != null && !value.matches(validationRegex)) {
            return false;
        }

        // Validation des valeurs possibles
        if (possibleValues != null && !possibleValues.trim().isEmpty()) {
            String[] values = possibleValues.split(",");
            boolean found = false;
            for (String possibleValue : values) {
                if (possibleValue.trim().equals(value.trim())) {
                    found = true;
                    break;
                }
            }
            if (!found) return false;
        }

        return true;
    }

    public boolean hasChanged() {
        return !getValue().equals(defaultValue);
    }

    public boolean requiresRestart() {
        return restartRequired && hasChanged();
    }

    public String getDisplayValue() {
        if (isSensitive && configValue != null && !configValue.isEmpty()) {
            return "***HIDDEN***";
        }
        return getValue();
    }

    public boolean isApplicableToEnvironment(String currentEnv) {
        return "ALL".equals(environment) ||
                currentEnv.equalsIgnoreCase(environment);
    }

    public boolean hasTag(String tag) {
        return tags != null &&
                java.util.Arrays.asList(tags.split(","))
                        .stream()
                        .anyMatch(t -> t.trim().equalsIgnoreCase(tag.trim()));
    }

    @PreUpdate
    public void preUpdate() {
        lastModifiedAt = LocalDateTime.now();
    }

    // Méthodes statiques pour les clés communes
    public static String getJwtSecretKey() {
        return "security.jwt.secret";
    }

    public static String getJwtExpirationKey() {
        return "security.jwt.expiration";
    }

    public static String getMaxLoginAttemptsKey() {
        return "security.max.login.attempts";
    }

    public static String getPasswordPolicyKey() {
        return "security.password.policy";
    }

    public static String getMessageMaxLengthKey() {
        return "message.max.length";
    }

    public static String getEmailEnabledKey() {
        return "email.enabled";
    }

    public static String getMaintenanceModeKey() {
        return "system.maintenance.mode";
    }

    public static String getRegistrationEnabledKey() {
        return "user.registration.enabled";
    }
}
