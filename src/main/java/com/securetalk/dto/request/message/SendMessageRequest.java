package com.securetalk.dto.request.message;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO pour l'envoi de messages
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendMessageRequest {

    @NotNull(message = "L'ID du destinataire est requis")
    private Long recipientId;

    @NotBlank(message = "Le contenu du message est requis")
    @Size(min = 1, max = 1000, message = "Le message doit contenir entre 1 et 1000 caractères")
    private String content;

    @Size(max = 100, message = "Le type de message ne peut pas dépasser 100 caractères")
    @Builder.Default
    private String messageType = "TEXT";
}
