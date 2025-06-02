// ===== SystemConfigRepository.java =====
package com.securetalk.repository;

import com.securetalk.model.SystemConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository pour la configuration système
 */
@Repository
public interface SystemConfigRepository extends JpaRepository<SystemConfig, Long> {

    /**
     * Trouve une configuration par clé
     */
    Optional<SystemConfig> findByConfigKey(String configKey);

    /**
     * Trouve toutes les configurations d'une catégorie
     */
    List<SystemConfig> findByCategoryOrderByConfigKey(String category);

    /**
     * Trouve toutes les configurations requises
     */
    List<SystemConfig> findByIsRequiredTrueOrderByConfigKey();

    /**
     * Trouve toutes les configurations chiffrées
     */
    List<SystemConfig> findByIsEncryptedTrueOrderByConfigKey();

    /**
     * Recherche de configurations
     */
    @Query("SELECT sc FROM SystemConfig sc WHERE " +
            "LOWER(sc.configKey) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(sc.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<SystemConfig> searchConfigs(@Param("searchTerm") String searchTerm);

    /**
     * Vérifie si une clé de configuration existe
     */
    boolean existsByConfigKey(String configKey);

    /**
     * Compte les configurations par catégorie
     */
    @Query("SELECT sc.category, COUNT(sc) FROM SystemConfig sc GROUP BY sc.category")
    List<Object[]> countConfigsByCategory();
}
