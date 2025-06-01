package com.securetalk.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.securetalk.model.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * DTO de réponse pour les utilisateurs
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDto {

    private Long id;

    private String username;

    private String email;

    private Set<Role> roles;

    private Boolean enabled;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime lastLogin;

    private String firstName;

    private String lastName;

    private String bio;

    private Boolean isOnline;

    private LocalDateTime lastSeen;

    // Statistiques pour les admins
    private Long totalMessagesSent;

    private Long totalMessagesReceived;
}
