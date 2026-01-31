package com.studora.repository;

import com.studora.entity.Tema;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TemaRepository extends JpaRepository<Tema, Long> {
    List<Tema> findByDisciplinaId(Long disciplinaId);

    Optional<Tema> findByDisciplinaIdAndNome(Long disciplinaId, String nome);

    boolean existsByDisciplinaId(Long disciplinaId);

    @Query("SELECT t FROM Tema t WHERE t.disciplina.id = :disciplinaId AND t.nome = :nome AND t.id != :id")
    Optional<Tema> findByDisciplinaIdAndNomeAndIdNot(@Param("disciplinaId") Long disciplinaId, @Param("nome") String nome, @Param("id") Long id);
}