// ===== MessageSpecifications.java =====
package com.securetalk.repository.specification;

import com.securetalk.model.Message;
import com.securetalk.model.User;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Spécifications JPA pour les requêtes dynamiques sur Message
 */
public class MessageSpecifications {

    /**
     * Messages d'un utilisateur spécifique (envoyés ou reçus)
     */
    public static Specification<Message> forUser(Long userId) {
        return (root, query, criteriaBuilder) -> {
            if (userId == null) {
                return criteriaBuilder.conjunction();
            }

            Join<Message, User> senderJoin = root.join("sender", JoinType.LEFT);
            Join<Message, User> recipientJoin = root.join("recipient", JoinType.LEFT);

            Predicate senderPredicate = criteriaBuilder.equal(senderJoin.get("id"), userId);
            Predicate recipientPredicate = criteriaBuilder.equal(recipientJoin.get("id"), userId);

            return criteriaBuilder.or(senderPredicate, recipientPredicate);
        };
    }

    /**
     * Messages entre deux utilisateurs
     */
    public static Specification<Message> betweenUsers(Long userId1, Long userId2) {
        return (root, query, criteriaBuilder) -> {
            if (userId1 == null || userId2 == null) {
                return criteriaBuilder.conjunction();
            }

            Join<Message, User> senderJoin = root.join("sender", JoinType.LEFT);
            Join<Message, User> recipientJoin = root.join("recipient", JoinType.LEFT);

            Predicate conversation1 = criteriaBuilder.and(
                    criteriaBuilder.equal(senderJoin.get("id"), userId1),
                    criteriaBuilder.equal(recipientJoin.get("id"), userId2)
            );

            Predicate conversation2 = criteriaBuilder.and(
                    criteriaBuilder.equal(senderJoin.get("id"), userId2),
                    criteriaBuilder.equal(recipientJoin.get("id"), userId1)
            );

            return criteriaBuilder.or(conversation1, conversation2);
        };
    }

    /**
     * Messages non supprimés
     */
    public static Specification<Message> notDeleted() {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("isDeleted"), false);
    }

    /**
     * Messages non lus
     */
    public static Specification<Message> unread() {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("isRead"), false);
    }

    /**
     * Messages d'un type spécifique
     */
    public static Specification<Message> ofType(String messageType) {
        return (root, query, criteriaBuilder) -> {
            if (messageType == null || messageType.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("messageType"), messageType);
        };
    }

    /**
     * Messages après une date
     */
    public static Specification<Message> after(LocalDateTime date) {
        return (root, query, criteriaBuilder) -> {
            if (date == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.greaterThanOrEqualTo(root.get("timestamp"), date);
        };
    }

    /**
     * Messages avant une date
     */
    public static Specification<Message> before(LocalDateTime date) {
        return (root, query, criteriaBuilder) -> {
            if (date == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.lessThanOrEqualTo(root.get("timestamp"), date);
        };
    }

    /**
     * Messages dans une plage de dates
     */
    public static Specification<Message> inDateRange(LocalDateTime startDate, LocalDateTime endDate) {
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
     * Messages avec une priorité spécifique
     */
    public static Specification<Message> withPriority(Message.MessagePriority priority) {
        return (root, query, criteriaBuilder) -> {
            if (priority == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("priority"), priority);
        };
    }

    /**
     * Messages envoyés par un utilisateur spécifique
     */
    public static Specification<Message> sentBy(Long senderId) {
        return (root, query, criteriaBuilder) -> {
            if (senderId == null) {
                return criteriaBuilder.conjunction();
            }
            Join<Message, User> senderJoin = root.join("sender", JoinType.LEFT);
            return criteriaBuilder.equal(senderJoin.get("id"), senderId);
        };
    }

    /**
     * Messages reçus par un utilisateur spécifique
     */
    public static Specification<Message> receivedBy(Long recipientId) {
        return (root, query, criteriaBuilder) -> {
            if (recipientId == null) {
                return criteriaBuilder.conjunction();
            }
            Join<Message, User> recipientJoin = root.join("recipient", JoinType.LEFT);
            return criteriaBuilder.equal(recipientJoin.get("id"), recipientId);
        };
    }

    /**
     * Recherche complexe de messages pour un utilisateur
     */
    public static Specification<Message> searchForUser(Long userId, String messageType,
                                                       LocalDateTime startDate, LocalDateTime endDate,
                                                       Boolean unreadOnly) {
        return Specification.where(forUser(userId))
                .and(notDeleted())
                .and(ofType(messageType))
                .and(inDateRange(startDate, endDate))
                .and(unreadOnly != null && unreadOnly ? unread() : null);
    }
}
