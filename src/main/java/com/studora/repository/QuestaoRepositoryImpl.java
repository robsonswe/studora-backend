package com.studora.repository;

import com.studora.dto.request.SimuladoGenerationRequest;
import com.studora.entity.NivelCargo;
import java.time.LocalDateTime;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class QuestaoRepositoryImpl implements QuestaoRepositoryCustom {

    @PersistenceContext
    private EntityManager em;

    @Override
    public List<Long> findIdsBySubtemaWithPreferences(Long subtemaId, SimuladoGenerationRequest request, List<Long> excludeIds, Pageable pageable) {
        String scopeJoin = "JOIN q.subtemas s";
        String scopeWhere = "s.id = :scopeId";
        return executeQuery(scopeJoin, scopeWhere, subtemaId, null, null, request, excludeIds, pageable);
    }

    @Override
    public List<Long> findIdsByTemaWithPreferences(Long temaId, List<Long> avoidSubtemaIds, SimuladoGenerationRequest request, List<Long> excludeIds, Pageable pageable) {
        String scopeJoin = "JOIN q.subtemas s JOIN s.tema t";
        String scopeWhere = "t.id = :scopeId";
        
        // Handling avoidSubtemaIds in WHERE
        String extraWhere = null;
        if (avoidSubtemaIds != null && !avoidSubtemaIds.isEmpty() && !avoidSubtemaIds.contains(-1L)) {
            extraWhere = "s.id NOT IN :avoidSubtemaIds";
        }

        return executeQuery(scopeJoin, scopeWhere, temaId, extraWhere, avoidSubtemaIds, request, excludeIds, pageable);
    }

    @Override
    public List<Long> findIdsByDisciplinaWithPreferences(Long disciplinaId, List<Long> avoidTemaIds, List<Long> avoidSubtemaIds, SimuladoGenerationRequest request, List<Long> excludeIds, Pageable pageable) {
        String scopeJoin = "JOIN q.subtemas s JOIN s.tema t JOIN t.disciplina d";
        String scopeWhere = "d.id = :scopeId";
        
        StringBuilder extraWhere = new StringBuilder();
        List<Object> extraParams = new ArrayList<>();
        
        boolean hasAvoidTemas = avoidTemaIds != null && !avoidTemaIds.isEmpty() && !avoidTemaIds.contains(-1L);
        boolean hasAvoidSubtemas = avoidSubtemaIds != null && !avoidSubtemaIds.isEmpty() && !avoidSubtemaIds.contains(-1L);

        if (hasAvoidTemas) {
            extraWhere.append("t.id NOT IN :avoidTemaIds");
        }
        if (hasAvoidSubtemas) {
            if (extraWhere.length() > 0) extraWhere.append(" AND ");
            extraWhere.append("s.id NOT IN :avoidSubtemaIds");
        }

        return executeQuery(scopeJoin, scopeWhere, disciplinaId, extraWhere.length() > 0 ? extraWhere.toString() : null, null, request, excludeIds, pageable, avoidTemaIds, avoidSubtemaIds);
    }

    private List<Long> executeQuery(String scopeJoin, String scopeWhere, Long scopeId, 
                                   String extraWhere, List<Long> avoidSubtemaIdsForTema,
                                   SimuladoGenerationRequest req, List<Long> excludeIds, Pageable pageable) {
        return executeQuery(scopeJoin, scopeWhere, scopeId, extraWhere, avoidSubtemaIdsForTema, req, excludeIds, pageable, null, null);
    }

    private List<Long> executeQuery(String scopeJoin, String scopeWhere, Long scopeId, 
                                   String extraWhere, List<Long> avoidSubtemaIdsForTema,
                                   SimuladoGenerationRequest req, List<Long> excludeIds, Pageable pageable,
                                   List<Long> avoidTemaIds, List<Long> avoidSubtemaIdsForDisc) {
        
        StringBuilder hql = new StringBuilder("SELECT DISTINCT q.id FROM Questao q ");
        hql.append("JOIN q.concurso c LEFT JOIN c.banca b LEFT JOIN c.instituicao i ");
        hql.append(scopeJoin).append(" ");

        // WHERE Clauses
        List<String> whereClauses = new ArrayList<>();
        whereClauses.add("q.anulada = false");
        whereClauses.add("q.desatualizada = false");
        whereClauses.add(scopeWhere);

        if (excludeIds != null && !excludeIds.isEmpty() && !excludeIds.contains(-1L)) {
            whereClauses.add("q.id NOT IN :excludeIds");
        }

        if (extraWhere != null) {
            whereClauses.add(extraWhere);
        }

        // Ignorar Respondidas
        LocalDateTime threshold = LocalDateTime.now().minusMonths(1);
        if (Boolean.TRUE.equals(req.getIgnorarRespondidas())) {
            // Stricter: Exclude ALL answered questions
            whereClauses.add("NOT EXISTS (SELECT 1 FROM Resposta r WHERE r.questao.id = q.id)");
        } else {
            // Default: Exclude only RECENTLY answered questions (within 30 days)
            whereClauses.add("NOT EXISTS (SELECT 1 FROM Resposta r WHERE r.questao.id = q.id AND r.createdAt >= :threshold)");
        }

        // Nivel Filtering (Hard Constraint for 'Teto')
        if (req.getNivel() != null) {
            String nivelFilter = "EXISTS (SELECT 1 FROM q.questaoCargos qc JOIN qc.concursoCargo cc JOIN cc.cargo cargo WHERE ";
            if (req.getNivel() == NivelCargo.SUPERIOR) {
                // Allow all (Superior, Medio, Fundamental) - Logic: "Superior" allows downgrades?
                // User: "if i pick nivel superior ... try to get ... from medio... from fundamental"
                // So Superior allows ALL.
                // Wait, if I pick Superior, I can get anything?
                // User: "opposite is not true tho. if i pick fundamental ... you can't fill it with questoes from medio or superior."
                // So:
                // Superior -> All allowed.
                // Medio -> Medio, Fundamental allowed.
                // Fundamental -> Fundamental allowed.
                nivelFilter += "cargo.nivel IN ('SUPERIOR', 'MEDIO', 'FUNDAMENTAL'))"; // Actually redundant if all enums covered, but safe.
            } else if (req.getNivel() == NivelCargo.MEDIO) {
                nivelFilter += "cargo.nivel IN ('MEDIO', 'FUNDAMENTAL'))";
            } else {
                nivelFilter += "cargo.nivel = 'FUNDAMENTAL')";
            }
            whereClauses.add(nivelFilter);
        }

        hql.append("WHERE ").append(String.join(" AND ", whereClauses));

        // ORDER BY Preferences (Scoring)
        StringBuilder orderBy = new StringBuilder("ORDER BY (0");

        // Banca Preference
        if (req.getBancaId() != null) {
            orderBy.append(" + CASE WHEN b.id = :bancaId THEN 1000 ELSE 0 END");
        }

        // Cargo Preference
        if (req.getCargoId() != null) {
            orderBy.append(" + CASE WHEN EXISTS (SELECT 1 FROM q.questaoCargos qc2 JOIN qc2.concursoCargo cc2 WHERE cc2.cargo.id = :cargoId) THEN 500 ELSE 0 END");
        }

        // Area Preference
        if (req.getAreas() != null && !req.getAreas().isEmpty()) {
            // Case-insensitive check? DB dependent. HQL 'lower()'
            // Construct OR clauses for areas
            // (lower(i.area) IN :areas OR EXISTS (... lower(cargo.area) IN :areas))
            // :areas param should be lowercased list.
            orderBy.append(" + CASE WHEN (lower(i.area) IN :areasLower OR EXISTS (SELECT 1 FROM q.questaoCargos qc3 JOIN qc3.concursoCargo cc3 JOIN cc3.cargo cargo3 WHERE lower(cargo3.area) IN :areasLower)) THEN 100 ELSE 0 END");
        }

        // Nivel Priority
        // Superior (requested) -> Sup(30) > Med(20) > Fund(10)
        // Medio (requested) -> Med(20) > Fund(10)
        // Fund (requested) -> Fund(10)
        // We can just assign static weights to levels found.
        // If question has Superior cargo -> +30. If Medio -> +20. If Fund -> +10.
        // MAX score wins.
        // Query: check if it has Superior, else if Medio...
        // "COALESCE((SELECT MAX(CASE WHEN c4.nivel='SUPERIOR' THEN 30 WHEN c4.nivel='MEDIO' THEN 20 ELSE 10 END) FROM ...), 0)"
        // Simplified: Just add points if it HAS that level.
        orderBy.append(" + CASE WHEN EXISTS (SELECT 1 FROM q.questaoCargos qc4 JOIN qc4.concursoCargo cc4 JOIN cc4.cargo c4 WHERE c4.nivel = 'SUPERIOR') THEN 30 ELSE 0 END");
        orderBy.append(" + CASE WHEN EXISTS (SELECT 1 FROM q.questaoCargos qc5 JOIN qc5.concursoCargo cc5 JOIN cc5.cargo c5 WHERE c5.nivel = 'MEDIO') THEN 20 ELSE 0 END");
        orderBy.append(" + CASE WHEN EXISTS (SELECT 1 FROM q.questaoCargos qc6 JOIN qc6.concursoCargo cc6 JOIN cc6.cargo c6 WHERE c6.nivel = 'FUNDAMENTAL') THEN 10 ELSE 0 END");

        orderBy.append(") DESC, RANDOM()");

        hql.append(" ").append(orderBy.toString());

        Query query = em.createQuery(hql.toString());

        // Set Parameters
        query.setParameter("scopeId", scopeId);
        if (hql.indexOf(":threshold") != -1) {
            query.setParameter("threshold", threshold);
        }
        
        if (excludeIds != null && !excludeIds.isEmpty() && !excludeIds.contains(-1L)) {
            query.setParameter("excludeIds", excludeIds);
        }
        if (avoidSubtemaIdsForTema != null && !avoidSubtemaIdsForTema.isEmpty() && !avoidSubtemaIdsForTema.contains(-1L)) {
            query.setParameter("avoidSubtemaIds", avoidSubtemaIdsForTema);
        }
        if (avoidTemaIds != null && !avoidTemaIds.isEmpty() && !avoidTemaIds.contains(-1L)) {
            query.setParameter("avoidTemaIds", avoidTemaIds);
        }
        if (avoidSubtemaIdsForDisc != null && !avoidSubtemaIdsForDisc.isEmpty() && !avoidSubtemaIdsForDisc.contains(-1L)) {
            query.setParameter("avoidSubtemaIds", avoidSubtemaIdsForDisc);
        } // Fix: Used generic param name in query building, need to match logic

        if (req.getBancaId() != null) {
            query.setParameter("bancaId", req.getBancaId());
        }
        if (req.getCargoId() != null) {
            query.setParameter("cargoId", req.getCargoId());
        }
        if (req.getAreas() != null && !req.getAreas().isEmpty()) {
            query.setParameter("areasLower", req.getAreas().stream().map(String::toLowerCase).collect(Collectors.toList()));
        }

        query.setMaxResults(pageable.getPageSize());
        
        return query.getResultList();
    }
}
