package com.studora.repository;

import com.studora.entity.Cargo;
import com.studora.entity.NivelCargo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CargoRepository extends JpaRepository<Cargo, Long> {
    @Query("SELECT c FROM Cargo c WHERE LOWER(c.nome) = LOWER(:nome) AND c.nivel = :nivel AND LOWER(c.area) = LOWER(:area)")
    Optional<Cargo> findByNomeAndNivelAndArea(@Param("nome") String nome, @Param("nivel") NivelCargo nivel, @Param("area") String area);
}
