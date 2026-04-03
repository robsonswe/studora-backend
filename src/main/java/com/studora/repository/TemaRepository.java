package com.studora.repository;

import com.studora.entity.Tema;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TemaRepository extends JpaRepository<Tema, Long> {
    Page<Tema> findByDisciplinaId(Long disciplinaId, Pageable pageable);
    List<Tema> findByDisciplinaId(Long disciplinaId);

    Optional<Tema> findByDisciplinaIdAndNome(Long disciplinaId, String nome);

    Optional<Tema> findByDisciplinaIdAndNomeIgnoreCase(Long disciplinaId, String nome);

    boolean existsByDisciplinaId(Long disciplinaId);

    @Query("SELECT t FROM Tema t WHERE t.disciplina.id = :disciplinaId AND t.nome = :nome AND t.id != :id")
    Optional<Tema> findByDisciplinaIdAndNomeAndIdNot(@Param("disciplinaId") Long disciplinaId, @Param("nome") String nome, @Param("id") Long id);

    @Query("SELECT t FROM Tema t WHERE t.disciplina.id = :disciplinaId AND UPPER(t.nome) = UPPER(:nome) AND t.id != :id")
    Optional<Tema> findByDisciplinaIdAndNomeIgnoreCaseAndIdNot(@Param("disciplinaId") Long disciplinaId, @Param("nome") String nome, @Param("id") Long id);

    @Query("SELECT t FROM Tema t " +
           "JOIN FETCH t.disciplina " +
           "WHERE t.id = :id")
    Optional<Tema> findByIdWithDetails(@Param("id") Long id);

    @Query(value = "SELECT t FROM Tema t JOIN FETCH t.disciplina WHERE UPPER(t.nome) LIKE UPPER(CONCAT('%', :nome, '%'))",
           countQuery = "SELECT count(t) FROM Tema t WHERE UPPER(t.nome) LIKE UPPER(CONCAT('%', :nome, '%'))")
    Page<Tema> findByNomeContainingIgnoreCase(@Param("nome") String nome, Pageable pageable);

    // --- Batch queries ---
    @org.springframework.data.jpa.repository.Query("SELECT t.disciplina.id, COUNT(t) FROM Tema t WHERE t.disciplina.id IN :disciplinaIds GROUP BY t.disciplina.id")
    java.util.List<Object[]> countByDisciplinaIds(@org.springframework.data.repository.query.Param("disciplinaIds") List<Long> disciplinaIds);

    @org.springframework.data.jpa.repository.Query("SELECT t FROM Tema t WHERE t.disciplina.id IN :disciplinaIds")
    java.util.List<Tema> findByDisciplinaIds(@org.springframework.data.repository.query.Param("disciplinaIds") List<Long> disciplinaIds);

    @Query(value = "SELECT t FROM Tema t JOIN FETCH t.disciplina",
           countQuery = "SELECT count(t) FROM Tema t")
    Page<Tema> findAll(Pageable pageable);

    @Query(value = "SELECT t FROM Tema t JOIN FETCH t.disciplina WHERE t.id IN (:ids)")
    java.util.List<Tema> findAllByIdWithDisciplina(@Param("ids") List<Long> ids);

    @Query(value = """
        SELECT t.disciplina_id, COUNT(t.id)
        FROM tema t
        WHERE t.disciplina_id IN (:ids)
          AND (SELECT COUNT(s.id) FROM subtema s WHERE s.tema_id = t.id) > 0
          AND (SELECT COUNT(s.id) FROM subtema s WHERE s.tema_id = t.id)
              = (SELECT COUNT(DISTINCT e.subtema_id) FROM estudo_subtema e
                 JOIN subtema s ON e.subtema_id = s.id WHERE s.tema_id = t.id)
        GROUP BY t.disciplina_id
    """, nativeQuery = true)
    List<Object[]> countTemasEstudadosByDisciplinaIds(@Param("ids") List<Long> ids);
}
