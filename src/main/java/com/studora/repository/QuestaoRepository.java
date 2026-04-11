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
           "LEFT JOIN FETCH q.subtemas s " +
           "LEFT JOIN FETCH s.tema st " +
           "LEFT JOIN FETCH st.disciplina " +
           "LEFT JOIN FETCH q.questaoCargos qc " +
           "LEFT JOIN FETCH qc.concursoCargo " +
           "LEFT JOIN FETCH q.concurso")
    List<Questao> findAllWithDetails();

    @Query("SELECT DISTINCT q FROM Questao q " +
           "LEFT JOIN FETCH q.alternativas a " +
           "LEFT JOIN FETCH q.subtemas s " +
           "LEFT JOIN FETCH s.tema st " +
           "LEFT JOIN FETCH st.disciplina " +
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
           "LEFT JOIN FETCH q.subtemas s " +
           "LEFT JOIN FETCH s.tema st " +
           "LEFT JOIN FETCH st.disciplina " +
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

    @Query("SELECT c.banca.id, COUNT(DISTINCT q.id) FROM Questao q JOIN q.concurso c WHERE c.banca.id IN :ids AND q.anulada = false GROUP BY c.banca.id")
    List<Object[]> countQuestoesByBancaIds(@Param("ids") List<Long> ids);

    @Query("SELECT c.instituicao.id, COUNT(DISTINCT q.id) FROM Questao q JOIN q.concurso c WHERE c.instituicao.id IN :ids AND q.anulada = false GROUP BY c.instituicao.id")
    List<Object[]> countQuestoesByInstituicaoIds(@Param("ids") List<Long> ids);

    @Query("SELECT qc.concursoCargo.cargo.id, COUNT(DISTINCT q.id) FROM Questao q JOIN q.questaoCargos qc WHERE qc.concursoCargo.cargo.id IN :ids AND q.anulada = false GROUP BY qc.concursoCargo.cargo.id")
    List<Object[]> countQuestoesByCargoIds(@Param("ids") List<Long> ids);

    // --- Granular breakdown queries for Disciplina ---
    @Query("SELECT CAST(qc.concursoCargo.cargo.nivel AS string), COUNT(DISTINCT q.id) FROM Questao q JOIN q.subtemas s JOIN q.questaoCargos qc WHERE s.tema.disciplina.id = :disciplinaId AND q.anulada = false GROUP BY qc.concursoCargo.cargo.nivel")
    List<Object[]> countQuestoesByDisciplinaIdGroupByNivel(@Param("disciplinaId") Long disciplinaId);

    @Query("SELECT c.banca.id, COUNT(DISTINCT q.id) FROM Questao q JOIN q.subtemas s JOIN q.concurso c WHERE s.tema.disciplina.id = :disciplinaId AND q.anulada = false GROUP BY c.banca.id")
    List<Object[]> countQuestoesByDisciplinaIdGroupByBanca(@Param("disciplinaId") Long disciplinaId);

    @Query("SELECT c.instituicao.id, COUNT(DISTINCT q.id) FROM Questao q JOIN q.subtemas s JOIN q.concurso c WHERE s.tema.disciplina.id = :disciplinaId AND q.anulada = false GROUP BY c.instituicao.id")
    List<Object[]> countQuestoesByDisciplinaIdGroupByInstituicao(@Param("disciplinaId") Long disciplinaId);

    @Query("SELECT c.instituicao.area, COUNT(DISTINCT q.id) FROM Questao q JOIN q.subtemas s JOIN q.concurso c WHERE s.tema.disciplina.id = :disciplinaId AND q.anulada = false GROUP BY c.instituicao.area")
    List<Object[]> countQuestoesByDisciplinaIdGroupByAreaInstituicao(@Param("disciplinaId") Long disciplinaId);

    @Query("SELECT qc.concursoCargo.cargo.id, COUNT(DISTINCT q.id) FROM Questao q JOIN q.subtemas s JOIN q.questaoCargos qc WHERE s.tema.disciplina.id = :disciplinaId AND q.anulada = false GROUP BY qc.concursoCargo.cargo.id")
    List<Object[]> countQuestoesByDisciplinaIdGroupByCargo(@Param("disciplinaId") Long disciplinaId);

    @Query("SELECT qc.concursoCargo.cargo.area, COUNT(DISTINCT q.id) FROM Questao q JOIN q.subtemas s JOIN q.questaoCargos qc WHERE s.tema.disciplina.id = :disciplinaId AND q.anulada = false GROUP BY qc.concursoCargo.cargo.area")
    List<Object[]> countQuestoesByDisciplinaIdGroupByAreaCargo(@Param("disciplinaId") Long disciplinaId);

    // --- Granular breakdown queries for Tema ---
    @Query("SELECT CAST(qc.concursoCargo.cargo.nivel AS string), COUNT(DISTINCT q.id) FROM Questao q JOIN q.subtemas s JOIN q.questaoCargos qc WHERE s.tema.id = :temaId AND q.anulada = false GROUP BY qc.concursoCargo.cargo.nivel")
    List<Object[]> countQuestoesByTemaIdGroupByNivel(@Param("temaId") Long temaId);

    @Query("SELECT c.banca.id, COUNT(DISTINCT q.id) FROM Questao q JOIN q.subtemas s JOIN q.concurso c WHERE s.tema.id = :temaId AND q.anulada = false GROUP BY c.banca.id")
    List<Object[]> countQuestoesByTemaIdGroupByBanca(@Param("temaId") Long temaId);

    @Query("SELECT c.instituicao.id, COUNT(DISTINCT q.id) FROM Questao q JOIN q.subtemas s JOIN q.concurso c WHERE s.tema.id = :temaId AND q.anulada = false GROUP BY c.instituicao.id")
    List<Object[]> countQuestoesByTemaIdGroupByInstituicao(@Param("temaId") Long temaId);

    @Query("SELECT c.instituicao.area, COUNT(DISTINCT q.id) FROM Questao q JOIN q.subtemas s JOIN q.concurso c WHERE s.tema.id = :temaId AND q.anulada = false GROUP BY c.instituicao.area")
    List<Object[]> countQuestoesByTemaIdGroupByAreaInstituicao(@Param("temaId") Long temaId);

    @Query("SELECT qc.concursoCargo.cargo.id, COUNT(DISTINCT q.id) FROM Questao q JOIN q.subtemas s JOIN q.questaoCargos qc WHERE s.tema.id = :temaId AND q.anulada = false GROUP BY qc.concursoCargo.cargo.id")
    List<Object[]> countQuestoesByTemaIdGroupByCargo(@Param("temaId") Long temaId);

    @Query("SELECT qc.concursoCargo.cargo.area, COUNT(DISTINCT q.id) FROM Questao q JOIN q.subtemas s JOIN q.questaoCargos qc WHERE s.tema.id = :temaId AND q.anulada = false GROUP BY qc.concursoCargo.cargo.area")
    List<Object[]> countQuestoesByTemaIdGroupByAreaCargo(@Param("temaId") Long temaId);

    // --- Granular breakdown queries for Subtema ---
    @Query("SELECT CAST(qc.concursoCargo.cargo.nivel AS string), COUNT(DISTINCT q.id) FROM Questao q JOIN q.subtemas s JOIN q.questaoCargos qc WHERE s.id = :subtemaId AND q.anulada = false GROUP BY qc.concursoCargo.cargo.nivel")
    List<Object[]> countQuestoesBySubtemaIdGroupByNivel(@Param("subtemaId") Long subtemaId);

    @Query("SELECT c.banca.id, COUNT(DISTINCT q.id) FROM Questao q JOIN q.subtemas s JOIN q.concurso c WHERE s.id = :subtemaId AND q.anulada = false GROUP BY c.banca.id")
    List<Object[]> countQuestoesBySubtemaIdGroupByBanca(@Param("subtemaId") Long subtemaId);

    @Query("SELECT c.instituicao.id, COUNT(DISTINCT q.id) FROM Questao q JOIN q.subtemas s JOIN q.concurso c WHERE s.id = :subtemaId AND q.anulada = false GROUP BY c.instituicao.id")
    List<Object[]> countQuestoesBySubtemaIdGroupByInstituicao(@Param("subtemaId") Long subtemaId);

    @Query("SELECT c.instituicao.area, COUNT(DISTINCT q.id) FROM Questao q JOIN q.subtemas s JOIN q.concurso c WHERE s.id = :subtemaId AND q.anulada = false GROUP BY c.instituicao.area")
    List<Object[]> countQuestoesBySubtemaIdGroupByAreaInstituicao(@Param("subtemaId") Long subtemaId);

    @Query("SELECT qc.concursoCargo.cargo.id, COUNT(DISTINCT q.id) FROM Questao q JOIN q.subtemas s JOIN q.questaoCargos qc WHERE s.id = :subtemaId AND q.anulada = false GROUP BY qc.concursoCargo.cargo.id")
    List<Object[]> countQuestoesBySubtemaIdGroupByCargo(@Param("subtemaId") Long subtemaId);

    @Query("SELECT qc.concursoCargo.cargo.area, COUNT(DISTINCT q.id) FROM Questao q JOIN q.subtemas s JOIN q.questaoCargos qc WHERE s.id = :subtemaId AND q.anulada = false GROUP BY qc.concursoCargo.cargo.area")
    List<Object[]> countQuestoesBySubtemaIdGroupByAreaCargo(@Param("subtemaId") Long subtemaId);

    // --- Granular breakdown queries for Banca/Cargo/Instituicao ---

    @Query("SELECT CAST(cc.cargo.nivel AS string), COUNT(DISTINCT q.id) FROM Questao q JOIN q.concurso c JOIN c.concursoCargos cc WHERE c.banca.id = :bancaId AND q.anulada = false GROUP BY cc.cargo.nivel")
    List<Object[]> countQuestoesByBancaIdGroupByNivel(@Param("bancaId") Long bancaId);

    @Query("SELECT c.instituicao.area, COUNT(DISTINCT q.id) FROM Questao q JOIN q.concurso c WHERE c.banca.id = :bancaId AND q.anulada = false GROUP BY c.instituicao.area")
    List<Object[]> countQuestoesByBancaIdGroupByAreaInstituicao(@Param("bancaId") Long bancaId);

    @Query("SELECT cc.cargo.area, COUNT(DISTINCT q.id) FROM Questao q JOIN q.concurso c JOIN c.concursoCargos cc WHERE c.banca.id = :bancaId AND q.anulada = false GROUP BY cc.cargo.area")
    List<Object[]> countQuestoesByBancaIdGroupByAreaCargo(@Param("bancaId") Long bancaId);

    // --- Granular breakdown queries for Instituicao ---
    @Query("SELECT CAST(cc.cargo.nivel AS string), COUNT(DISTINCT q.id) FROM Questao q JOIN q.concurso c JOIN c.concursoCargos cc WHERE c.instituicao.id = :instituicaoId AND q.anulada = false GROUP BY cc.cargo.nivel")
    List<Object[]> countQuestoesByInstituicaoIdGroupByNivel(@Param("instituicaoId") Long instituicaoId);

    @Query("SELECT c.banca.id, COUNT(DISTINCT q.id) FROM Questao q JOIN q.concurso c WHERE c.instituicao.id = :instituicaoId AND q.anulada = false GROUP BY c.banca.id")
    List<Object[]> countQuestoesByInstituicaoIdGroupByBanca(@Param("instituicaoId") Long instituicaoId);

    @Query("SELECT cc.cargo.id, COUNT(DISTINCT q.id) FROM Questao q JOIN q.concurso c JOIN c.concursoCargos cc WHERE c.instituicao.id = :instituicaoId AND q.anulada = false GROUP BY cc.cargo.id")
    List<Object[]> countQuestoesByInstituicaoIdGroupByCargo(@Param("instituicaoId") Long instituicaoId);

    @Query("SELECT cc.cargo.area, COUNT(DISTINCT q.id) FROM Questao q JOIN q.concurso c JOIN c.concursoCargos cc WHERE c.instituicao.id = :instituicaoId AND q.anulada = false GROUP BY cc.cargo.area")
    List<Object[]> countQuestoesByInstituicaoIdGroupByAreaCargo(@Param("instituicaoId") Long instituicaoId);

    // --- Granular breakdown queries for Cargo ---
    @Query("SELECT CAST(qc.concursoCargo.cargo.nivel AS string), COUNT(DISTINCT q.id) FROM Questao q JOIN q.questaoCargos qc WHERE qc.concursoCargo.cargo.id = :cargoId AND q.anulada = false GROUP BY qc.concursoCargo.cargo.nivel")
    List<Object[]> countQuestoesByCargoIdGroupByNivel(@Param("cargoId") Long cargoId);

    @Query("SELECT c.banca.id, COUNT(DISTINCT q.id) FROM Questao q JOIN q.questaoCargos qc JOIN q.concurso c WHERE qc.concursoCargo.cargo.id = :cargoId AND q.anulada = false GROUP BY c.banca.id")
    List<Object[]> countQuestoesByCargoIdGroupByBanca(@Param("cargoId") Long cargoId);

    @Query("SELECT qc.concursoCargo.cargo.area, COUNT(DISTINCT q.id) FROM Questao q JOIN q.questaoCargos qc WHERE qc.concursoCargo.cargo.id = :cargoId AND q.anulada = false GROUP BY qc.concursoCargo.cargo.area")
    List<Object[]> countQuestoesByCargoIdGroupByAreaCargo(@Param("cargoId") Long cargoId);

    @Query("SELECT c.instituicao.area, COUNT(DISTINCT q.id) FROM Questao q JOIN q.questaoCargos qc JOIN q.concurso c WHERE qc.concursoCargo.cargo.id = :cargoId AND q.anulada = false GROUP BY c.instituicao.area")
    List<Object[]> countQuestoesByCargoIdGroupByAreaInstituicao(@Param("cargoId") Long cargoId);

    // --- Autoral aggregate count queries for taxonomy scopes ---
    @Query("SELECT s.tema.disciplina.id, COUNT(DISTINCT q.id) FROM Questao q JOIN q.subtemas s WHERE s.tema.disciplina.id IN :ids AND q.autoral = true GROUP BY s.tema.disciplina.id")
    List<Object[]> countAutoralQuestoesByDisciplinaIds(@Param("ids") List<Long> ids);

    @Query("SELECT s.tema.id, COUNT(DISTINCT q.id) FROM Questao q JOIN q.subtemas s WHERE s.tema.id IN :ids AND q.autoral = true GROUP BY s.tema.id")
    List<Object[]> countAutoralQuestoesByTemaIds(@Param("ids") List<Long> ids);

    @Query("SELECT s.id, COUNT(DISTINCT q.id) FROM Questao q JOIN q.subtemas s WHERE s.id IN :ids AND q.autoral = true GROUP BY s.id")
    List<Object[]> countAutoralQuestoesBySubtemaIds(@Param("ids") List<Long> ids);

    @Query("SELECT COUNT(DISTINCT q.id) FROM Questao q JOIN q.subtemas s WHERE s.tema.disciplina.id = :id AND q.autoral = true")
    Long countAutoralQuestoesByDisciplinaId(@Param("id") Long id);

    @Query("SELECT COUNT(DISTINCT q.id) FROM Questao q JOIN q.subtemas s WHERE s.tema.id = :id AND q.autoral = true")
    Long countAutoralQuestoesByTemaId(@Param("id") Long id);

    @Query("SELECT COUNT(DISTINCT q.id) FROM Questao q JOIN q.subtemas s WHERE s.id = :id AND q.autoral = true")
    Long countAutoralQuestoesBySubtemaId(@Param("id") Long id);
}
