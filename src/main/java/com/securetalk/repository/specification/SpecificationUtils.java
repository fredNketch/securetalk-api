// ===== SpecificationUtils.java =====
package com.securetalk.repository.specification;

import org.springframework.data.jpa.domain.Specification;

/**
 * Utilitaires pour les spécifications JPA
 */
public class SpecificationUtils {

    /**
     * Combine des spécifications avec ET logique, en ignorant les nulls
     */
    @SafeVarargs
    public static <T> Specification<T> and(Specification<T>... specifications) {
        Specification<T> result = null;

        for (Specification<T> spec : specifications) {
            if (spec != null) {
                result = result == null ? spec : result.and(spec);
            }
        }

        return result != null ? result : Specification.where(null);
    }

    /**
     * Combine des spécifications avec OU logique, en ignorant les nulls
     */
    @SafeVarargs
    public static <T> Specification<T> or(Specification<T>... specifications) {
        Specification<T> result = null;

        for (Specification<T> spec : specifications) {
            if (spec != null) {
                result = result == null ? spec : result.or(spec);
            }
        }

        return result != null ? result : Specification.where(null);
    }

    /**
     * Crée une spécification qui ne filtre rien (retourne tous les éléments)
     */
    public static <T> Specification<T> noFilter() {
        return (root, query, criteriaBuilder) -> criteriaBuilder.conjunction();
    }

    /**
     * Crée une spécification qui ne retourne rien
     */
    public static <T> Specification<T> none() {
        return (root, query, criteriaBuilder) -> criteriaBuilder.disjunction();
    }
}