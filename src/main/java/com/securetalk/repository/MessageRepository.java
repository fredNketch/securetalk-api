// ===== MessageRepository.java =====
package com.securetalk.repository;

import com.securetalk.model.Message;
import com.securetalk.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository pour la gestion des messages
 */
@Repository
public interface MessageRepository extends JpaRepository<Message, Long>, JpaSpecificationExecutor<Message> {

    // ===== CONVERSATIONS =====

    /**
     * Trouve tous les messages entre deux utilisateurs, triés par date
     */
    @Query("SELECT m FROM Message m WHERE " +
            "((m.sender.id = :userId1 AND m.recipient.id = :userId2) OR " +
            " (m.sender.id = :userId2 AND m.recipient.id = :userId1)) " +
            "AND m.isDeleted = false " +
            "ORDER BY m.timestamp ASC")
    List<Message> findConversationBetweenUsers(@Param("userId1") Long userId1,
                                               @Param("userId2") Long userId2);

    /**
     * Trouve les messages d'une conversation avec pagination
     */
    @Query("SELECT m FROM Message m WHERE " +
            "((m.sender.id = :userId1 AND m.recipient.id = :userId2) OR " +
            " (m.sender.id = :userId2 AND m.recipient.id = :userId1)) " +
            "AND m.isDeleted = false " +
            "ORDER BY m.timestamp DESC")
    Page<Message> findConversationBetweenUsers(@Param("userId1") Long userId1,
                                               @Param("userId2") Long userId2,
                                               Pageable pageable);

    /**
     * Trouve le dernier message entre deux utilisateurs
     */
    @Query("SELECT m FROM Message m WHERE " +
            "((m.sender.id = :userId1 AND m.recipient.id = :userId2) OR " +
            " (m.sender.id = :userId2 AND m.recipient.id = :userId1)) " +
            "AND m.isDeleted = false " +
            "ORDER BY m.timestamp DESC " +
            "LIMIT 1")
    Optional<Message> findLastMessageBetweenUsers(@Param("userId1") Long userId1,
                                                  @Param("userId2") Long userId2);

    /**
     * Trouve toutes les conversations d'un utilisateur avec les derniers messages
     */
    @Query("SELECT CASE WHEN m.sender.id = :userId THEN m.recipient ELSE m.sender END as participant, " +
            "m as lastMessage, " +
            "COUNT(CASE WHEN m.recipient.id = :userId AND m.isRead = false AND m.isDeleted = false THEN 1 END) as unreadCount " +
            "FROM Message m WHERE (m.sender.id = :userId OR m.recipient.id = :userId) " +
            "AND m.isDeleted = false " +
            "GROUP BY CASE WHEN m.sender.id = :userId THEN m.recipient.id ELSE m.sender.id END " +
            "ORDER BY MAX(m.timestamp) DESC")
    List<Object[]> findConversationsForUser(@Param("userId") Long userId);

    // ===== MESSAGES NON LUS =====

    /**
     * Trouve tous les messages non lus pour un utilisateur
     */
    @Query("SELECT m FROM Message m WHERE m.recipient.id = :userId AND m.isRead = false AND m.isDeleted = false")
    List<Message> findUnreadMessagesForUser(@Param("userId") Long userId);

    /**
     * Compte les messages non lus pour un utilisateur
     */
    @Query("SELECT COUNT(m) FROM Message m WHERE m.recipient.id = :userId AND m.isRead = false AND m.isDeleted = false")
    long countUnreadMessagesForUser(@Param("userId") Long userId);

    /**
     * Compte les messages non lus entre deux utilisateurs
     */
    @Query("SELECT COUNT(m) FROM Message m WHERE m.sender.id = :senderId AND m.recipient.id = :recipientId " +
            "AND m.isRead = false AND m.isDeleted = false")
    long countUnreadMessagesBetweenUsers(@Param("senderId") Long senderId, @Param("recipientId") Long recipientId);

    /**
     * Trouve les messages non lus d'une conversation spécifique
     */
    @Query("SELECT m FROM Message m WHERE m.sender.id = :senderId AND m.recipient.id = :recipientId " +
            "AND m.isRead = false AND m.isDeleted = false ORDER BY m.timestamp ASC")
    List<Message> findUnreadMessagesBetweenUsers(@Param("senderId") Long senderId,
                                                 @Param("recipientId") Long recipientId);

    // ===== RECHERCHE DE MESSAGES =====

    /**
     * Recherche de messages par contenu (nécessite déchiffrement côté application)
     * Cette requête retourne tous les messages, le filtrage par contenu se fait côté service
     */
    @Query("SELECT m FROM Message m WHERE " +
            "(m.sender.id = :userId OR m.recipient.id = :userId) " +
            "AND m.isDeleted = false " +
            "AND m.timestamp >= :startDate AND m.timestamp <= :endDate " +
            "ORDER BY m.timestamp DESC")
    List<Message> findMessagesForUserInDateRange(@Param("userId") Long userId,
                                                 @Param("startDate") LocalDateTime startDate,
                                                 @Param("endDate") LocalDateTime endDate);

    /**
     * Recherche de messages par type
     */
    @Query("SELECT m FROM Message m WHERE " +
            "(m.sender.id = :userId OR m.recipient.id = :userId) " +
            "AND m.messageType = :messageType AND m.isDeleted = false " +
            "ORDER BY m.timestamp DESC")
    List<Message> findMessagesByType(@Param("userId") Long userId,
                                     @Param("messageType") String messageType);

    // ===== MESSAGES PAR UTILISATEUR =====

    /**
     * Trouve tous les messages envoyés par un utilisateur
     */
    List<Message> findBySenderAndIsDeletedFalseOrderByTimestampDesc(User sender);

    /**
     * Trouve tous les messages reçus par un utilisateur
     */
    List<Message> findByRecipientAndIsDeletedFalseOrderByTimestampDesc(User recipient);

    /**
     * Trouve tous les messages d'un utilisateur (envoyés et reçus)
     */
    @Query("SELECT m FROM Message m WHERE " +
            "(m.sender.id = :userId OR m.recipient.id = :userId) " +
            "AND m.isDeleted = false " +
            "ORDER BY m.timestamp DESC")
    List<Message> findAllMessagesForUser(@Param("userId") Long userId);

    /**
     * Trouve les messages récents d'un utilisateur
     */
    @Query("SELECT m FROM Message m WHERE " +
            "(m.sender.id = :userId OR m.recipient.id = :userId) " +
            "AND m.timestamp >= :since AND m.isDeleted = false " +
            "ORDER BY m.timestamp DESC")
    List<Message> findRecentMessagesForUser(@Param("userId") Long userId,
                                            @Param("since") LocalDateTime since);

    // ===== STATISTIQUES =====

    /**
     * Compte le total des messages
     */
    long countByIsDeletedFalse();

    /**
     * Compte les messages envoyés par un utilisateur
     */
    long countBySenderAndIsDeletedFalse(User sender);

    /**
     * Compte les messages reçus par un utilisateur
     */
    long countByRecipientAndIsDeletedFalse(User recipient);

    /**
     * Statistiques des messages par jour
     */
    @Query("SELECT DATE(m.timestamp) as date, COUNT(m) as count " +
            "FROM Message m WHERE m.timestamp >= :startDate AND m.isDeleted = false " +
            "GROUP BY DATE(m.timestamp) ORDER BY date")
    List<Object[]> getMessageStatisticsByDay(@Param("startDate") LocalDateTime startDate);

    /**
     * Statistiques des messages par utilisateur
     */
    @Query("SELECT m.sender, COUNT(m) as messageCount " +
            "FROM Message m WHERE m.isDeleted = false " +
            "GROUP BY m.sender ORDER BY messageCount DESC")
    List<Object[]> getMessageStatisticsByUser(Pageable pageable);

    /**
     * Messages par heure pour les dernières 24h
     */
    @Query("SELECT HOUR(m.timestamp) as hour, COUNT(m) as count " +
            "FROM Message m WHERE m.timestamp >= :since AND m.isDeleted = false " +
            "GROUP BY HOUR(m.timestamp) ORDER BY hour")
    List<Object[]> getHourlyMessageStatistics(@Param("since") LocalDateTime since);

    // ===== MISE À JOUR EN MASSE =====

    /**
     * Marque un message comme lu
     */
    @Modifying
    @Query("UPDATE Message m SET m.isRead = true, m.readAt = :readTime WHERE m.id = :messageId")
    void markAsRead(@Param("messageId") Long messageId, @Param("readTime") LocalDateTime readTime);

    /**
     * Marque tous les messages d'une conversation comme lus
     */
    @Modifying
    @Query("UPDATE Message m SET m.isRead = true, m.readAt = :readTime " +
            "WHERE m.sender.id = :senderId AND m.recipient.id = :recipientId " +
            "AND m.isRead = false AND m.isDeleted = false")
    void markConversationAsRead(@Param("senderId") Long senderId,
                                @Param("recipientId") Long recipientId,
                                @Param("readTime") LocalDateTime readTime);

    /**
     * Marque un message comme délivré
     */
    @Modifying
    @Query("UPDATE Message m SET m.deliveredAt = :deliveredTime WHERE m.id = :messageId")
    void markAsDelivered(@Param("messageId") Long messageId, @Param("deliveredTime") LocalDateTime deliveredTime);

    /**
     * Supprime logiquement un message
     */
    @Modifying
    @Query("UPDATE Message m SET m.isDeleted = true, m.deletedAt = :deletedTime, m.deletedBy = :deletedBy " +
            "WHERE m.id = :messageId")
    void softDeleteMessage(@Param("messageId") Long messageId,
                           @Param("deletedTime") LocalDateTime deletedTime,
                           @Param("deletedBy") Long deletedBy);

    /**
     * Supprime logiquement tous les messages d'un utilisateur
     */
    @Modifying
    @Query("UPDATE Message m SET m.isDeleted = true, m.deletedAt = :deletedTime, m.deletedBy = :deletedBy " +
            "WHERE m.sender.id = :userId OR m.recipient.id = :userId")
    void softDeleteAllMessagesForUser(@Param("userId") Long userId,
                                      @Param("deletedTime") LocalDateTime deletedTime,
                                      @Param("deletedBy") Long deletedBy);

    /**
     * Supprime logiquement une conversation complète
     */
    @Modifying
    @Query("UPDATE Message m SET m.isDeleted = true, m.deletedAt = :deletedTime, m.deletedBy = :deletedBy " +
            "WHERE ((m.sender.id = :userId1 AND m.recipient.id = :userId2) OR " +
            "       (m.sender.id = :userId2 AND m.recipient.id = :userId1))")
    void softDeleteConversation(@Param("userId1") Long userId1,
                                @Param("userId2") Long userId2,
                                @Param("deletedTime") LocalDateTime deletedTime,
                                @Param("deletedBy") Long deletedBy);

    // ===== NETTOYAGE ET MAINTENANCE =====

    /**
     * Trouve les messages supprimés logiquement depuis plus de X jours
     */
    @Query("SELECT m FROM Message m WHERE m.isDeleted = true AND m.deletedAt < :cutoffDate")
    List<Message> findSoftDeletedMessagesOlderThan(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Supprime physiquement les messages supprimés logiquement depuis plus de X jours
     */
    @Modifying
    @Query("DELETE FROM Message m WHERE m.isDeleted = true AND m.deletedAt < :cutoffDate")
    void deletePermanentlySoftDeletedMessagesOlderThan(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Trouve les messages non lus anciens (plus de X jours)
     */
    @Query("SELECT m FROM Message m WHERE m.isRead = false AND m.timestamp < :cutoffDate AND m.isDeleted = false")
    List<Message> findOldUnreadMessages(@Param("cutoffDate") LocalDateTime cutoffDate);

    // ===== SÉCURITÉ ET AUDIT =====

    /**
     * Trouve les messages par adresse IP (pour audit de sécurité)
     */
    @Query("SELECT m FROM Message m WHERE m.ipAddress = :ipAddress AND m.timestamp >= :since")
    List<Message> findMessagesByIpAddress(@Param("ipAddress") String ipAddress,
                                          @Param("since") LocalDateTime since);

    /**
     * Trouve les messages suspects (trop de messages en peu de temps)
     */
    @Query("SELECT m.sender, COUNT(m) as messageCount " +
            "FROM Message m WHERE m.timestamp >= :since " +
            "GROUP BY m.sender " +
            "HAVING COUNT(m) > :threshold " +
            "ORDER BY messageCount DESC")
    List<Object[]> findSuspiciousMessageActivity(@Param("since") LocalDateTime since,
                                                 @Param("threshold") Long threshold);

    /**
     * Vérifie si un utilisateur peut envoyer un message (pas de spam)
     */
    @Query("SELECT COUNT(m) FROM Message m WHERE m.sender.id = :senderId " +
            "AND m.timestamp >= :since")
    long countRecentMessagesBySender(@Param("senderId") Long senderId,
                                     @Param("since") LocalDateTime since);
}