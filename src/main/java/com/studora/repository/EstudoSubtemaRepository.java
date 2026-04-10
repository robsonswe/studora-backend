package com.studora.repository;

import com.studora.entity.EstudoSubtema;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EstudoSubtemaRepository extends JpaRepository<EstudoSubtema, Long> {

    List<EstudoSubtema> findBySubtemaIdOrderByCreatedAtDesc(Long subtemaId);

    long countBySubtemaId(Long subtemaId);

    Optional<EstudoSubtema> findFirstBySubtemaIdOrderByCreatedAtDesc(Long subtemaId);

    @Query("SELECT e.subtema.id, COUNT(e) FROM EstudoSubtema e WHERE e.subtema.id IN :subtemaIds GROUP BY e.subtema.id")
    List<Object[]> countBySubtemaIds(@Param("subtemaIds") List<Long> subtemaIds);

    @Query("SELECT e.subtema.id, MAX(e.createdAt) FROM EstudoSubtema e WHERE e.subtema.id IN :subtemaIds GROUP BY e.subtema.id")
    List<Object[]> findLatestStudyDatesBySubtemaIds(@Param("subtemaIds") List<Long> subtemaIds);

    // --- Tema-level queries ---
    @Query("SELECT COUNT(e) FROM EstudoSubtema e WHERE e.subtema.tema.id = :temaId")
    long countByTemaId(@Param("temaId") Long temaId);

    @Query("SELECT MAX(e.createdAt) FROM EstudoSubtema e WHERE e.subtema.tema.id = :temaId")
    LocalDateTime findLatestStudyDateByTemaId(@Param("temaId") Long temaId);

    @Query("SELECT COUNT(DISTINCT e.subtema.id) FROM EstudoSubtema e WHERE e.subtema.tema.id = :temaId")
    long countDistinctStudiedSubtemasByTemaId(@Param("temaId") Long temaId);

    // --- Tema-level batch queries ---
    @Query("SELECT e.subtema.tema.id, COUNT(e) FROM EstudoSubtema e WHERE e.subtema.tema.id IN :temaIds GROUP BY e.subtema.tema.id")
    List<Object[]> countByTemaIds(@Param("temaIds") List<Long> temaIds);

    @Query("SELECT e.subtema.tema.id, MAX(e.createdAt) FROM EstudoSubtema e WHERE e.subtema.tema.id IN :temaIds GROUP BY e.subtema.tema.id")
    List<Object[]> findLatestStudyDatesByTemaIds(@Param("temaIds") List<Long> temaIds);

    @Query("SELECT e.subtema.tema.id, COUNT(DISTINCT e.subtema.id) FROM EstudoSubtema e WHERE e.subtema.tema.id IN :temaIds GROUP BY e.subtema.tema.id")
    List<Object[]> countDistinctStudiedSubtemasByTemaIds(@Param("temaIds") List<Long> temaIds);

    // --- Disciplina-level queries ---
    @Query("SELECT COUNT(e) FROM EstudoSubtema e WHERE e.subtema.tema.disciplina.id = :disciplinaId")
    long countByDisciplinaId(@Param("disciplinaId") Long disciplinaId);

    @Query("SELECT MAX(e.createdAt) FROM EstudoSubtema e WHERE e.subtema.tema.disciplina.id = :disciplinaId")
    LocalDateTime findLatestStudyDateByDisciplinaId(@Param("disciplinaId") Long disciplinaId);

    @Query("SELECT COUNT(DISTINCT e.subtema.id) FROM EstudoSubtema e WHERE e.subtema.tema.disciplina.id = :disciplinaId")
    long countDistinctStudiedSubtemasByDisciplinaId(@Param("disciplinaId") Long disciplinaId);

    // --- Disciplina-level batch queries ---
    @Query("SELECT e.subtema.tema.disciplina.id, COUNT(e) FROM EstudoSubtema e WHERE e.subtema.tema.disciplina.id IN :disciplinaIds GROUP BY e.subtema.tema.disciplina.id")
    List<Object[]> countByDisciplinaIds(@Param("disciplinaIds") List<Long> disciplinaIds);

    @Query("SELECT e.subtema.tema.disciplina.id, MAX(e.createdAt) FROM EstudoSubtema e WHERE e.subtema.tema.disciplina.id IN :disciplinaIds GROUP BY e.subtema.tema.disciplina.id")
    List<Object[]> findLatestStudyDatesByDisciplinaIds(@Param("disciplinaIds") List<Long> disciplinaIds);

    @Query("SELECT e.subtema.tema.disciplina.id, COUNT(DISTINCT e.subtema.id) FROM EstudoSubtema e WHERE e.subtema.tema.disciplina.id IN :disciplinaIds GROUP BY e.subtema.tema.disciplina.id")
    List<Object[]> countDistinctStudiedSubtemasByDisciplinaIds(@Param("disciplinaIds") List<Long> disciplinaIds);

    @Query("SELECT e.subtema.tema.disciplina.id, COUNT(DISTINCT e.subtema.tema.id) FROM EstudoSubtema e WHERE e.subtema.tema.disciplina.id IN :disciplinaIds GROUP BY e.subtema.tema.disciplina.id")
    List<Object[]> countDistinctStudiedTemasByDisciplinaIds(@Param("disciplinaIds") List<Long> disciplinaIds);
}
