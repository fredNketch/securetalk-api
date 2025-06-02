// ===== RefreshTokenRepository.java =====
package com.securetalk.repository;

import com.securetalk.model.RefreshToken;
import com.securetalk.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository pour la gestion des refresh tokens
 */
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    /**
     * Trouve un refresh token par sa valeur
     */
    Optional<RefreshToken> findByToken(String token);

    /**
     * Trouve tous les tokens d'un utilisateur
     */
    List<RefreshToken> findByUser(User user);

    /**
     * Trouve tous les tokens valides d'un utilisateur
     */
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.user = :user AND rt.revoked = false AND rt.expiresAt > :now")
    List<RefreshToken> findValidTokensByUser(@Param("user") User user, @Param("now") LocalDateTime now);

    /**
     * Trouve les tokens expirés
     */
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.expiresAt <= :now")
    List<RefreshToken> findExpiredTokens(@Param("now") LocalDateTime now);

    /**
     * Révoque tous les tokens d'un utilisateur
     */
    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.revoked = true, rt.revokedAt = :revokedAt WHERE rt.user = :user")
    void revokeAllTokensForUser(@Param("user") User user, @Param("revokedAt") LocalDateTime revokedAt);

    /**
     * Révoque un token spécifique
     */
    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.revoked = true, rt.revokedAt = :revokedAt WHERE rt.token = :token")
    void revokeToken(@Param("token") String token, @Param("revokedAt") LocalDateTime revokedAt);

    /**
     * Supprime les tokens expirés et révoqués
     */
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE (rt.expiresAt <= :now OR rt.revoked = true) AND rt.createdAt < :cutoffDate")
    void deleteExpiredAndRevokedTokens(@Param("now") LocalDateTime now, @Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Compte les tokens actifs par utilisateur
     */
    @Query("SELECT COUNT(rt) FROM RefreshToken rt WHERE rt.user = :user AND rt.revoked = false AND rt.expiresAt > :now")
    long countActiveTokensByUser(@Param("user") User user, @Param("now") LocalDateTime now);

    /**
     * Trouve les tokens par adresse IP (sécurité)
     */
    List<RefreshToken> findByIpAddressAndCreatedAtAfter(String ipAddress, LocalDateTime since);
}