package com.studora.repository.specification;

import com.studora.entity.Tema;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class TemaSpecification {

    public static Specification<Tema> withFilters(String nome, List<Long> disciplinaIds) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Fetch join for performance
            if (Long.class != query.getResultType()) {
                root.fetch("disciplina", JoinType.INNER);
            }

            if (nome != null && !nome.isBlank()) {
                predicates.add(cb.like(cb.upper(root.get("nome")), "%" + nome.toUpperCase() + "%"));
            }

            if (disciplinaIds != null && !disciplinaIds.isEmpty()) {
                predicates.add(root.get("disciplina").get("id").in(disciplinaIds));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
