package com.studora.repository;

import com.studora.entity.Cargo;
import com.studora.entity.NivelCargo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface CargoRepository extends JpaRepository<Cargo, Long> {
    @Query("SELECT c FROM Cargo c WHERE UPPER(c.nome) = UPPER(:nome) AND c.nivel = :nivel AND UPPER(c.area) = UPPER(:area)")
    Optional<Cargo> findByNomeAndNivelAndArea(@Param("nome") String nome, @Param("nivel") NivelCargo nivel, @Param("area") String area);

    org.springframework.data.domain.Page<Cargo> findByNomeContainingIgnoreCase(String nome, org.springframework.data.domain.Pageable pageable);

    @Query("SELECT DISTINCT c.area FROM Cargo c WHERE c.area IS NOT NULL")
    List<String> findDistinctAreas();

    @Query("SELECT DISTINCT c.area FROM Cargo c WHERE c.area IS NOT NULL AND LOWER(c.area) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<String> findDistinctAreas(@Param("search") String search);
}
