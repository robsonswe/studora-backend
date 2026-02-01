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

    Page<Tema> findByNomeContainingIgnoreCase(String nome, Pageable pageable);
}