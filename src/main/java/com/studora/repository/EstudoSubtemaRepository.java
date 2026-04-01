package com.studora.repository;

import com.studora.entity.EstudoSubtema;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EstudoSubtemaRepository extends JpaRepository<EstudoSubtema, Long> {

    List<EstudoSubtema> findBySubtemaIdOrderByCreatedAtDesc(Long subtemaId);

    long countBySubtemaId(Long subtemaId);

    Optional<EstudoSubtema> findFirstBySubtemaIdOrderByCreatedAtDesc(Long subtemaId);

    @org.springframework.data.jpa.repository.Query("SELECT e.subtema.id, COUNT(e) FROM EstudoSubtema e WHERE e.subtema.id IN :subtemaIds GROUP BY e.subtema.id")
    List<Object[]> countBySubtemaIds(@org.springframework.data.repository.query.Param("subtemaIds") List<Long> subtemaIds);

    @org.springframework.data.jpa.repository.Query("SELECT e.subtema.id, MAX(e.createdAt) FROM EstudoSubtema e WHERE e.subtema.id IN :subtemaIds GROUP BY e.subtema.id")
    List<Object[]> findLatestStudyDatesBySubtemaIds(@org.springframework.data.repository.query.Param("subtemaIds") List<Long> subtemaIds);

    // --- Tema-level batch queries ---
    @org.springframework.data.jpa.repository.Query("SELECT e.subtema.tema.id, COUNT(e) FROM EstudoSubtema e WHERE e.subtema.tema.id IN :temaIds GROUP BY e.subtema.tema.id")
    List<Object[]> countByTemaIds(@org.springframework.data.repository.query.Param("temaIds") List<Long> temaIds);

    @org.springframework.data.jpa.repository.Query("SELECT e.subtema.tema.id, MAX(e.createdAt) FROM EstudoSubtema e WHERE e.subtema.tema.id IN :temaIds GROUP BY e.subtema.tema.id")
    List<Object[]> findLatestStudyDatesByTemaIds(@org.springframework.data.repository.query.Param("temaIds") List<Long> temaIds);

    @org.springframework.data.jpa.repository.Query("SELECT e.subtema.tema.id, COUNT(DISTINCT e.subtema.id) FROM EstudoSubtema e WHERE e.subtema.tema.id IN :temaIds GROUP BY e.subtema.tema.id")
    List<Object[]> countDistinctStudiedSubtemasByTemaIds(@org.springframework.data.repository.query.Param("temaIds") List<Long> temaIds);

    // --- Disciplina-level batch queries ---
    @org.springframework.data.jpa.repository.Query("SELECT e.subtema.tema.disciplina.id, COUNT(e) FROM EstudoSubtema e WHERE e.subtema.tema.disciplina.id IN :disciplinaIds GROUP BY e.subtema.tema.disciplina.id")
    List<Object[]> countByDisciplinaIds(@org.springframework.data.repository.query.Param("disciplinaIds") List<Long> disciplinaIds);

    @org.springframework.data.jpa.repository.Query("SELECT e.subtema.tema.disciplina.id, MAX(e.createdAt) FROM EstudoSubtema e WHERE e.subtema.tema.disciplina.id IN :disciplinaIds GROUP BY e.subtema.tema.disciplina.id")
    List<Object[]> findLatestStudyDatesByDisciplinaIds(@org.springframework.data.repository.query.Param("disciplinaIds") List<Long> disciplinaIds);

    @org.springframework.data.jpa.repository.Query("SELECT e.subtema.tema.disciplina.id, COUNT(DISTINCT e.subtema.id) FROM EstudoSubtema e WHERE e.subtema.tema.disciplina.id IN :disciplinaIds GROUP BY e.subtema.tema.disciplina.id")
    List<Object[]> countDistinctStudiedSubtemasByDisciplinaIds(@org.springframework.data.repository.query.Param("disciplinaIds") List<Long> disciplinaIds);

    @org.springframework.data.jpa.repository.Query("SELECT e.subtema.tema.disciplina.id, COUNT(DISTINCT e.subtema.tema.id) FROM EstudoSubtema e WHERE e.subtema.tema.disciplina.id IN :disciplinaIds GROUP BY e.subtema.tema.disciplina.id")
    List<Object[]> countDistinctStudiedTemasByDisciplinaIds(@org.springframework.data.repository.query.Param("disciplinaIds") List<Long> disciplinaIds);
}
