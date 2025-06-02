// ===== UserSpecifications.java =====
package com.securetalk.repository.specification;

import com.securetalk.model.Role;
import com.securetalk.model.User;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Spécifications JPA pour les requêtes dynamiques sur User
 */
public class UserSpecifications {

    /**
     * Utilisateurs dont l'email contient le terme de recherche
     */
    public static Specification<User> emailContains(String email) {
        return (root, query, criteriaBuilder) -> {
            if (email == null || email.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("email")),
                    "%" + email.toLowerCase() + "%"
            );
        };
    }

    /**
     * Utilisateurs dont le nom d'utilisateur contient le terme de recherche
     */
    public static Specification<User> usernameContains(String username) {
        return (root, query, criteriaBuilder) -> {
            if (username == null || username.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("username")),
                    "%" + username.toLowerCase() + "%"
            );
        };
    }

    /**
     * Utilisateurs ayant un rôle spécifique
     */
    public static Specification<User> hasRole(Role role) {
        return (root, query, criteriaBuilder) -> {
            if (role == null) {
                return criteriaBuilder.conjunction();
            }
            Join<User, Role> rolesJoin = root.join("roles", JoinType.INNER);
            return criteriaBuilder.equal(rolesJoin, role);
        };
    }

    /**
     * Utilisateurs actifs ou inactifs
     */
    public static Specification<User> isEnabled(Boolean enabled) {
        return (root, query, criteriaBuilder) -> {
            if (enabled == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("enabled"), enabled);
        };
    }

    /**
     * Utilisateurs en ligne ou hors ligne
     */
    public static Specification<User> isOnline(Boolean online) {
        return (root, query, criteriaBuilder) -> {
            if (online == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("isOnline"), online);
        };
    }

    /**
     * Utilisateurs créés après une date
     */
    public static Specification<User> createdAfter(LocalDateTime date) {
        return (root, query, criteriaBuilder) -> {
            if (date == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), date);
        };
    }

    /**
     * Utilisateurs créés avant une date
     */
    public static Specification<User> createdBefore(LocalDateTime date) {
        return (root, query, criteriaBuilder) -> {
            if (date == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), date);
        };
    }

    /**
     * Utilisateurs connectés après une date
     */
    public static Specification<User> lastLoginAfter(LocalDateTime date) {
        return (root, query, criteriaBuilder) -> {
            if (date == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.greaterThanOrEqualTo(root.get("lastLogin"), date);
        };
    }

    /**
     * Recherche globale dans plusieurs champs
     */
    public static Specification<User> globalSearch(String searchTerm) {
        return (root, query, criteriaBuilder) -> {
            if (searchTerm == null || searchTerm.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }

            String likePattern = "%" + searchTerm.toLowerCase() + "%";

            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("username")), likePattern));
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), likePattern));
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("firstName")), likePattern));
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("lastName")), likePattern));

            return criteriaBuilder.or(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Utilisateurs disponibles pour la messagerie (actifs et non bloqués)
     */
    public static Specification<User> availableForMessaging(Long currentUserId) {
        return (root, query, criteriaBuilder) -> {
            if (currentUserId == null) {
                return criteriaBuilder.conjunction();
            }

            List<Predicate> predicates = new ArrayList<>();

            // Utilisateur actif
            predicates.add(criteriaBuilder.equal(root.get("enabled"), true));

            // Pas l'utilisateur actuel
            predicates.add(criteriaBuilder.notEqual(root.get("id"), currentUserId));

            // TODO: Ajouter la logique de blocage avec sous-requête

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
