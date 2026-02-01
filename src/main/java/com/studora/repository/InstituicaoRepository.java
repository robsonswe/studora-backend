package com.studora.repository;

import com.studora.entity.Instituicao;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InstituicaoRepository extends JpaRepository<Instituicao, Long> {
    Optional<Instituicao> findByNome(String nome);
    Optional<Instituicao> findByNomeIgnoreCase(String nome);
    org.springframework.data.domain.Page<Instituicao> findByNomeContainingIgnoreCase(String nome, org.springframework.data.domain.Pageable pageable);

    @Query("SELECT DISTINCT i.area FROM Instituicao i WHERE i.area IS NOT NULL")
    java.util.List<String> findDistinctAreas();

    @Query("SELECT DISTINCT i.area FROM Instituicao i WHERE i.area IS NOT NULL AND LOWER(i.area) LIKE LOWER(CONCAT('%', :search, '%'))")
    java.util.List<String> findDistinctAreas(@Param("search") String search);
}
