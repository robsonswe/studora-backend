package com.studora.repository;

import com.studora.entity.Cargo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CargoRepository extends JpaRepository<Cargo, Long> {
    Optional<Cargo> findByNomeAndNivelAndArea(String nome, String nivel, String area);
}
