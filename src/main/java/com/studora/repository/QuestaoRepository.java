package com.studora.repository;

import com.studora.entity.Questao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuestaoRepository extends JpaRepository<Questao, Long>, JpaSpecificationExecutor<Questao>, QuestaoRepositoryCustom {
    boolean existsByConcursoId(Long concursoId);
    boolean existsBySubtemasId(Long subtemaId);

    @Query("SELECT DISTINCT q FROM Questao q " +
           "LEFT JOIN FETCH q.alternativas " +
           "LEFT JOIN FETCH q.subtemas " +
           "LEFT JOIN FETCH q.questaoCargos qc " +
           "LEFT JOIN FETCH qc.concursoCargo " +
           "LEFT JOIN FETCH q.concurso")
    List<Questao> findAllWithDetails();

    @Query("SELECT DISTINCT q FROM Questao q " +
           "LEFT JOIN FETCH q.alternativas a " +
           "LEFT JOIN FETCH q.subtemas " +
           "LEFT JOIN FETCH q.questaoCargos qc " +
           "LEFT JOIN FETCH qc.concursoCargo cc " +
           "LEFT JOIN FETCH cc.cargo " +
           "LEFT JOIN FETCH q.concurso " +
           "LEFT JOIN FETCH q.respostas r " +
           "LEFT JOIN FETCH r.alternativaEscolhida " +
           "WHERE q.id IN :ids")
    List<Questao> findByIdsWithDetails(@Param("ids") List<Long> ids);

    @Query("SELECT DISTINCT q FROM Questao q " +
           "LEFT JOIN FETCH q.alternativas a " +
           "LEFT JOIN FETCH q.subtemas " +
           "LEFT JOIN FETCH q.questaoCargos qc " +
           "LEFT JOIN FETCH qc.concursoCargo cc " +
           "LEFT JOIN FETCH cc.cargo " +
           "LEFT JOIN FETCH q.concurso " +
           "LEFT JOIN FETCH q.respostas r " +
           "LEFT JOIN FETCH r.alternativaEscolhida " +
           "WHERE q.id = :id")
    Optional<Questao> findByIdWithDetails(@Param("id") Long id);

    // --- Batch question count queries ---

    @Query("SELECT s.id, COUNT(DISTINCT q.id) FROM Questao q JOIN q.subtemas s WHERE s.id IN :ids GROUP BY s.id")
    List<Object[]> countQuestoesBySubtemaIds(@Param("ids") List<Long> ids);

    @Query("SELECT s.tema.id, COUNT(DISTINCT q.id) FROM Questao q JOIN q.subtemas s WHERE s.tema.id IN :ids GROUP BY s.tema.id")
    List<Object[]> countQuestoesByTemaIds(@Param("ids") List<Long> ids);

    @Query("SELECT s.tema.disciplina.id, COUNT(DISTINCT q.id) FROM Questao q JOIN q.subtemas s WHERE s.tema.disciplina.id IN :ids GROUP BY s.tema.disciplina.id")
    List<Object[]> countQuestoesByDisciplinaIds(@Param("ids") List<Long> ids);
}
