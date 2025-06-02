// ===== UserSessionRepository.java =====
package com.securetalk.repository;

import com.securetalk.model.User;
import com.securetalk.model.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository pour la gestion des sessions utilisateur
 */
@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, Long> {

    /**
     * Trouve une session par token
     */
    Optional<UserSession> findBySessionToken(String sessionToken);

    /**
     * Trouve toutes les sessions actives d'un utilisateur
     */
    @Query("SELECT us FROM UserSession us WHERE us.user = :user AND us.isActive = true AND us.expiresAt > :now")
    List<UserSession> findActiveSessionsByUser(@Param("user") User user, @Param("now") LocalDateTime now);

    /**
     * Trouve toutes les sessions d'un utilisateur
     */
    List<UserSession> findByUserOrderByCreatedAtDesc(User user);

    /**
     * Trouve les sessions expirées
     */
    @Query("SELECT us FROM UserSession us WHERE us.expiresAt <= :now OR us.isActive = false")
    List<UserSession> findExpiredSessions(@Param("now") LocalDateTime now);

    /**
     * Trouve les sessions par adresse IP
     */
    List<UserSession> findByIpAddressAndCreatedAtAfter(String ipAddress, LocalDateTime since);

    /**
     * Compte les sessions actives par utilisateur
     */
    @Query("SELECT COUNT(us) FROM UserSession us WHERE us.user = :user AND us.isActive = true AND us.expiresAt > :now")
    long countActiveSessionsByUser(@Param("user") User user, @Param("now") LocalDateTime now);

    /**
     * Met à jour l'activité d'une session
     */
    @Modifying
    @Query("UPDATE UserSession us SET us.lastActivity = :lastActivity WHERE us.sessionToken = :sessionToken")
    void updateSessionActivity(@Param("sessionToken") String sessionToken, @Param("lastActivity") LocalDateTime lastActivity);

    /**
     * Termine toutes les sessions d'un utilisateur
     */
    @Modifying
    @Query("UPDATE UserSession us SET us.isActive = false, us.logoutAt = :logoutTime WHERE us.user = :user AND us.isActive = true")
    void terminateAllSessionsForUser(@Param("user") User user, @Param("logoutTime") LocalDateTime logoutTime);

    /**
     * Supprime les sessions anciennes
     */
    @Modifying
    @Query("DELETE FROM UserSession us WHERE us.createdAt < :cutoffDate AND us.isActive = false")
    void deleteOldSessions(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Statistiques des sessions par appareil
     */
    @Query("SELECT us.deviceType, COUNT(us) FROM UserSession us WHERE us.createdAt >= :since GROUP BY us.deviceType")
    List<Object[]> getSessionStatisticsByDevice(@Param("since") LocalDateTime since);
}
