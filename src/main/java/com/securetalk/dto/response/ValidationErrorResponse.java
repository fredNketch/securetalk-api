package com.securetalk.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * DTO de réponse spécialisé pour les erreurs de validation
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidationErrorResponse {

    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    @Builder.Default
    private Integer status = 400;

    @Builder.Default
    private String error = "Validation Failed";

    private String message;

    private String path;

    private Map<String, String> fieldErrors;

    private List<String> globalErrors;

    private Integer errorCount;
}
