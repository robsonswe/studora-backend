package com.studora.repository;

import com.studora.entity.Instituicao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InstituicaoRepository extends JpaRepository<Instituicao, Long> {
    Optional<Instituicao> findByNome(String nome);
    Optional<Instituicao> findByNomeIgnoreCase(String nome);
    Page<Instituicao> findByNomeContainingIgnoreCase(String nome, Pageable pageable);
    Page<Instituicao> findByNomeNormalizedContaining(String nomeNormalized, Pageable pageable);

    @Query("SELECT DISTINCT i.area FROM Instituicao i WHERE i.area IS NOT NULL")
    List<String> findDistinctAreas();

    @Query("SELECT DISTINCT i.area FROM Instituicao i WHERE i.area IS NOT NULL AND i.areaNormalized LIKE CONCAT('%', :search, '%')")
    List<String> findDistinctAreas(@Param("search") String search);
}
