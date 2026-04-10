package com.studora.repository;

import com.studora.entity.Resposta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.List;
import java.util.Collection;

@Repository
public interface RespostaRepository extends JpaRepository<Resposta, Long> {
    
    Optional<Resposta> findFirstByQuestaoIdOrderByCreatedAtDesc(Long questaoId);

    List<Resposta> findByQuestaoIdOrderByCreatedAtDesc(Long questaoId);
    
    @Transactional
    @Modifying
    @Query("DELETE FROM Resposta r WHERE r.questao.id = :questaoId")
    void deleteByQuestaoId(@Param("questaoId") Long questaoId);

    @Query("SELECT r FROM Resposta r " +
           "JOIN FETCH r.questao " +
           "JOIN FETCH r.alternativaEscolhida " +
           "WHERE r.id = :id")
    Optional<Resposta> findByIdWithDetails(@Param("id") Long id);

    @Query("SELECT r FROM Resposta r " +
           "JOIN FETCH r.questao " +
           "JOIN FETCH r.alternativaEscolhida " +
           "WHERE r.questao.id = :questaoId " +
           "ORDER BY r.createdAt DESC")
    List<Resposta> findByQuestaoIdWithDetails(@Param("questaoId") Long questaoId);

    @Query("SELECT r FROM Resposta r " +
           "JOIN FETCH r.questao " +
           "JOIN FETCH r.alternativaEscolhida " +
           "WHERE r.questao.id IN :questaoIds " +
           "ORDER BY r.createdAt DESC")
    List<Resposta> findByQuestaoIdInWithDetails(@Param("questaoIds") Collection<Long> questaoIds);

    List<Resposta> findBySimuladoId(Long simuladoId);

    int countBySimuladoId(Long simuladoId);

    @Transactional
    @Modifying
    @Query("UPDATE Resposta r SET r.simulado = null WHERE r.simulado.id = :simuladoId")
    void detachSimulado(@Param("simuladoId") Long simuladoId);

    @Query("SELECT DISTINCT r FROM Resposta r " +
           "JOIN FETCH r.questao q " +
           "JOIN FETCH r.alternativaEscolhida " +
           "JOIN FETCH q.alternativas " +
           "ORDER BY r.createdAt ASC")
    List<Resposta> findAllWithFullDetails();

    @Query("SELECT DISTINCT r FROM Resposta r " +
           "JOIN FETCH r.questao q " +
           "JOIN FETCH r.alternativaEscolhida " +
           "JOIN FETCH q.alternativas " +
           "WHERE r.createdAt >= :since " +
           "ORDER BY r.createdAt ASC")
    List<Resposta> findAllWithFullDetailsSince(@Param("since") java.time.LocalDateTime since);

    // --- Batch: questoesRespondidas ---
    @Query("SELECT s.id, COUNT(DISTINCT r.questao.id) FROM Resposta r JOIN r.questao.subtemas s WHERE s.id IN :ids GROUP BY s.id")
    List<Object[]> countRespondidasBySubtemaIds(@Param("ids") List<Long> ids);

    @Query("SELECT s.tema.id, COUNT(DISTINCT r.questao.id) FROM Resposta r JOIN r.questao.subtemas s WHERE s.tema.id IN :ids GROUP BY s.tema.id")
    List<Object[]> countRespondidasByTemaIds(@Param("ids") List<Long> ids);

    @Query("SELECT s.tema.disciplina.id, COUNT(DISTINCT r.questao.id) FROM Resposta r JOIN r.questao.subtemas s WHERE s.tema.disciplina.id IN :ids GROUP BY s.tema.disciplina.id")
    List<Object[]> countRespondidasByDisciplinaIds(@Param("ids") List<Long> ids);

    @Query("SELECT c.banca.id, COUNT(DISTINCT r.questao.id) FROM Resposta r JOIN r.questao q JOIN q.concurso c WHERE c.banca.id IN :ids GROUP BY c.banca.id")
    List<Object[]> countRespondidasByBancaIds(@Param("ids") List<Long> ids);

    @Query("SELECT c.instituicao.id, COUNT(DISTINCT r.questao.id) FROM Resposta r JOIN r.questao q JOIN q.concurso c WHERE c.instituicao.id IN :ids GROUP BY c.instituicao.id")
    List<Object[]> countRespondidasByInstituicaoIds(@Param("ids") List<Long> ids);

    @Query("SELECT qc.concursoCargo.cargo.id, COUNT(DISTINCT r.questao.id) FROM Resposta r JOIN r.questao q JOIN q.questaoCargos qc WHERE qc.concursoCargo.cargo.id IN :ids GROUP BY qc.concursoCargo.cargo.id")
    List<Object[]> countRespondidasByCargoIds(@Param("ids") List<Long> ids);

    // --- Granular breakdown queries for Disciplina ---
    @Query("SELECT CAST(qc.concursoCargo.cargo.nivel AS string), COUNT(DISTINCT r.questao.id), SUM(CASE WHEN ae.correta = true THEN 1 ELSE 0 END) FROM Resposta r JOIN r.questao q JOIN r.alternativaEscolhida ae JOIN q.subtemas s JOIN q.questaoCargos qc WHERE s.tema.disciplina.id = :disciplinaId AND q.anulada = false GROUP BY qc.concursoCargo.cargo.nivel")
    List<Object[]> countRespondidasAcertadasByDisciplinaIdGroupByNivel(@Param("disciplinaId") Long disciplinaId);

    @Query("SELECT c.banca.id, COUNT(DISTINCT r.questao.id), SUM(CASE WHEN ae.correta = true THEN 1 ELSE 0 END) FROM Resposta r JOIN r.questao q JOIN r.alternativaEscolhida ae JOIN q.subtemas s JOIN q.concurso c WHERE s.tema.disciplina.id = :disciplinaId AND q.anulada = false GROUP BY c.banca.id")
    List<Object[]> countRespondidasAcertadasByDisciplinaIdGroupByBanca(@Param("disciplinaId") Long disciplinaId);

    @Query("SELECT c.instituicao.id, COUNT(DISTINCT r.questao.id), SUM(CASE WHEN ae.correta = true THEN 1 ELSE 0 END) FROM Resposta r JOIN r.questao q JOIN r.alternativaEscolhida ae JOIN q.subtemas s JOIN q.concurso c WHERE s.tema.disciplina.id = :disciplinaId AND q.anulada = false GROUP BY c.instituicao.id")
    List<Object[]> countRespondidasAcertadasByDisciplinaIdGroupByInstituicao(@Param("disciplinaId") Long disciplinaId);

    @Query("SELECT c.instituicao.area, COUNT(DISTINCT r.questao.id), SUM(CASE WHEN ae.correta = true THEN 1 ELSE 0 END) FROM Resposta r JOIN r.questao q JOIN r.alternativaEscolhida ae JOIN q.subtemas s JOIN q.concurso c WHERE s.tema.disciplina.id = :disciplinaId AND q.anulada = false GROUP BY c.instituicao.area")
    List<Object[]> countRespondidasAcertadasByDisciplinaIdGroupByAreaInstituicao(@Param("disciplinaId") Long disciplinaId);

    @Query("SELECT qc.concursoCargo.cargo.id, COUNT(DISTINCT r.questao.id), SUM(CASE WHEN ae.correta = true THEN 1 ELSE 0 END) FROM Resposta r JOIN r.questao q JOIN r.alternativaEscolhida ae JOIN q.subtemas s JOIN q.questaoCargos qc WHERE s.tema.disciplina.id = :disciplinaId AND q.anulada = false GROUP BY qc.concursoCargo.cargo.id")
    List<Object[]> countRespondidasAcertadasByDisciplinaIdGroupByCargo(@Param("disciplinaId") Long disciplinaId);

    @Query("SELECT qc.concursoCargo.cargo.area, COUNT(DISTINCT r.questao.id), SUM(CASE WHEN ae.correta = true THEN 1 ELSE 0 END) FROM Resposta r JOIN r.questao q JOIN r.alternativaEscolhida ae JOIN q.subtemas s JOIN q.questaoCargos qc WHERE s.tema.disciplina.id = :disciplinaId AND q.anulada = false GROUP BY qc.concursoCargo.cargo.area")
    List<Object[]> countRespondidasAcertadasByDisciplinaIdGroupByAreaCargo(@Param("disciplinaId") Long disciplinaId);

    @Query("SELECT CAST(qc.concursoCargo.cargo.nivel AS string), AVG(r.tempoRespostaSegundos) FROM Resposta r JOIN r.questao q JOIN q.subtemas s JOIN q.questaoCargos qc WHERE s.tema.disciplina.id = :disciplinaId AND r.tempoRespostaSegundos IS NOT NULL GROUP BY qc.concursoCargo.cargo.nivel")
    List<Object[]> avgTempoByDisciplinaIdGroupByNivel(@Param("disciplinaId") Long disciplinaId);

    @Query("SELECT c.banca.id, AVG(r.tempoRespostaSegundos) FROM Resposta r JOIN r.questao q JOIN q.subtemas s JOIN q.concurso c WHERE s.tema.disciplina.id = :disciplinaId AND r.tempoRespostaSegundos IS NOT NULL GROUP BY c.banca.id")
    List<Object[]> avgTempoByDisciplinaIdGroupByBanca(@Param("disciplinaId") Long disciplinaId);

    @Query("SELECT c.instituicao.id, AVG(r.tempoRespostaSegundos) FROM Resposta r JOIN r.questao q JOIN q.subtemas s JOIN q.concurso c WHERE s.tema.disciplina.id = :disciplinaId AND r.tempoRespostaSegundos IS NOT NULL GROUP BY c.instituicao.id")
    List<Object[]> avgTempoByDisciplinaIdGroupByInstituicao(@Param("disciplinaId") Long disciplinaId);

    @Query("SELECT c.instituicao.area, AVG(r.tempoRespostaSegundos) FROM Resposta r JOIN r.questao q JOIN q.subtemas s JOIN q.concurso c WHERE s.tema.disciplina.id = :disciplinaId AND r.tempoRespostaSegundos IS NOT NULL GROUP BY c.instituicao.area")
    List<Object[]> avgTempoByDisciplinaIdGroupByAreaInstituicao(@Param("disciplinaId") Long disciplinaId);

    @Query("SELECT qc.concursoCargo.cargo.id, AVG(r.tempoRespostaSegundos) FROM Resposta r JOIN r.questao q JOIN q.subtemas s JOIN q.questaoCargos qc WHERE s.tema.disciplina.id = :disciplinaId AND r.tempoRespostaSegundos IS NOT NULL GROUP BY qc.concursoCargo.cargo.id")
    List<Object[]> avgTempoByDisciplinaIdGroupByCargo(@Param("disciplinaId") Long disciplinaId);

    @Query("SELECT qc.concursoCargo.cargo.area, AVG(r.tempoRespostaSegundos) FROM Resposta r JOIN r.questao q JOIN q.subtemas s JOIN q.questaoCargos qc WHERE s.tema.disciplina.id = :disciplinaId AND r.tempoRespostaSegundos IS NOT NULL GROUP BY qc.concursoCargo.cargo.area")
    List<Object[]> avgTempoByDisciplinaIdGroupByAreaCargo(@Param("disciplinaId") Long disciplinaId);

    // --- Granular breakdown queries for Tema ---
    @Query("SELECT CAST(qc.concursoCargo.cargo.nivel AS string), COUNT(DISTINCT r.questao.id), SUM(CASE WHEN ae.correta = true THEN 1 ELSE 0 END) FROM Resposta r JOIN r.questao q JOIN r.alternativaEscolhida ae JOIN q.subtemas s JOIN q.questaoCargos qc WHERE s.tema.id = :temaId AND q.anulada = false GROUP BY qc.concursoCargo.cargo.nivel")
    List<Object[]> countRespondidasAcertadasByTemaIdGroupByNivel(@Param("temaId") Long temaId);

    @Query("SELECT c.banca.id, COUNT(DISTINCT r.questao.id), SUM(CASE WHEN ae.correta = true THEN 1 ELSE 0 END) FROM Resposta r JOIN r.questao q JOIN r.alternativaEscolhida ae JOIN q.subtemas s JOIN q.concurso c WHERE s.tema.id = :temaId AND q.anulada = false GROUP BY c.banca.id")
    List<Object[]> countRespondidasAcertadasByTemaIdGroupByBanca(@Param("temaId") Long temaId);

    @Query("SELECT c.instituicao.id, COUNT(DISTINCT r.questao.id), SUM(CASE WHEN ae.correta = true THEN 1 ELSE 0 END) FROM Resposta r JOIN r.questao q JOIN r.alternativaEscolhida ae JOIN q.subtemas s JOIN q.concurso c WHERE s.tema.id = :temaId AND q.anulada = false GROUP BY c.instituicao.id")
    List<Object[]> countRespondidasAcertadasByTemaIdGroupByInstituicao(@Param("temaId") Long temaId);

    @Query("SELECT c.instituicao.area, COUNT(DISTINCT r.questao.id), SUM(CASE WHEN ae.correta = true THEN 1 ELSE 0 END) FROM Resposta r JOIN r.questao q JOIN r.alternativaEscolhida ae JOIN q.subtemas s JOIN q.concurso c WHERE s.tema.id = :temaId AND q.anulada = false GROUP BY c.instituicao.area")
    List<Object[]> countRespondidasAcertadasByTemaIdGroupByAreaInstituicao(@Param("temaId") Long temaId);

    @Query("SELECT qc.concursoCargo.cargo.id, COUNT(DISTINCT r.questao.id), SUM(CASE WHEN ae.correta = true THEN 1 ELSE 0 END) FROM Resposta r JOIN r.questao q JOIN r.alternativaEscolhida ae JOIN q.subtemas s JOIN q.questaoCargos qc WHERE s.tema.id = :temaId AND q.anulada = false GROUP BY qc.concursoCargo.cargo.id")
    List<Object[]> countRespondidasAcertadasByTemaIdGroupByCargo(@Param("temaId") Long temaId);

    @Query("SELECT qc.concursoCargo.cargo.area, COUNT(DISTINCT r.questao.id), SUM(CASE WHEN ae.correta = true THEN 1 ELSE 0 END) FROM Resposta r JOIN r.questao q JOIN r.alternativaEscolhida ae JOIN q.subtemas s JOIN q.questaoCargos qc WHERE s.tema.id = :temaId AND q.anulada = false GROUP BY qc.concursoCargo.cargo.area")
    List<Object[]> countRespondidasAcertadasByTemaIdGroupByAreaCargo(@Param("temaId") Long temaId);

    @Query("SELECT CAST(qc.concursoCargo.cargo.nivel AS string), AVG(r.tempoRespostaSegundos) FROM Resposta r JOIN r.questao q JOIN q.subtemas s JOIN q.questaoCargos qc WHERE s.tema.id = :temaId AND r.tempoRespostaSegundos IS NOT NULL GROUP BY qc.concursoCargo.cargo.nivel")
    List<Object[]> avgTempoByTemaIdGroupByNivel(@Param("temaId") Long temaId);

    @Query("SELECT c.banca.id, AVG(r.tempoRespostaSegundos) FROM Resposta r JOIN r.questao q JOIN q.subtemas s JOIN q.concurso c WHERE s.tema.id = :temaId AND r.tempoRespostaSegundos IS NOT NULL GROUP BY c.banca.id")
    List<Object[]> avgTempoByTemaIdGroupByBanca(@Param("temaId") Long temaId);

    @Query("SELECT c.instituicao.id, AVG(r.tempoRespostaSegundos) FROM Resposta r JOIN r.questao q JOIN q.subtemas s JOIN q.concurso c WHERE s.tema.id = :temaId AND r.tempoRespostaSegundos IS NOT NULL GROUP BY c.instituicao.id")
    List<Object[]> avgTempoByTemaIdGroupByInstituicao(@Param("temaId") Long temaId);

    @Query("SELECT c.instituicao.area, AVG(r.tempoRespostaSegundos) FROM Resposta r JOIN r.questao q JOIN q.subtemas s JOIN q.concurso c WHERE s.tema.id = :temaId AND r.tempoRespostaSegundos IS NOT NULL GROUP BY c.instituicao.area")
    List<Object[]> avgTempoByTemaIdGroupByAreaInstituicao(@Param("temaId") Long temaId);

    @Query("SELECT qc.concursoCargo.cargo.id, AVG(r.tempoRespostaSegundos) FROM Resposta r JOIN r.questao q JOIN q.subtemas s JOIN q.questaoCargos qc WHERE s.tema.id = :temaId AND r.tempoRespostaSegundos IS NOT NULL GROUP BY qc.concursoCargo.cargo.id")
    List<Object[]> avgTempoByTemaIdGroupByCargo(@Param("temaId") Long temaId);

    @Query("SELECT qc.concursoCargo.cargo.area, AVG(r.tempoRespostaSegundos) FROM Resposta r JOIN r.questao q JOIN q.subtemas s JOIN q.questaoCargos qc WHERE s.tema.id = :temaId AND r.tempoRespostaSegundos IS NOT NULL GROUP BY qc.concursoCargo.cargo.area")
    List<Object[]> avgTempoByTemaIdGroupByAreaCargo(@Param("temaId") Long temaId);

    // --- Granular breakdown queries for Subtema ---
    @Query("SELECT CAST(qc.concursoCargo.cargo.nivel AS string), COUNT(DISTINCT r.questao.id), SUM(CASE WHEN ae.correta = true THEN 1 ELSE 0 END) FROM Resposta r JOIN r.questao q JOIN r.alternativaEscolhida ae JOIN q.subtemas s JOIN q.questaoCargos qc WHERE s.id = :subtemaId AND q.anulada = false GROUP BY qc.concursoCargo.cargo.nivel")
    List<Object[]> countRespondidasAcertadasBySubtemaIdGroupByNivel(@Param("subtemaId") Long subtemaId);

    @Query("SELECT c.banca.id, COUNT(DISTINCT r.questao.id), SUM(CASE WHEN ae.correta = true THEN 1 ELSE 0 END) FROM Resposta r JOIN r.questao q JOIN r.alternativaEscolhida ae JOIN q.subtemas s JOIN q.concurso c WHERE s.id = :subtemaId AND q.anulada = false GROUP BY c.banca.id")
    List<Object[]> countRespondidasAcertadasBySubtemaIdGroupByBanca(@Param("subtemaId") Long subtemaId);

    @Query("SELECT c.instituicao.id, COUNT(DISTINCT r.questao.id), SUM(CASE WHEN ae.correta = true THEN 1 ELSE 0 END) FROM Resposta r JOIN r.questao q JOIN r.alternativaEscolhida ae JOIN q.subtemas s JOIN q.concurso c WHERE s.id = :subtemaId AND q.anulada = false GROUP BY c.instituicao.id")
    List<Object[]> countRespondidasAcertadasBySubtemaIdGroupByInstituicao(@Param("subtemaId") Long subtemaId);

    @Query("SELECT c.instituicao.area, COUNT(DISTINCT r.questao.id), SUM(CASE WHEN ae.correta = true THEN 1 ELSE 0 END) FROM Resposta r JOIN r.questao q JOIN r.alternativaEscolhida ae JOIN q.subtemas s JOIN q.concurso c WHERE s.id = :subtemaId AND q.anulada = false GROUP BY c.instituicao.area")
    List<Object[]> countRespondidasAcertadasBySubtemaIdGroupByAreaInstituicao(@Param("subtemaId") Long subtemaId);

    @Query("SELECT qc.concursoCargo.cargo.id, COUNT(DISTINCT r.questao.id), SUM(CASE WHEN ae.correta = true THEN 1 ELSE 0 END) FROM Resposta r JOIN r.questao q JOIN r.alternativaEscolhida ae JOIN q.subtemas s JOIN q.questaoCargos qc WHERE s.id = :subtemaId AND q.anulada = false GROUP BY qc.concursoCargo.cargo.id")
    List<Object[]> countRespondidasAcertadasBySubtemaIdGroupByCargo(@Param("subtemaId") Long subtemaId);

    @Query("SELECT qc.concursoCargo.cargo.area, COUNT(DISTINCT r.questao.id), SUM(CASE WHEN ae.correta = true THEN 1 ELSE 0 END) FROM Resposta r JOIN r.questao q JOIN r.alternativaEscolhida ae JOIN q.subtemas s JOIN q.questaoCargos qc WHERE s.id = :subtemaId AND q.anulada = false GROUP BY qc.concursoCargo.cargo.area")
    List<Object[]> countRespondidasAcertadasBySubtemaIdGroupByAreaCargo(@Param("subtemaId") Long subtemaId);

    @Query("SELECT CAST(qc.concursoCargo.cargo.nivel AS string), AVG(r.tempoRespostaSegundos) FROM Resposta r JOIN r.questao q JOIN q.subtemas s JOIN q.questaoCargos qc WHERE s.id = :subtemaId AND r.tempoRespostaSegundos IS NOT NULL GROUP BY qc.concursoCargo.cargo.nivel")
    List<Object[]> avgTempoBySubtemaIdGroupByNivel(@Param("subtemaId") Long subtemaId);

    @Query("SELECT c.banca.id, AVG(r.tempoRespostaSegundos) FROM Resposta r JOIN r.questao q JOIN q.subtemas s JOIN q.concurso c WHERE s.id = :subtemaId AND r.tempoRespostaSegundos IS NOT NULL GROUP BY c.banca.id")
    List<Object[]> avgTempoBySubtemaIdGroupByBanca(@Param("subtemaId") Long subtemaId);

    @Query("SELECT c.instituicao.id, AVG(r.tempoRespostaSegundos) FROM Resposta r JOIN r.questao q JOIN q.subtemas s JOIN q.concurso c WHERE s.id = :subtemaId AND r.tempoRespostaSegundos IS NOT NULL GROUP BY c.instituicao.id")
    List<Object[]> avgTempoBySubtemaIdGroupByInstituicao(@Param("subtemaId") Long subtemaId);

    @Query("SELECT c.instituicao.area, AVG(r.tempoRespostaSegundos) FROM Resposta r JOIN r.questao q JOIN q.subtemas s JOIN q.concurso c WHERE s.id = :subtemaId AND r.tempoRespostaSegundos IS NOT NULL GROUP BY c.instituicao.area")
    List<Object[]> avgTempoBySubtemaIdGroupByAreaInstituicao(@Param("subtemaId") Long subtemaId);

    @Query("SELECT qc.concursoCargo.cargo.id, AVG(r.tempoRespostaSegundos) FROM Resposta r JOIN r.questao q JOIN q.subtemas s JOIN q.questaoCargos qc WHERE s.id = :subtemaId AND r.tempoRespostaSegundos IS NOT NULL GROUP BY qc.concursoCargo.cargo.id")
    List<Object[]> avgTempoBySubtemaIdGroupByCargo(@Param("subtemaId") Long subtemaId);

    @Query("SELECT qc.concursoCargo.cargo.area, AVG(r.tempoRespostaSegundos) FROM Resposta r JOIN r.questao q JOIN q.subtemas s JOIN q.questaoCargos qc WHERE s.id = :subtemaId AND r.tempoRespostaSegundos IS NOT NULL GROUP BY qc.concursoCargo.cargo.area")
    List<Object[]> avgTempoBySubtemaIdGroupByAreaCargo(@Param("subtemaId") Long subtemaId);

    // --- Batch: questoesAcertadas ---
    @Query("SELECT s.id, COUNT(DISTINCT r.questao.id) FROM Resposta r JOIN r.questao.subtemas s WHERE s.id IN :ids AND r.alternativaEscolhida.correta = true GROUP BY s.id")
    List<Object[]> countAcertadasBySubtemaIds(@Param("ids") List<Long> ids);

    @Query("SELECT s.tema.id, COUNT(DISTINCT r.questao.id) FROM Resposta r JOIN r.questao.subtemas s WHERE s.tema.id IN :ids AND r.alternativaEscolhida.correta = true GROUP BY s.tema.id")
    List<Object[]> countAcertadasByTemaIds(@Param("ids") List<Long> ids);

    @Query("SELECT s.tema.disciplina.id, COUNT(DISTINCT r.questao.id) FROM Resposta r JOIN r.questao.subtemas s WHERE s.tema.disciplina.id IN :ids AND r.alternativaEscolhida.correta = true GROUP BY s.tema.disciplina.id")
    List<Object[]> countAcertadasByDisciplinaIds(@Param("ids") List<Long> ids);

    @Query("SELECT c.banca.id, COUNT(DISTINCT r.questao.id) FROM Resposta r JOIN r.questao q JOIN q.concurso c WHERE c.banca.id IN :ids AND r.alternativaEscolhida.correta = true GROUP BY c.banca.id")
    List<Object[]> countAcertadasByBancaIds(@Param("ids") List<Long> ids);

    @Query("SELECT c.instituicao.id, COUNT(DISTINCT r.questao.id) FROM Resposta r JOIN r.questao q JOIN q.concurso c WHERE c.instituicao.id IN :ids AND r.alternativaEscolhida.correta = true GROUP BY c.instituicao.id")
    List<Object[]> countAcertadasByInstituicaoIds(@Param("ids") List<Long> ids);

    @Query("SELECT qc.concursoCargo.cargo.id, COUNT(DISTINCT r.questao.id) FROM Resposta r JOIN r.questao q JOIN q.questaoCargos qc WHERE qc.concursoCargo.cargo.id IN :ids AND r.alternativaEscolhida.correta = true GROUP BY qc.concursoCargo.cargo.id")
    List<Object[]> countAcertadasByCargoIds(@Param("ids") List<Long> ids);

    // --- Batch: mediaTempoResposta ---
    @Query("SELECT s.id, AVG(r.tempoRespostaSegundos) FROM Resposta r JOIN r.questao.subtemas s WHERE s.id IN :ids AND r.tempoRespostaSegundos IS NOT NULL GROUP BY s.id")
    List<Object[]> avgTempoBySubtemaIds(@Param("ids") List<Long> ids);

    @Query("SELECT s.tema.id, AVG(r.tempoRespostaSegundos) FROM Resposta r JOIN r.questao.subtemas s WHERE s.tema.id IN :ids AND r.tempoRespostaSegundos IS NOT NULL GROUP BY s.tema.id")
    List<Object[]> avgTempoByTemaIds(@Param("ids") List<Long> ids);

    @Query("SELECT s.tema.disciplina.id, AVG(r.tempoRespostaSegundos) FROM Resposta r JOIN r.questao.subtemas s WHERE s.tema.disciplina.id IN :ids AND r.tempoRespostaSegundos IS NOT NULL GROUP BY s.tema.disciplina.id")
    List<Object[]> avgTempoByDisciplinaIds(@Param("ids") List<Long> ids);

    @Query("SELECT c.banca.id, AVG(r.tempoRespostaSegundos) FROM Resposta r JOIN r.questao q JOIN q.concurso c WHERE c.banca.id IN :ids AND r.tempoRespostaSegundos IS NOT NULL GROUP BY c.banca.id")
    List<Object[]> avgTempoByBancaIds(@Param("ids") List<Long> ids);

    @Query("SELECT c.instituicao.id, AVG(r.tempoRespostaSegundos) FROM Resposta r JOIN r.questao q JOIN q.concurso c WHERE c.instituicao.id IN :ids AND r.tempoRespostaSegundos IS NOT NULL GROUP BY c.instituicao.id")
    List<Object[]> avgTempoByInstituicaoIds(@Param("ids") List<Long> ids);

    @Query("SELECT qc.concursoCargo.cargo.id, AVG(r.tempoRespostaSegundos) FROM Resposta r JOIN r.questao q JOIN q.questaoCargos qc WHERE qc.concursoCargo.cargo.id IN :ids AND r.tempoRespostaSegundos IS NOT NULL GROUP BY qc.concursoCargo.cargo.id")
    List<Object[]> avgTempoByCargoIds(@Param("ids") List<Long> ids);

    // --- Batch: ultimaQuestao ---
    @Query("SELECT s.id, MAX(r.createdAt) FROM Resposta r JOIN r.questao.subtemas s WHERE s.id IN :ids GROUP BY s.id")
    List<Object[]> findLatestResponseDatesBySubtemaIds(@Param("ids") List<Long> ids);

    @Query("SELECT s.tema.id, MAX(r.createdAt) FROM Resposta r JOIN r.questao.subtemas s WHERE s.tema.id IN :ids GROUP BY s.tema.id")
    List<Object[]> findLatestResponseDatesByTemaIds(@Param("ids") List<Long> ids);

    @Query("SELECT s.tema.disciplina.id, MAX(r.createdAt) FROM Resposta r JOIN r.questao.subtemas s WHERE s.tema.disciplina.id IN :ids GROUP BY s.tema.disciplina.id")
    List<Object[]> findLatestResponseDatesByDisciplinaIds(@Param("ids") List<Long> ids);

    @Query("SELECT c.banca.id, MAX(r.createdAt) FROM Resposta r JOIN r.questao q JOIN q.concurso c WHERE c.banca.id IN :ids GROUP BY c.banca.id")
    List<Object[]> findLatestResponseDatesByBancaIds(@Param("ids") List<Long> ids);

    @Query("SELECT c.instituicao.id, MAX(r.createdAt) FROM Resposta r JOIN r.questao q JOIN q.concurso c WHERE c.instituicao.id IN :ids GROUP BY c.instituicao.id")
    List<Object[]> findLatestResponseDatesByInstituicaoIds(@Param("ids") List<Long> ids);

    @Query("SELECT qc.concursoCargo.cargo.id, MAX(r.createdAt) FROM Resposta r JOIN r.questao q JOIN q.questaoCargos qc WHERE qc.concursoCargo.cargo.id IN :ids GROUP BY qc.concursoCargo.cargo.id")
    List<Object[]> findLatestResponseDatesByCargoIds(@Param("ids") List<Long> ids);

    // --- Batch: DificuldadeStats (Native queries) ---
    @Query(value = """
        SELECT s.id AS object_id,
               COALESCE(r.dificuldade_id, 2) AS diff_val,
               COUNT(r.id) AS total_ans,
               SUM(CASE WHEN a.correta = 1 THEN 1 ELSE 0 END) AS total_corr
        FROM (
            SELECT id, questao_id, alternativa_id, dificuldade_id,
                   ROW_NUMBER() OVER(PARTITION BY questao_id ORDER BY created_at DESC) as rn
            FROM resposta
        ) r
        JOIN questao q ON r.questao_id = q.id
        JOIN questao_subtema qs ON q.id = qs.questao_id
        JOIN subtema s ON qs.subtema_id = s.id
        JOIN alternativa a ON r.alternativa_id = a.id
        WHERE s.id IN (:ids) AND r.rn = 1
        GROUP BY s.id, COALESCE(r.dificuldade_id, 2)
    """, nativeQuery = true)
    List<Object[]> getDificuldadeStatsBySubtemaIds(@Param("ids") List<Long> ids);

    @Query(value = """
        SELECT t.id AS object_id,
               COALESCE(r.dificuldade_id, 2) AS diff_val,
               COUNT(r.id) AS total_ans,
               SUM(CASE WHEN a.correta = 1 THEN 1 ELSE 0 END) AS total_corr
        FROM (
            SELECT id, questao_id, alternativa_id, dificuldade_id,
                   ROW_NUMBER() OVER(PARTITION BY questao_id ORDER BY created_at DESC) as rn
            FROM resposta
        ) r
        JOIN questao q ON r.questao_id = q.id
        JOIN questao_subtema qs ON q.id = qs.questao_id
        JOIN subtema s ON qs.subtema_id = s.id
        JOIN tema t ON s.tema_id = t.id
        JOIN alternativa a ON r.alternativa_id = a.id
        WHERE t.id IN (:ids) AND r.rn = 1
        GROUP BY t.id, COALESCE(r.dificuldade_id, 2)
    """, nativeQuery = true)
    List<Object[]> getDificuldadeStatsByTemaIds(@Param("ids") List<Long> ids);

    @Query(value = """
        SELECT t.disciplina_id AS object_id,
               COALESCE(r.dificuldade_id, 2) AS diff_val,
               COUNT(r.id) AS total_ans,
               SUM(CASE WHEN a.correta = 1 THEN 1 ELSE 0 END) AS total_corr
        FROM (
            SELECT id, questao_id, alternativa_id, dificuldade_id,
                   ROW_NUMBER() OVER(PARTITION BY questao_id ORDER BY created_at DESC) as rn
            FROM resposta
        ) r
        JOIN questao q ON r.questao_id = q.id
        JOIN questao_subtema qs ON q.id = qs.questao_id
        JOIN subtema s ON qs.subtema_id = s.id
        JOIN tema t ON s.tema_id = t.id
        JOIN alternativa a ON r.alternativa_id = a.id
        WHERE t.disciplina_id IN (:ids) AND r.rn = 1
        GROUP BY t.disciplina_id, COALESCE(r.dificuldade_id, 2)
    """, nativeQuery = true)
    List<Object[]> getDificuldadeStatsByDisciplinaIds(@Param("ids") List<Long> ids);

    @Query(value = """
        SELECT c.banca_id AS object_id,
               COALESCE(r.dificuldade_id, 2) AS diff_val,
               COUNT(r.id) AS total_ans,
               SUM(CASE WHEN a.correta = 1 THEN 1 ELSE 0 END) AS total_corr
        FROM (
            SELECT id, questao_id, alternativa_id, dificuldade_id,
                   ROW_NUMBER() OVER(PARTITION BY questao_id ORDER BY created_at DESC) as rn
            FROM resposta
        ) r
        JOIN questao q ON r.questao_id = q.id
        JOIN concurso c ON q.concurso_id = c.id
        JOIN alternativa a ON r.alternativa_id = a.id
        WHERE c.banca_id IN (:ids) AND r.rn = 1
        GROUP BY c.banca_id, COALESCE(r.dificuldade_id, 2)
    """, nativeQuery = true)
    List<Object[]> getDificuldadeStatsByBancaIds(@Param("ids") List<Long> ids);

    @Query(value = """
        SELECT c.instituicao_id AS object_id,
               COALESCE(r.dificuldade_id, 2) AS diff_val,
               COUNT(r.id) AS total_ans,
               SUM(CASE WHEN a.correta = 1 THEN 1 ELSE 0 END) AS total_corr
        FROM (
            SELECT id, questao_id, alternativa_id, dificuldade_id,
                   ROW_NUMBER() OVER(PARTITION BY questao_id ORDER BY created_at DESC) as rn
            FROM resposta
        ) r
        JOIN questao q ON r.questao_id = q.id
        JOIN concurso c ON q.concurso_id = c.id
        JOIN alternativa a ON r.alternativa_id = a.id
        WHERE c.instituicao_id IN (:ids) AND r.rn = 1
        GROUP BY c.instituicao_id, COALESCE(r.dificuldade_id, 2)
    """, nativeQuery = true)
    List<Object[]> getDificuldadeStatsByInstituicaoIds(@Param("ids") List<Long> ids);

    @Query(value = """
        SELECT cc.cargo_id AS object_id,
               COALESCE(r.dificuldade_id, 2) AS diff_val,
               COUNT(r.id) AS total_ans,
               SUM(CASE WHEN a.correta = 1 THEN 1 ELSE 0 END) AS total_corr
        FROM (
            SELECT id, questao_id, alternativa_id, dificuldade_id,
                   ROW_NUMBER() OVER(PARTITION BY questao_id ORDER BY created_at DESC) as rn
            FROM resposta
        ) r
        JOIN questao q ON r.questao_id = q.id
        JOIN questao_cargo qc ON q.id = qc.questao_id
        JOIN concurso_cargo cc ON qc.concurso_cargo_id = cc.id
        JOIN alternativa a ON r.alternativa_id = a.id
        WHERE cc.cargo_id IN (:ids) AND r.rn = 1
        GROUP BY cc.cargo_id, COALESCE(r.dificuldade_id, 2)
    """, nativeQuery = true)
    List<Object[]> getDificuldadeStatsByCargoIds(@Param("ids") List<Long> ids);

    // --- Granular breakdown queries for Banca ---
    @Query("SELECT CAST(cc.cargo.nivel AS string), COUNT(DISTINCT r.questao.id), SUM(CASE WHEN a.correta = true THEN 1 ELSE 0 END) FROM Resposta r JOIN r.questao q JOIN r.alternativaEscolhida a JOIN q.concurso c JOIN c.concursoCargos cc WHERE c.banca.id = :bancaId AND q.anulada = false GROUP BY cc.cargo.nivel")
    List<Object[]> countRespondidasAcertadasByBancaIdGroupByNivel(@Param("bancaId") Long bancaId);

    @Query("SELECT c.instituicao.area, COUNT(DISTINCT r.questao.id), SUM(CASE WHEN a.correta = true THEN 1 ELSE 0 END) FROM Resposta r JOIN r.questao q JOIN r.alternativaEscolhida a JOIN q.concurso c WHERE c.banca.id = :bancaId AND q.anulada = false GROUP BY c.instituicao.area")
    List<Object[]> countRespondidasAcertadasByBancaIdGroupByAreaInstituicao(@Param("bancaId") Long bancaId);

    @Query("SELECT cc.cargo.area, COUNT(DISTINCT r.questao.id), SUM(CASE WHEN a.correta = true THEN 1 ELSE 0 END) FROM Resposta r JOIN r.questao q JOIN r.alternativaEscolhida a JOIN q.concurso c JOIN c.concursoCargos cc WHERE c.banca.id = :bancaId AND q.anulada = false GROUP BY cc.cargo.area")
    List<Object[]> countRespondidasAcertadasByBancaIdGroupByAreaCargo(@Param("bancaId") Long bancaId);

    @Query("SELECT CAST(cc.cargo.nivel AS string), AVG(r.tempoRespostaSegundos) FROM Resposta r JOIN r.questao q JOIN q.concurso c JOIN c.concursoCargos cc WHERE c.banca.id = :bancaId AND r.tempoRespostaSegundos IS NOT NULL GROUP BY cc.cargo.nivel")
    List<Object[]> avgTempoByBancaIdGroupByNivel(@Param("bancaId") Long bancaId);

    @Query("SELECT c.instituicao.area, AVG(r.tempoRespostaSegundos) FROM Resposta r JOIN r.questao q JOIN q.concurso c WHERE c.banca.id = :bancaId AND r.tempoRespostaSegundos IS NOT NULL GROUP BY c.instituicao.area")
    List<Object[]> avgTempoByBancaIdGroupByAreaInstituicao(@Param("bancaId") Long bancaId);

    @Query("SELECT cc.cargo.area, AVG(r.tempoRespostaSegundos) FROM Resposta r JOIN r.questao q JOIN q.concurso c JOIN c.concursoCargos cc WHERE c.banca.id = :bancaId AND r.tempoRespostaSegundos IS NOT NULL GROUP BY cc.cargo.area")
    List<Object[]> avgTempoByBancaIdGroupByAreaCargo(@Param("bancaId") Long bancaId);

    // --- Granular breakdown queries for Instituicao ---
    @Query("SELECT CAST(cc.cargo.nivel AS string), COUNT(DISTINCT r.questao.id), SUM(CASE WHEN a.correta = true THEN 1 ELSE 0 END) FROM Resposta r JOIN r.questao q JOIN r.alternativaEscolhida a JOIN q.concurso c JOIN c.concursoCargos cc WHERE c.instituicao.id = :instituicaoId AND q.anulada = false GROUP BY cc.cargo.nivel")
    List<Object[]> countRespondidasAcertadasByInstituicaoIdGroupByNivel(@Param("instituicaoId") Long instituicaoId);

    @Query("SELECT c.banca.id, COUNT(DISTINCT r.questao.id), SUM(CASE WHEN a.correta = true THEN 1 ELSE 0 END) FROM Resposta r JOIN r.questao q JOIN r.alternativaEscolhida a JOIN q.concurso c WHERE c.instituicao.id = :instituicaoId AND q.anulada = false GROUP BY c.banca.id")
    List<Object[]> countRespondidasAcertadasByInstituicaoIdGroupByBanca(@Param("instituicaoId") Long instituicaoId);

    @Query("SELECT cc.cargo.id, COUNT(DISTINCT r.questao.id), SUM(CASE WHEN a.correta = true THEN 1 ELSE 0 END) FROM Resposta r JOIN r.questao q JOIN r.alternativaEscolhida a JOIN q.concurso c JOIN c.concursoCargos cc WHERE c.instituicao.id = :instituicaoId AND q.anulada = false GROUP BY cc.cargo.id")
    List<Object[]> countRespondidasAcertadasByInstituicaoIdGroupByCargo(@Param("instituicaoId") Long instituicaoId);

    @Query("SELECT cc.cargo.area, COUNT(DISTINCT r.questao.id), SUM(CASE WHEN a.correta = true THEN 1 ELSE 0 END) FROM Resposta r JOIN r.questao q JOIN r.alternativaEscolhida a JOIN q.concurso c JOIN c.concursoCargos cc WHERE c.instituicao.id = :instituicaoId AND q.anulada = false GROUP BY cc.cargo.area")
    List<Object[]> countRespondidasAcertadasByInstituicaoIdGroupByAreaCargo(@Param("instituicaoId") Long instituicaoId);

    @Query("SELECT CAST(cc.cargo.nivel AS string), AVG(r.tempoRespostaSegundos) FROM Resposta r JOIN r.questao q JOIN q.concurso c JOIN c.concursoCargos cc WHERE c.instituicao.id = :instituicaoId AND r.tempoRespostaSegundos IS NOT NULL GROUP BY cc.cargo.nivel")
    List<Object[]> avgTempoByInstituicaoIdGroupByNivel(@Param("instituicaoId") Long instituicaoId);

    @Query("SELECT c.banca.id, AVG(r.tempoRespostaSegundos) FROM Resposta r JOIN r.questao q JOIN q.concurso c WHERE c.instituicao.id = :instituicaoId AND r.tempoRespostaSegundos IS NOT NULL GROUP BY c.banca.id")
    List<Object[]> avgTempoByInstituicaoIdGroupByBanca(@Param("instituicaoId") Long instituicaoId);

    @Query("SELECT cc.cargo.id, AVG(r.tempoRespostaSegundos) FROM Resposta r JOIN r.questao q JOIN q.concurso c JOIN c.concursoCargos cc WHERE c.instituicao.id = :instituicaoId AND r.tempoRespostaSegundos IS NOT NULL GROUP BY cc.cargo.id")
    List<Object[]> avgTempoByInstituicaoIdGroupByCargo(@Param("instituicaoId") Long instituicaoId);

    @Query("SELECT cc.cargo.area, AVG(r.tempoRespostaSegundos) FROM Resposta r JOIN r.questao q JOIN q.concurso c JOIN c.concursoCargos cc WHERE c.instituicao.id = :instituicaoId AND r.tempoRespostaSegundos IS NOT NULL GROUP BY cc.cargo.area")
    List<Object[]> avgTempoByInstituicaoIdGroupByAreaCargo(@Param("instituicaoId") Long instituicaoId);

    // --- Granular breakdown queries for Cargo ---
    @Query("SELECT CAST(qc.concursoCargo.cargo.nivel AS string), COUNT(DISTINCT r.questao.id), SUM(CASE WHEN a.correta = true THEN 1 ELSE 0 END) FROM Resposta r JOIN r.questao q JOIN r.alternativaEscolhida a JOIN q.questaoCargos qc WHERE qc.concursoCargo.cargo.id = :cargoId AND q.anulada = false GROUP BY qc.concursoCargo.cargo.nivel")
    List<Object[]> countRespondidasAcertadasByCargoIdGroupByNivel(@Param("cargoId") Long cargoId);

    @Query("SELECT c.banca.id, COUNT(DISTINCT r.questao.id), SUM(CASE WHEN a.correta = true THEN 1 ELSE 0 END) FROM Resposta r JOIN r.questao q JOIN r.alternativaEscolhida a JOIN q.questaoCargos qc JOIN q.concurso c WHERE qc.concursoCargo.cargo.id = :cargoId AND q.anulada = false GROUP BY c.banca.id")
    List<Object[]> countRespondidasAcertadasByCargoIdGroupByBanca(@Param("cargoId") Long cargoId);

    @Query("SELECT qc.concursoCargo.cargo.area, COUNT(DISTINCT r.questao.id), SUM(CASE WHEN a.correta = true THEN 1 ELSE 0 END) FROM Resposta r JOIN r.questao q JOIN r.alternativaEscolhida a JOIN q.questaoCargos qc WHERE qc.concursoCargo.cargo.id = :cargoId AND q.anulada = false GROUP BY qc.concursoCargo.cargo.area")
    List<Object[]> countRespondidasAcertadasByCargoIdGroupByAreaCargo(@Param("cargoId") Long cargoId);

    @Query("SELECT c.instituicao.area, COUNT(DISTINCT r.questao.id), SUM(CASE WHEN a.correta = true THEN 1 ELSE 0 END) FROM Resposta r JOIN r.questao q JOIN r.alternativaEscolhida a JOIN q.questaoCargos qc JOIN q.concurso c WHERE qc.concursoCargo.cargo.id = :cargoId AND q.anulada = false GROUP BY c.instituicao.area")
    List<Object[]> countRespondidasAcertadasByCargoIdGroupByAreaInstituicao(@Param("cargoId") Long cargoId);

    @Query("SELECT CAST(qc.concursoCargo.cargo.nivel AS string), AVG(r.tempoRespostaSegundos) FROM Resposta r JOIN r.questao q JOIN q.questaoCargos qc WHERE qc.concursoCargo.cargo.id = :cargoId AND r.tempoRespostaSegundos IS NOT NULL GROUP BY qc.concursoCargo.cargo.nivel")
    List<Object[]> avgTempoByCargoIdGroupByNivel(@Param("cargoId") Long cargoId);

    @Query("SELECT c.banca.id, AVG(r.tempoRespostaSegundos) FROM Resposta r JOIN r.questao q JOIN q.questaoCargos qc JOIN q.concurso c WHERE qc.concursoCargo.cargo.id = :cargoId AND r.tempoRespostaSegundos IS NOT NULL GROUP BY c.banca.id")
    List<Object[]> avgTempoByCargoIdGroupByBanca(@Param("cargoId") Long cargoId);

    @Query("SELECT qc.concursoCargo.cargo.area, AVG(r.tempoRespostaSegundos) FROM Resposta r JOIN r.questao q JOIN q.questaoCargos qc WHERE qc.concursoCargo.cargo.id = :cargoId AND r.tempoRespostaSegundos IS NOT NULL GROUP BY qc.concursoCargo.cargo.area")
    List<Object[]> avgTempoByCargoIdGroupByAreaCargo(@Param("cargoId") Long cargoId);

    @Query("SELECT c.instituicao.area, AVG(r.tempoRespostaSegundos) FROM Resposta r JOIN r.questao q JOIN q.questaoCargos qc JOIN q.concurso c WHERE qc.concursoCargo.cargo.id = :cargoId AND r.tempoRespostaSegundos IS NOT NULL GROUP BY c.instituicao.area")
    List<Object[]> avgTempoByCargoIdGroupByAreaInstituicao(@Param("cargoId") Long cargoId);
}
