package com.securetalk.dto.request.message;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO pour la mise à jour de messages
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateMessageRequest {

    @NotBlank(message = "Le nouveau contenu est requis")
    @Size(min = 1, max = 1000, message = "Le message doit contenir entre 1 et 1000 caractères")
    private String content;
}