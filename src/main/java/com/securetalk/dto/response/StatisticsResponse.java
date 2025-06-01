package com.securetalk.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO de réponse pour les statistiques (admin)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StatisticsResponse {

    private Long totalUsers;

    private Long activeUsers;

    private Long totalMessages;

    private Long messagesLastWeek;

    private Long messagesLastMonth;

    private Map<String, Long> userRegistrations; // Par période

    private Map<String, Long> messagesByDay; // Messages par jour

    private Map<String, Long> topActiveUsers; // Top utilisateurs actifs

    private LocalDateTime lastUpdate;

    private Double averageMessagesPerUser;

    private Long onlineUsers;
}
