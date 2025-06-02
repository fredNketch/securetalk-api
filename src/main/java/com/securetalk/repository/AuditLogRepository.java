// ===== AuditLogRepository.java =====
package com.securetalk.repository;

import com.securetalk.model.AuditLog;
import com.securetalk.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository pour les logs d'audit
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    /**
     * Trouve les logs par utilisateur
     */
    Page<AuditLog> findByUserOrderByTimestampDesc(User user, Pageable pageable);

    /**
     * Trouve les logs par action
     */
    List<AuditLog> findByActionAndTimestampAfterOrderByTimestampDesc(String action, LocalDateTime since);

    /**
     * Trouve les logs par niveau
     */
    Page<AuditLog> findByLevelOrderByTimestampDesc(AuditLog.AuditLevel level, Pageable pageable);

    /**
     * Trouve les logs par entité
     */
    List<AuditLog> findByEntityTypeAndEntityIdOrderByTimestampDesc(String entityType, Long entityId);

    /**
     * Trouve les logs d'une période
     */
    Page<AuditLog> findByTimestampBetweenOrderByTimestampDesc(LocalDateTime start, LocalDateTime end, Pageable pageable);

    /**
     * Trouve les logs par IP
     */
    List<AuditLog> findByIpAddressAndTimestampAfterOrderByTimestampDesc(String ipAddress, LocalDateTime since);

    /**
     * Statistiques d'audit par action
     */
    @Query("SELECT a.action, COUNT(a) FROM AuditLog a WHERE a.timestamp >= :since GROUP BY a.action ORDER BY COUNT(a) DESC")
    List<Object[]> getAuditStatisticsByAction(@Param("since") LocalDateTime since);

    /**
     * Statistiques d'audit par utilisateur
     */
    @Query("SELECT a.user, COUNT(a) FROM AuditLog a WHERE a.timestamp >= :since AND a.user IS NOT NULL GROUP BY a.user ORDER BY COUNT(a) DESC")
    List<Object[]> getAuditStatisticsByUser(@Param("since") LocalDateTime since, Pageable pageable);

    /**
     * Recherche dans les logs
     */
    @Query("SELECT a FROM AuditLog a WHERE " +
            "(LOWER(a.action) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            " LOWER(a.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
            "ORDER BY a.timestamp DESC")
    Page<AuditLog> searchAuditLogs(@Param("searchTerm") String searchTerm, Pageable pageable);
}
