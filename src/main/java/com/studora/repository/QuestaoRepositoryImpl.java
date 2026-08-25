package com.studora.repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.studora.dto.request.SimuladoGenerationRequest;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

@Repository
public class QuestaoRepositoryImpl implements QuestaoRepositoryCustom {

    @PersistenceContext
    private EntityManager em;

    @Override
    public List<Long> findIdsBySubtemaWithPreferences(Long subtemaId, SimuladoGenerationRequest request, List<Long> excludeIds, Pageable pageable) {
        String scopeExists = "EXISTS (SELECT 1 FROM q.questaoSubtemas qs_s JOIN qs_s.subtema s WHERE s.id = :scopeId)";
        return executeQuery(scopeExists, subtemaId, null, null, request, excludeIds, pageable);
    }

    @Override
    public List<Long> findIdsByTemaWithPreferences(Long temaId, List<Long> avoidSubtemaIds, SimuladoGenerationRequest request, List<Long> excludeIds, Pageable pageable) {
        String scopeExists = "EXISTS (SELECT 1 FROM q.questaoSubtemas qs_s JOIN qs_s.subtema s JOIN s.tema t WHERE t.id = :scopeId)";

        // Handling avoidSubtemaIds in the scope subquery
        if (avoidSubtemaIds != null && !avoidSubtemaIds.isEmpty() && !avoidSubtemaIds.contains(-1L)) {
            scopeExists += " AND s.id NOT IN :avoidSubtemaIds";
        }

        return executeQuery(scopeExists, temaId, null, avoidSubtemaIds, request, excludeIds, pageable);
    }

    @Override
    public List<Long> findIdsByDisciplinaWithPreferences(Long disciplinaId, List<Long> avoidTemaIds, List<Long> avoidSubtemaIds, SimuladoGenerationRequest request, List<Long> excludeIds, Pageable pageable) {
        StringBuilder scopeExists = new StringBuilder(
            "EXISTS (SELECT 1 FROM q.questaoSubtemas qs_s JOIN qs_s.subtema s JOIN s.tema t JOIN t.disciplina d WHERE d.id = :scopeId");

        boolean hasAvoidTemas = avoidTemaIds != null && !avoidTemaIds.isEmpty() && !avoidTemaIds.contains(-1L);
        boolean hasAvoidSubtemas = avoidSubtemaIds != null && !avoidSubtemaIds.isEmpty() && !avoidSubtemaIds.contains(-1L);

        if (hasAvoidTemas) {
            scopeExists.append(" AND t.id NOT IN :avoidTemaIds");
        }
        if (hasAvoidSubtemas) {
            scopeExists.append(" AND s.id NOT IN :avoidSubtemaIds");
        }
        scopeExists.append(")");

        return executeQuery(scopeExists.toString(), disciplinaId,
                hasAvoidTemas ? avoidTemaIds : null,
                hasAvoidSubtemas ? avoidSubtemaIds : null,
                request, excludeIds, pageable);
    }

    private List<Long> executeQuery(String scopeExists, Long scopeId,
                                   List<Long> avoidTemaIds, List<Long> avoidSubtemaIds,
                                   SimuladoGenerationRequest req, List<Long> excludeIds, Pageable pageable) {
        // Top-level joins over to-many associations are avoided on purpose:
        // they multiply rows and force DISTINCT, which PostgreSQL rejects with
        // ORDER BY expressions outside the SELECT list.
        StringBuilder hql = new StringBuilder("SELECT q.id FROM Questao q ");

        // WHERE Clauses
        List<String> whereClauses = new ArrayList<>();
        whereClauses.add("q.anulada = false");
        whereClauses.add("q.desatualizada = false");
        whereClauses.add(scopeExists);

        if (excludeIds != null && !excludeIds.isEmpty() && !excludeIds.contains(-1L)) {
            whereClauses.add("q.id NOT IN :excludeIds");
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
        // When includeAutoral is true, autoral questions bypass the nivel filter
        if (req.getNivel() != null) {
            String nivelLevels = switch (req.getNivel()) {
                case SUPERIOR -> "'SUPERIOR', 'MEDIO', 'FUNDAMENTAL'";
                case MEDIO -> "'MEDIO', 'FUNDAMENTAL'";
                case FUNDAMENTAL -> "'FUNDAMENTAL'";
            };
            String nivelFilter =
                "(q.autoral = true OR EXISTS (SELECT 1 FROM q.secoes qsNivel JOIN qsNivel.provaSecao psNivel JOIN psNivel.prova provaNivel JOIN provaNivel.concursoCargo ccNivel JOIN ccNivel.cargo cargoNivel WHERE cargoNivel.nivel IN (" + nivelLevels + ")))";
            whereClauses.add(nivelFilter);
        }

        hql.append("WHERE ").append(String.join(" AND ", whereClauses));

        // ORDER BY Preferences (Scoring)
        StringBuilder orderBy = new StringBuilder("ORDER BY (0");

        // Banca Preference
        if (req.getBancaId() != null) {
            orderBy.append(" + CASE WHEN EXISTS (SELECT 1 FROM q.secoes qsBanco JOIN qsBanco.provaSecao psBanco JOIN psBanco.prova provaBanco WHERE provaBanco.concurso.banca.id = :bancaId) THEN 1000 ELSE 0 END");
        }

        // Cargo Preference
        if (req.getCargoId() != null) {
            orderBy.append(" + CASE WHEN EXISTS (SELECT 1 FROM q.secoes qsCargo JOIN qsCargo.provaSecao psCargo JOIN psCargo.prova provaCargo JOIN provaCargo.concursoCargo ccCargo WHERE ccCargo.cargo.id = :cargoId) THEN 500 ELSE 0 END");
        }

        // Area Preference
        if (req.getAreas() != null && !req.getAreas().isEmpty()) {
            orderBy.append(" + CASE WHEN EXISTS (SELECT 1 FROM q.secoes qsArea JOIN qsArea.provaSecao psArea JOIN psArea.prova provaArea JOIN provaArea.concurso concursoArea JOIN concursoArea.instituicao instituicaoArea WHERE lower(instituicaoArea.area) IN :areasLower)")
                   .append(" OR EXISTS (SELECT 1 FROM q.secoes qsArea2 JOIN qsArea2.provaSecao psArea2 JOIN psArea2.prova provaArea2 JOIN provaArea2.concursoCargo ccArea2 JOIN ccArea2.cargo cargoArea2 WHERE lower(cargoArea2.area) IN :areasLower)")
                   .append(" THEN 100 ELSE 0 END");
        }

        // Nivel Priority
        orderBy.append(" + CASE WHEN EXISTS (SELECT 1 FROM q.secoes qsSup JOIN qsSup.provaSecao psSup JOIN psSup.prova provaSup JOIN provaSup.concursoCargo ccSup JOIN ccSup.cargo cSup WHERE cSup.nivel = 'SUPERIOR') THEN 30 ELSE 0 END");
        orderBy.append(" + CASE WHEN EXISTS (SELECT 1 FROM q.secoes qsMed JOIN qsMed.provaSecao psMed JOIN psMed.prova provaMed JOIN provaMed.concursoCargo ccMed JOIN ccMed.cargo cMed WHERE cMed.nivel = 'MEDIO') THEN 20 ELSE 0 END");
        orderBy.append(" + CASE WHEN EXISTS (SELECT 1 FROM q.secoes qsFun JOIN qsFun.provaSecao psFun JOIN psFun.prova provaFun JOIN provaFun.concursoCargo ccFun JOIN ccFun.cargo cFun WHERE cFun.nivel = 'FUNDAMENTAL') THEN 10 ELSE 0 END");

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
        if (avoidTemaIds != null && !avoidTemaIds.isEmpty() && !avoidTemaIds.contains(-1L)) {
            query.setParameter("avoidTemaIds", avoidTemaIds);
        }
        if (avoidSubtemaIds != null && !avoidSubtemaIds.isEmpty() && !avoidSubtemaIds.contains(-1L)) {
            query.setParameter("avoidSubtemaIds", avoidSubtemaIds);
        }

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

