package com.ticketmanagement.ticket;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

final class TicketSpecifications {

    private TicketSpecifications() {
    }

    static Specification<Ticket> withFilters(String likePattern, TicketStatus status) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }

            if (likePattern != null) {
                String lowerPattern = likePattern.toLowerCase();
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(
                                criteriaBuilder.lower(root.get("title")), lowerPattern, '\\'),
                        criteriaBuilder.like(
                                criteriaBuilder.lower(root.get("description")), lowerPattern, '\\')));
            }

            query.orderBy(
                    criteriaBuilder.desc(root.get("createdAt")),
                    criteriaBuilder.desc(root.get("ticketNumber")));

            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }

    static String escapeLike(String value) {
        return value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }

    static String toLikePattern(String q) {
        if (q == null) {
            return null;
        }
        String trimmed = q.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        return "%" + escapeLike(trimmed) + "%";
    }
}
