// ===== AuditLogSpecifications.java =====
package com.securetalk.repository.specification;

import com.securetalk.model.AuditLog;
import com.securetalk.model.User;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Spécifications JPA pour les requêtes dynamiques sur AuditLog
 */
public class AuditLogSpecifications {

    /**
     * Logs d'un utilisateur spécifique
     */
    public static Specification<AuditLog> forUser(Long userId) {
        return (root, query, criteriaBuilder) -> {
            if (userId == null) {
                return criteriaBuilder.conjunction();
            }
            Join<AuditLog, User> userJoin = root.join("user", JoinType.LEFT);
            return criteriaBuilder.equal(userJoin.get("id"), userId);
        };
    }

    /**
     * Logs d'une action spécifique
     */
    public static Specification<AuditLog> withAction(String action) {
        return (root, query, criteriaBuilder) -> {
            if (action == null || action.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("action"), action);
        };
    }

    /**
     * Logs d'un niveau spécifique
     */
    public static Specification<AuditLog> withLevel(AuditLog.AuditLevel level) {
        return (root, query, criteriaBuilder) -> {
            if (level == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("level"), level);
        };
    }

    /**
     * Logs concernant une entité spécifique
     */
    public static Specification<AuditLog> forEntity(String entityType, Long entityId) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (entityType != null && !entityType.trim().isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("entityType"), entityType));
            }

            if (entityId != null) {
                predicates.add(criteriaBuilder.equal(root.get("entityId"), entityId));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Logs dans une plage de dates
     */
    public static Specification<AuditLog> inDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (startDate != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("timestamp"), startDate));
            }

            if (endDate != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("timestamp"), endDate));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Logs par adresse IP
     */
    public static Specification<AuditLog> fromIpAddress(String ipAddress) {
        return (root, query, criteriaBuilder) -> {
            if (ipAddress == null || ipAddress.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("ipAddress"), ipAddress);
        };
    }

    /**
     * Recherche textuelle dans description
     */
    public static Specification<AuditLog> descriptionContains(String searchTerm) {
        return (root, query, criteriaBuilder) -> {
            if (searchTerm == null || searchTerm.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("description")),
                    "%" + searchTerm.toLowerCase() + "%"
            );
        };
    }

    /**
     * Recherche complexe dans les logs
     */
    public static Specification<AuditLog> complexSearch(Long userId, String action,
                                                        AuditLog.AuditLevel level,
                                                        LocalDateTime startDate,
                                                        LocalDateTime endDate,
                                                        String searchTerm) {
        return Specification.where(forUser(userId))
                .and(withAction(action))
                .and(withLevel(level))
                .and(inDateRange(startDate, endDate))
                .and(descriptionContains(searchTerm));
    }
}