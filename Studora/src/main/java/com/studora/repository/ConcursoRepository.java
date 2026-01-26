package com.studora.repository;

import com.studora.entity.Concurso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConcursoRepository extends JpaRepository<Concurso, Long> {
    // Custom queries can be added here if needed
}