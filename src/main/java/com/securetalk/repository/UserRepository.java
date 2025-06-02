// ===== UserRepository.java =====
package com.securetalk.repository;

import com.securetalk.model.Role;
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
 * Repository pour la gestion des utilisateurs
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    // ===== RECHERCHES BASIQUES =====

    /**
     * Trouve un utilisateur par email (utilisé pour l'authentification)
     */
    Optional<User> findByEmail(String email);

    /**
     * Trouve un utilisateur par nom d'utilisateur
     */
    Optional<User> findByUsername(String username);

    /**
     * Trouve un utilisateur par email ou nom d'utilisateur
     */
    @Query("SELECT u FROM User u WHERE u.email = :identifier OR u.username = :identifier")
    Optional<User> findByEmailOrUsername(@Param("identifier") String identifier);

    // ===== VÉRIFICATIONS D'EXISTENCE =====

    /**
     * Vérifie si un email existe déjà
     */
    boolean existsByEmail(String email);

    /**
     * Vérifie si un nom d'utilisateur existe déjà
     */
    boolean existsByUsername(String username);

    /**
     * Vérifie si un email existe pour un autre utilisateur
     */
    boolean existsByEmailAndIdNot(String email, Long id);

    /**
     * Vérifie si un nom d'utilisateur existe pour un autre utilisateur
     */
    boolean existsByUsernameAndIdNot(String username, Long id);

    // ===== RECHERCHES PAR STATUT =====

    /**
     * Trouve tous les utilisateurs actifs
     */
    List<User> findByEnabledTrue();

    /**
     * Trouve tous les utilisateurs actifs avec pagination
     */
    Page<User> findByEnabledTrue(Pageable pageable);

    /**
     * Trouve tous les utilisateurs désactivés
     */
    List<User> findByEnabledFalse();

    /**
     * Trouve tous les utilisateurs en ligne
     */
    @Query("SELECT u FROM User u WHERE u.isOnline = true AND u.enabled = true")
    List<User> findOnlineUsers();

    /**
     * Trouve tous les utilisateurs en ligne sauf un utilisateur spécifique
     */
    @Query("SELECT u FROM User u WHERE u.isOnline = true AND u.enabled = true AND u.id != :excludeUserId")
    List<User> findOnlineUsersExcept(@Param("excludeUserId") Long excludeUserId);

    // ===== RECHERCHES PAR RÔLE =====

    /**
     * Trouve tous les utilisateurs ayant un rôle spécifique
     */
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r = :role")
    List<User> findByRole(@Param("role") Role role);

    /**
     * Compte le nombre d'utilisateurs ayant un rôle spécifique
     */
    @Query("SELECT COUNT(u) FROM User u JOIN u.roles r WHERE r = :role")
    long countByRole(@Param("role") Role role);

    /**
     * Trouve tous les administrateurs
     */
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r = 'ADMIN'")
    List<User> findAllAdmins();

    /**
     * Compte le nombre d'administrateurs
     */
    @Query("SELECT COUNT(u) FROM User u JOIN u.roles r WHERE r = 'ADMIN'")
    long countAdmins();

    // ===== RECHERCHES TEMPORELLES =====

    /**
     * Trouve les utilisateurs créés après une date
     */
    List<User> findByCreatedAtAfter(LocalDateTime date);

    /**
     * Trouve les utilisateurs connectés après une date
     */
    List<User> findByLastLoginAfter(LocalDateTime date);

    /**
     * Trouve les utilisateurs actifs récemment (dernière activité)
     */
    @Query("SELECT u FROM User u WHERE u.lastSeen >= :since AND u.enabled = true")
    List<User> findRecentlyActiveUsers(@Param("since") LocalDateTime since);

    /**
     * Trouve les utilisateurs inactifs depuis une date
     */
    @Query("SELECT u FROM User u WHERE u.lastSeen < :since OR u.lastSeen IS NULL")
    List<User> findInactiveUsersSince(@Param("since") LocalDateTime since);

    // ===== RECHERCHES TEXTUELLES =====

    /**
     * Recherche d'utilisateurs par nom d'utilisateur ou email (LIKE)
     */
    @Query("SELECT u FROM User u WHERE " +
            "LOWER(u.username) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<User> searchUsers(@Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Recherche d'utilisateurs actifs pour la messagerie
     */
    @Query("SELECT u FROM User u WHERE u.enabled = true AND u.id != :currentUserId AND (" +
            "LOWER(u.username) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<User> searchActiveUsersForMessaging(@Param("currentUserId") Long currentUserId,
                                             @Param("searchTerm") String searchTerm);

    // ===== STATISTIQUES =====

    /**
     * Compte le total des utilisateurs
     */
    long count();

    /**
     * Compte les utilisateurs actifs
     */
    long countByEnabledTrue();

    /**
     * Compte les utilisateurs en ligne
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.isOnline = true AND u.enabled = true")
    long countOnlineUsers();

    /**
     * Statistiques d'inscription par période
     */
    @Query("SELECT DATE(u.createdAt) as date, COUNT(u) as count " +
            "FROM User u WHERE u.createdAt >= :startDate " +
            "GROUP BY DATE(u.createdAt) ORDER BY date")
    List<Object[]> getRegistrationStatistics(@Param("startDate") LocalDateTime startDate);

    /**
     * Top utilisateurs par nombre de messages envoyés
     */
    @Query("SELECT u, COUNT(m) as messageCount FROM User u " +
            "LEFT JOIN u.sentMessages m " +
            "WHERE u.enabled = true " +
            "GROUP BY u " +
            "ORDER BY messageCount DESC")
    List<Object[]> getTopUsersByMessagesCount(Pageable pageable);

    // ===== MISE À JOUR EN MASSE =====

    /**
     * Met à jour le statut en ligne d'un utilisateur
     */
    @Modifying
    @Query("UPDATE User u SET u.isOnline = :isOnline, u.lastSeen = :lastSeen WHERE u.id = :userId")
    void updateOnlineStatus(@Param("userId") Long userId,
                            @Param("isOnline") Boolean isOnline,
                            @Param("lastSeen") LocalDateTime lastSeen);

    /**
     * Met à jour la dernière connexion
     */
    @Modifying
    @Query("UPDATE User u SET u.lastLogin = :loginTime, u.lastSeen = :loginTime, " +
            "u.isOnline = true, u.failedLoginAttempts = 0, u.lockedUntil = null " +
            "WHERE u.id = :userId")
    void updateLastLogin(@Param("userId") Long userId, @Param("loginTime") LocalDateTime loginTime);

    /**
     * Incrémente les tentatives de connexion échouées
     */
    @Modifying
    @Query("UPDATE User u SET u.failedLoginAttempts = u.failedLoginAttempts + 1 WHERE u.email = :email")
    void incrementFailedLoginAttempts(@Param("email") String email);

    /**
     * Verrouille un compte
     */
    @Modifying
    @Query("UPDATE User u SET u.lockedUntil = :lockedUntil, u.accountNonLocked = false WHERE u.id = :userId")
    void lockAccount(@Param("userId") Long userId, @Param("lockedUntil") LocalDateTime lockedUntil);

    /**
     * Déverrouille un compte
     */
    @Modifying
    @Query("UPDATE User u SET u.lockedUntil = null, u.accountNonLocked = true, " +
            "u.failedLoginAttempts = 0 WHERE u.id = :userId")
    void unlockAccount(@Param("userId") Long userId);

    /**
     * Met tous les utilisateurs hors ligne (utile au redémarrage du serveur)
     */
    @Modifying
    @Query("UPDATE User u SET u.isOnline = false WHERE u.isOnline = true")
    void setAllUsersOffline();

    /**
     * Met à jour le mot de passe et la date de changement
     */
    @Modifying
    @Query("UPDATE User u SET u.password = :newPassword, u.passwordChangedAt = :changedAt " +
            "WHERE u.id = :userId")
    void updatePassword(@Param("userId") Long userId,
                        @Param("newPassword") String newPassword,
                        @Param("changedAt") LocalDateTime changedAt);

    // ===== REQUÊTES POUR LA MESSAGERIE =====

    /**
     * Trouve les utilisateurs avec qui un utilisateur a échangé des messages
     */
    @Query("SELECT DISTINCT CASE " +
            "WHEN m.sender.id = :userId THEN m.recipient " +
            "ELSE m.sender END " +
            "FROM Message m " +
            "WHERE m.sender.id = :userId OR m.recipient.id = :userId")
    List<User> findConversationParticipants(@Param("userId") Long userId);

    /**
     * Trouve les utilisateurs disponibles pour la messagerie (excluant les bloqués)
     */
    @Query("SELECT u FROM User u WHERE u.enabled = true AND u.id != :currentUserId " +
            "AND u.id NOT IN (" +
            "    SELECT b.blocked.id FROM BlockedUser b " +
            "    WHERE b.blocker.id = :currentUserId AND b.isActive = true" +
            ") " +
            "AND :currentUserId NOT IN (" +
            "    SELECT b2.blocker.id FROM BlockedUser b2 " +
            "    WHERE b2.blocked.id = u.id AND b2.isActive = true" +
            ")")
    List<User> findAvailableUsersForMessaging(@Param("currentUserId") Long currentUserId);

    // ===== REQUÊTES DE SÉCURITÉ =====

    /**
     * Trouve les comptes verrouillés
     */
    @Query("SELECT u FROM User u WHERE u.lockedUntil IS NOT NULL AND u.lockedUntil > :now")
    List<User> findLockedAccounts(@Param("now") LocalDateTime now);

    /**
     * Trouve les comptes avec trop de tentatives de connexion échouées
     */
    @Query("SELECT u FROM User u WHERE u.failedLoginAttempts >= :maxAttempts")
    List<User> findAccountsWithTooManyFailedAttempts(@Param("maxAttempts") Integer maxAttempts);

    /**
     * Trouve les utilisateurs qui n'ont jamais changé leur mot de passe
     */
    @Query("SELECT u FROM User u WHERE u.passwordChangedAt IS NULL")
    List<User> findUsersWithDefaultPassword();

    /**
     * Trouve les utilisateurs dont le mot de passe est ancien
     */
    @Query("SELECT u FROM User u WHERE u.passwordChangedAt < :date OR u.passwordChangedAt IS NULL")
    List<User> findUsersWithOldPassword(@Param("date") LocalDateTime date);
}