package com.securetalk.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO de réponse pour le health check
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HealthResponse {

    private String status; // UP, DOWN, DEGRADED

    private LocalDateTime timestamp;

    private String version;

    private Map<String, Object> components; // Database, Redis, etc.

    private Long uptime; // En secondes

    private Map<String, Object> metrics; // Métriques système
}
