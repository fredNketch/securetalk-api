// ===== BlockedUserRepository.java =====
package com.securetalk.repository;

import com.securetalk.model.BlockedUser;
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
 * Repository pour la gestion des utilisateurs bloqués
 */
@Repository
public interface BlockedUserRepository extends JpaRepository<BlockedUser, Long> {

    /**
     * Trouve un blocage actif entre deux utilisateurs
     */
    @Query("SELECT bu FROM BlockedUser bu WHERE bu.blocker = :blocker AND bu.blocked = :blocked AND bu.isActive = true")
    Optional<BlockedUser> findActiveBlock(@Param("blocker") User blocker, @Param("blocked") User blocked);

    /**
     * Vérifie si un utilisateur en bloque un autre
     */
    @Query("SELECT COUNT(bu) > 0 FROM BlockedUser bu WHERE bu.blocker = :blocker AND bu.blocked = :blocked AND bu.isActive = true")
    boolean isUserBlocked(@Param("blocker") User blocker, @Param("blocked") User blocked);

    /**
     * Vérifie si deux utilisateurs se bloquent mutuellement
     */
    @Query("SELECT COUNT(bu) FROM BlockedUser bu WHERE " +
            "((bu.blocker = :user1 AND bu.blocked = :user2) OR " +
            " (bu.blocker = :user2 AND bu.blocked = :user1)) " +
            "AND bu.isActive = true")
    long countMutualBlocks(@Param("user1") User user1, @Param("user2") User user2);

    /**
     * Trouve tous les utilisateurs bloqués par un utilisateur
     */
    @Query("SELECT bu.blocked FROM BlockedUser bu WHERE bu.blocker = :blocker AND bu.isActive = true")
    List<User> findBlockedUsersByBlocker(@Param("blocker") User blocker);

    /**
     * Trouve tous les utilisateurs qui bloquent un utilisateur donné
     */
    @Query("SELECT bu.blocker FROM BlockedUser bu WHERE bu.blocked = :blocked AND bu.isActive = true")
    List<User> findBlockersByBlockedUser(@Param("blocked") User blocked);

    /**
     * Trouve tous les blocages actifs d'un utilisateur
     */
    @Query("SELECT bu FROM BlockedUser bu WHERE bu.blocker = :user AND bu.isActive = true ORDER BY bu.createdAt DESC")
    List<BlockedUser> findActiveBlocksByUser(@Param("user") User user);

    /**
     * Débloque un utilisateur
     */
    @Modifying
    @Query("UPDATE BlockedUser bu SET bu.isActive = false, bu.unblockedAt = :unblockedAt " +
            "WHERE bu.blocker = :blocker AND bu.blocked = :blocked AND bu.isActive = true")
    void unblockUser(@Param("blocker") User blocker, @Param("blocked") User blocked, @Param("unblockedAt") LocalDateTime unblockedAt);

    /**
     * Statistiques de blocage
     */
    @Query("SELECT COUNT(bu) FROM BlockedUser bu WHERE bu.isActive = true")
    long countActiveBlocks();

    /**
     * Utilisateurs les plus bloqués
     */
    @Query("SELECT bu.blocked, COUNT(bu) as blockCount FROM BlockedUser bu WHERE bu.isActive = true " +
            "GROUP BY bu.blocked ORDER BY blockCount DESC")
    List<Object[]> getMostBlockedUsers();
}
