package com.studora.repository;

import com.studora.entity.ConcursoCargoSubtema;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConcursoCargoSubtemaRepository extends JpaRepository<ConcursoCargoSubtema, Long> {
}
