package com.studora.repository;

import com.studora.entity.Subtema;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubtemaRepository extends JpaRepository<Subtema, Long>, JpaSpecificationExecutor<Subtema> {
    Page<Subtema> findByTemaId(Long temaId, Pageable pageable);
    List<Subtema> findByTemaId(Long temaId);

    Optional<Subtema> findByTemaIdAndNome(Long temaId, String nome);

    Optional<Subtema> findByTemaIdAndNomeIgnoreCase(Long temaId, String nome);

    boolean existsByTemaId(Long temaId);

    @Query("SELECT s FROM Subtema s WHERE s.tema.id = :temaId AND s.nome = :nome AND s.id != :id")
    Optional<Subtema> findByTemaIdAndNomeAndIdNot(@Param("temaId") Long temaId, @Param("nome") String nome, @Param("id") Long id);

    @Query("SELECT s FROM Subtema s WHERE s.tema.id = :temaId AND UPPER(s.nome) = UPPER(:nome) AND s.id != :id")
    Optional<Subtema> findByTemaIdAndNomeIgnoreCaseAndIdNot(@Param("temaId") Long temaId, @Param("nome") String nome, @Param("id") Long id);

    @Query("SELECT s FROM Subtema s " +
           "JOIN FETCH s.tema t " +
           "JOIN FETCH t.disciplina " +
           "WHERE s.id = :id")
    Optional<Subtema> findByIdWithDetails(@Param("id") Long id);

    @Query(value = "SELECT s FROM Subtema s JOIN FETCH s.tema t JOIN FETCH t.disciplina WHERE UPPER(s.nome) LIKE UPPER(CONCAT('%', :nome, '%'))",
           countQuery = "SELECT count(s) FROM Subtema s WHERE UPPER(s.nome) LIKE UPPER(CONCAT('%', :nome, '%'))")
    Page<Subtema> findByNomeContainingIgnoreCase(@Param("nome") String nome, Pageable pageable);

    // --- Batch queries ---
    @org.springframework.data.jpa.repository.Query("SELECT s.tema.id, COUNT(s) FROM Subtema s WHERE s.tema.id IN :temaIds GROUP BY s.tema.id")
    java.util.List<Object[]> countByTemaIds(@org.springframework.data.repository.query.Param("temaIds") List<Long> temaIds);

    @org.springframework.data.jpa.repository.Query("SELECT s.tema.disciplina.id, COUNT(s) FROM Subtema s WHERE s.tema.disciplina.id IN :disciplinaIds GROUP BY s.tema.disciplina.id")
    java.util.List<Object[]> countByDisciplinaIds(@org.springframework.data.repository.query.Param("disciplinaIds") List<Long> disciplinaIds);

    @Query(value = "SELECT s FROM Subtema s JOIN FETCH s.tema t JOIN FETCH t.disciplina WHERE s.id IN (:ids)")
    java.util.List<Subtema> findAllByIdWithTemaAndDisciplina(@Param("ids") List<Long> ids);

    @Query(value = "SELECT s FROM Subtema s JOIN FETCH s.tema t JOIN FETCH t.disciplina",
           countQuery = "SELECT count(s) FROM Subtema s")
    Page<Subtema> findAll(Pageable pageable);
}
