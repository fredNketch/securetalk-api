package com.securetalk.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO de réponse pour les messages
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MessageDto {

    private Long id;

    private UserDto sender;

    private UserDto recipient;

    private String content; // Contenu déchiffré

    private String messageType;

    private LocalDateTime timestamp;

    private Boolean isRead;

    private LocalDateTime readAt;

    private Boolean isEdited;

    private LocalDateTime editedAt;

    private Boolean isDeleted;

    private LocalDateTime deletedAt;
}
