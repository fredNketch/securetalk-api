package com.securetalk.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO de réponse pour les notifications
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NotificationDto {

    private Long id;

    private String type; // MESSAGE, SYSTEM, WARNING, etc.

    private String title;

    private String message;

    private UserDto sender;

    private LocalDateTime timestamp;

    private Boolean isRead;

    private LocalDateTime readAt;

    private String priority; // LOW, NORMAL, HIGH, URGENT

    private Map<String, Object> metadata; // Données additionnelles
}
