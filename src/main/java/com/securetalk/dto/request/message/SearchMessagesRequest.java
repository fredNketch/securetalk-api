package com.securetalk.dto.request.message;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO pour la recherche de messages
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchMessagesRequest {

    @Size(max = 100, message = "Le terme de recherche ne peut pas dépasser 100 caractères")
    private String searchTerm;

    private Long userId;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    @Min(value = 0, message = "La page doit être positive ou nulle")
    @Builder.Default
    private Integer page = 0;

    @Min(value = 1, message = "La taille de page doit être d'au moins 1")
    @Builder.Default
    private Integer size = 20;

    @Builder.Default
    private String sortBy = "timestamp";

    @Builder.Default
    private String sortDirection = "DESC";
}