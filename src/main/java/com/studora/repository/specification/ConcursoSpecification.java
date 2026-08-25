package com.studora.repository.specification;

import com.studora.dto.concurso.ConcursoFilter;
import com.studora.entity.Concurso;
import com.studora.entity.ConcursoCargo;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

public class ConcursoSpecification {

    public static Specification<Concurso> withFilter(ConcursoFilter filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Collection filters use EXISTS subqueries: joining concurso_cargo would
            // multiply rows and force DISTINCT, which PostgreSQL rejects with
            // ORDER BY on joined columns.

            if (filter.getBancaId() != null) {
                predicates.add(cb.equal(root.get("banca").get("id"), filter.getBancaId()));
            }

            if (filter.getInstituicaoId() != null) {
                predicates.add(cb.equal(root.get("instituicao").get("id"), filter.getInstituicaoId()));
            }

            if (filter.getCargoId() != null) {
                predicates.add(existsCargoPredicate(root, query, cb,
                        (cc, c) -> c.equal(cc.get("cargo").get("id"), filter.getCargoId())));
            }

            if (filter.getInstituicaoArea() != null) {
                predicates.add(cb.equal(cb.lower(root.get("instituicao").get("area")), filter.getInstituicaoArea().toLowerCase()));
            }

            if (filter.getCargoArea() != null) {
                predicates.add(existsCargoPredicate(root, query, cb,
                        (cc, c) -> c.equal(c.lower(cc.get("cargo").get("area")), filter.getCargoArea().toLowerCase())));
            }

            if (filter.getCargoNivel() != null) {
                predicates.add(existsCargoPredicate(root, query, cb,
                        (cc, c) -> c.equal(cc.get("cargo").get("nivel"), filter.getCargoNivel())));
            }

            if (filter.getInscrito() != null) {
                if (filter.getInscrito()) {
                    predicates.add(existsCargoPredicate(root, query, cb,
                            (cc, c) -> c.isTrue(cc.get("inscrito"))));
                } else {
                    // Subquery to find concursos that have at least one inscribed cargo
                    Subquery<Long> subquery = query.subquery(Long.class);
                    Root<ConcursoCargo> subRoot = subquery.from(ConcursoCargo.class);
                    subquery.select(subRoot.get("concurso").get("id"));
                    subquery.where(cb.isTrue(subRoot.get("inscrito")));

                    predicates.add(cb.not(root.get("id").in(subquery)));
                }
            }

            if (filter.getFinalizado() != null) {
                predicates.add(cb.equal(root.get("finalizado"), filter.getFinalizado()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Builds an EXISTS predicate over the concurso_cargo association, keeping the
     * main query free of row-multiplying joins.
     */
    private static Predicate existsCargoPredicate(Root<Concurso> root, CriteriaQuery<?> query, CriteriaBuilder cb,
                                                  BiFunction<Root<ConcursoCargo>, CriteriaBuilder, Predicate> constraint) {
        Subquery<Integer> subquery = query.subquery(Integer.class);
        Root<ConcursoCargo> cc = subquery.from(ConcursoCargo.class);
        subquery.select(cb.literal(1));
        subquery.where(cb.and(
                cb.equal(cc.get("concurso").get("id"), root.get("id")),
                constraint.apply(cc, cb)
        ));
        return cb.exists(subquery);
    }
}
