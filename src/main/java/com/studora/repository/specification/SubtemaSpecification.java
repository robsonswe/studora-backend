package com.studora.repository.specification;

import com.studora.entity.Subtema;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class SubtemaSpecification {

    public static Specification<Subtema> withFilters(String nome, List<Long> temaIds, List<Long> disciplinaIds) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Fetch join for performance
            if (Long.class != query.getResultType()) {
                var temaJoin = root.fetch("tema", JoinType.INNER);
                temaJoin.fetch("disciplina", JoinType.INNER);
            }

            if (nome != null && !nome.isBlank()) {
                String normalized = com.studora.util.StringUtils.normalizeForSearch(nome);
                predicates.add(cb.like(root.get("nomeNormalized"), "%" + normalized + "%"));
            }

            List<Predicate> idPredicates = new ArrayList<>();
            if (temaIds != null && !temaIds.isEmpty()) {
                idPredicates.add(root.get("tema").get("id").in(temaIds));
            }
            if (disciplinaIds != null && !disciplinaIds.isEmpty()) {
                idPredicates.add(root.get("tema").get("disciplina").get("id").in(disciplinaIds));
            }

            if (!idPredicates.isEmpty()) {
                // If there's exactly one disciplina and one or more temas, intersect them (AND)
                // Otherwise keep them independent (OR) to allow broad filtering
                if (idPredicates.size() == 2 && disciplinaIds != null && disciplinaIds.size() == 1) {
                    predicates.add(cb.and(idPredicates.toArray(new Predicate[0])));
                } else {
                    predicates.add(cb.or(idPredicates.toArray(new Predicate[0])));
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
