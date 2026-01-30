package com.studora.repository;

import com.studora.entity.ConcursoCargo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ConcursoCargoRepository extends JpaRepository<ConcursoCargo, Long> {

    List<ConcursoCargo> findByConcursoId(Long concursoId);

    @Query("SELECT cc FROM ConcursoCargo cc WHERE cc.concurso.id = :concursoId AND cc.cargo.id = :cargoId")
    List<ConcursoCargo> findByConcursoIdAndCargoId(@Param("concursoId") Long concursoId, @Param("cargoId") Long cargoId);
}
