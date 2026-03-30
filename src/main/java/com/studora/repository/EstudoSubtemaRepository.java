package com.studora.repository;

import com.studora.entity.EstudoSubtema;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EstudoSubtemaRepository extends JpaRepository<EstudoSubtema, Long> {
    
    List<EstudoSubtema> findBySubtemaIdOrderByCreatedAtDesc(Long subtemaId);
    
    long countBySubtemaId(Long subtemaId);
    
    Optional<EstudoSubtema> findFirstBySubtemaIdOrderByCreatedAtDesc(Long subtemaId);

    @org.springframework.data.jpa.repository.Query("SELECT e.subtema.id, COUNT(e) FROM EstudoSubtema e WHERE e.subtema.id IN :subtemaIds GROUP BY e.subtema.id")
    List<Object[]> countBySubtemaIds(@org.springframework.data.repository.query.Param("subtemaIds") List<Long> subtemaIds);

    @org.springframework.data.jpa.repository.Query("SELECT e.subtema.id, MAX(e.createdAt) FROM EstudoSubtema e WHERE e.subtema.id IN :subtemaIds GROUP BY e.subtema.id")
    List<Object[]> findLatestStudyDatesBySubtemaIds(@org.springframework.data.repository.query.Param("subtemaIds") List<Long> subtemaIds);
}
