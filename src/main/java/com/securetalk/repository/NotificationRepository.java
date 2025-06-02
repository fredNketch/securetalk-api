// ===== NotificationRepository.java =====
package com.securetalk.repository;

import com.securetalk.model.Notification;
import com.securetalk.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository pour les notifications
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * Trouve toutes les notifications d'un utilisateur
     */
    Page<Notification> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    /**
     * Trouve les notifications non lues d'un utilisateur
     */
    List<Notification> findByUserAndIsReadFalseOrderByCreatedAtDesc(User user);

    /**
     * Compte les notifications non lues d'un utilisateur
     */
    long countByUserAndIsReadFalse(User user);

    /**
     * Trouve les notifications par type
     */
    List<Notification> findByUserAndNotificationTypeOrderByCreatedAtDesc(User user, Notification.NotificationType type);

    /**
     * Trouve les notifications par priorité
     */
    List<Notification> findByUserAndPriorityOrderByCreatedAtDesc(User user, Notification.NotificationPriority priority);

    /**
     * Trouve les notifications non expirées
     */
    @Query("SELECT n FROM Notification n WHERE n.user = :user AND " +
            "(n.expiresAt IS NULL OR n.expiresAt > :now) " +
            "ORDER BY n.createdAt DESC")
    List<Notification> findActiveNotificationsByUser(@Param("user") User user, @Param("now") LocalDateTime now);

    /**
     * Marque toutes les notifications d'un utilisateur comme lues
     */
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = :readTime WHERE n.user = :user AND n.isRead = false")
    void markAllAsReadForUser(@Param("user") User user, @Param("readTime") LocalDateTime readTime);

    /**
     * Supprime les notifications expirées
     */
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.expiresAt IS NOT NULL AND n.expiresAt <= :now")
    void deleteExpiredNotifications(@Param("now") LocalDateTime now);

    /**
     * Supprime les anciennes notifications lues
     */
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.isRead = true AND n.readAt < :cutoffDate")
    void deleteOldReadNotifications(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Statistiques des notifications par type
     */
    @Query("SELECT n.notificationType, COUNT(n) FROM Notification n WHERE n.createdAt >= :since GROUP BY n.notificationType")
    List<Object[]> getNotificationStatisticsByType(@Param("since") LocalDateTime since);
}