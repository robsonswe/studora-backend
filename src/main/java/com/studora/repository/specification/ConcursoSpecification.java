package com.studora.repository.specification;

import com.studora.dto.concurso.ConcursoFilter;
import com.studora.entity.Concurso;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class ConcursoSpecification {

    public static Specification<Concurso> withFilter(ConcursoFilter filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Avoid duplicate results when joining collections (e.g., concursoCargos)
            query.distinct(true);

            if (filter.getBancaId() != null) {
                predicates.add(cb.equal(root.get("banca").get("id"), filter.getBancaId()));
            }

            if (filter.getInstituicaoId() != null) {
                predicates.add(cb.equal(root.get("instituicao").get("id"), filter.getInstituicaoId()));
            }

            if (filter.getCargoId() != null) {
                predicates.add(cb.equal(root.join("concursoCargos").get("cargo").get("id"), filter.getCargoId()));
            }

            if (filter.getInstituicaoArea() != null) {
                predicates.add(cb.equal(cb.lower(root.get("instituicao").get("area")), filter.getInstituicaoArea().toLowerCase()));
            }

            if (filter.getCargoArea() != null) {
                predicates.add(cb.equal(cb.lower(root.join("concursoCargos").get("cargo").get("area")), filter.getCargoArea().toLowerCase()));
            }

            if (filter.getCargoNivel() != null) {
                predicates.add(cb.equal(root.join("concursoCargos").get("cargo").get("nivel"), filter.getCargoNivel()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
