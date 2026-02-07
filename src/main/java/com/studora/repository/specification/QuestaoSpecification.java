package com.studora.repository.specification;

import com.studora.dto.questao.QuestaoFilter;
import com.studora.entity.Questao;
import com.studora.entity.Resposta;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class QuestaoSpecification {

    public static Specification<Questao> notAnsweredRecently(java.time.LocalDateTime threshold) {
        return (root, query, cb) -> {
            Subquery<Long> subquery = query.subquery(Long.class);
            Root<Resposta> respostaRoot = subquery.from(Resposta.class);
            subquery.select(respostaRoot.get("questao").get("id"));
            
            // Format threshold as ISO string for string comparison in SQLite
            String thresholdStr = threshold.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            
            // Use cast to String for the column and compare with the literal string
            subquery.where(cb.greaterThanOrEqualTo(respostaRoot.get("createdAt").as(String.class), thresholdStr));
            
            return cb.not(root.get("id").in(subquery));
        };
    }

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
