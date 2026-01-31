package com.studora.repository;

import com.studora.entity.Subtema;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubtemaRepository extends JpaRepository<Subtema, Long> {
    List<Subtema> findByTemaId(Long temaId);

    Optional<Subtema> findByTemaIdAndNome(Long temaId, String nome);

    boolean existsByTemaId(Long temaId);

    @Query("SELECT s FROM Subtema s WHERE s.tema.id = :temaId AND s.nome = :nome AND s.id != :id")
    Optional<Subtema> findByTemaIdAndNomeAndIdNot(@Param("temaId") Long temaId, @Param("nome") String nome, @Param("id") Long id);
}