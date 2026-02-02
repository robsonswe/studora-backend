package com.studora.repository.specification;

import com.studora.dto.QuestaoFilter;
import com.studora.entity.Questao;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class QuestaoSpecification {

    public static Specification<Questao> withFilter(QuestaoFilter filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Avoid duplicate results when joining collections
            query.distinct(true);

            // Hierarchy branch
            if (filter.getConcursoId() != null) {
                predicates.add(cb.equal(root.get("concurso").get("id"), filter.getConcursoId()));
            }

            if (filter.getBancaId() != null) {
                predicates.add(cb.equal(root.get("concurso").get("banca").get("id"), filter.getBancaId()));
            }

            if (filter.getInstituicaoId() != null) {
                predicates.add(cb.equal(root.get("concurso").get("instituicao").get("id"), filter.getInstituicaoId()));
            }

            if (filter.getCargoId() != null) {
                predicates.add(cb.equal(root.join("questaoCargos").get("concursoCargo").get("cargo").get("id"), filter.getCargoId()));
            }

            // Taxonomy branch
            if (filter.getSubtemaId() != null) {
                predicates.add(cb.equal(root.join("subtemas").get("id"), filter.getSubtemaId()));
            }

            if (filter.getTemaId() != null) {
                predicates.add(cb.equal(root.join("subtemas").get("tema").get("id"), filter.getTemaId()));
            }

            if (filter.getDisciplinaId() != null) {
                predicates.add(cb.equal(root.join("subtemas").get("tema").get("disciplina").get("id"), filter.getDisciplinaId()));
            }

            // Flags
            if (filter.getAnulada() != null) {
                predicates.add(cb.equal(root.get("anulada"), filter.getAnulada()));
            }

            if (filter.getDesatualizada() != null) {
                predicates.add(cb.equal(root.get("desatualizada"), filter.getDesatualizada()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
