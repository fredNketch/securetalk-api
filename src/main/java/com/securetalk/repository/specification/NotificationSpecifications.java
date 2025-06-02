// ===== NotificationSpecifications.java =====
package com.securetalk.repository.specification;

import com.securetalk.model.Notification;
import com.securetalk.model.User;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Spécifications JPA pour les requêtes dynamiques sur Notification
 */
public class NotificationSpecifications {

    /**
     * Notifications d'un utilisateur spécifique
     */
    public static Specification<Notification> forUser(Long userId) {
        return (root, query, criteriaBuilder) -> {
            if (userId == null) {
                return criteriaBuilder.conjunction();
            }
            Join<Notification, User> userJoin = root.join("user", JoinType.LEFT);
            return criteriaBuilder.equal(userJoin.get("id"), userId);
        };
    }

    /**
     * Notifications lues ou non lues
     */
    public static Specification<Notification> isRead(Boolean read) {
        return (root, query, criteriaBuilder) -> {
            if (read == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("isRead"), read);
        };
    }

    /**
     * Notifications d'un type spécifique
     */
    public static Specification<Notification> ofType(Notification.NotificationType type) {
        return (root, query, criteriaBuilder) -> {
            if (type == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("notificationType"), type);
        };
    }

    /**
     * Notifications avec une priorité spécifique
     */
    public static Specification<Notification> withPriority(Notification.NotificationPriority priority) {
        return (root, query, criteriaBuilder) -> {
            if (priority == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("priority"), priority);
        };
    }

    /**
     * Notifications non expirées
     */
    public static Specification<Notification> notExpired() {
        return (root, query, criteriaBuilder) -> {
            LocalDateTime now = LocalDateTime.now();
            Predicate nullExpiration = criteriaBuilder.isNull(root.get("expiresAt"));
            Predicate notExpired = criteriaBuilder.greaterThan(root.get("expiresAt"), now);
            return criteriaBuilder.or(nullExpiration, notExpired);
        };
    }

    /**
     * Notifications dans une plage de dates
     */
    public static Specification<Notification> inDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (startDate != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), startDate));
            }

            if (endDate != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), endDate));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Recherche textuelle dans titre et message
     */
    public static Specification<Notification> textSearch(String searchTerm) {
        return (root, query, criteriaBuilder) -> {
            if (searchTerm == null || searchTerm.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }

            String likePattern = "%" + searchTerm.toLowerCase() + "%";

            Predicate titlePredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("title")), likePattern);
            Predicate messagePredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("message")), likePattern);

            return criteriaBuilder.or(titlePredicate, messagePredicate);
        };
    }

    /**
     * Notifications actives pour un utilisateur (non lues et non expirées)
     */
    public static Specification<Notification> activeForUser(Long userId) {
        return Specification.where(forUser(userId))
                .and(isRead(false))
                .and(notExpired());
    }
}
