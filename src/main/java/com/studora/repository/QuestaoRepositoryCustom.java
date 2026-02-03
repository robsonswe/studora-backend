package com.studora.repository;

import com.studora.dto.request.SimuladoGenerationRequest;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface QuestaoRepositoryCustom {
    List<Long> findIdsBySubtemaWithPreferences(Long subtemaId, SimuladoGenerationRequest request, List<Long> excludeIds, Pageable pageable);
    
    List<Long> findIdsByTemaWithPreferences(Long temaId, List<Long> avoidSubtemaIds, SimuladoGenerationRequest request, List<Long> excludeIds, Pageable pageable);
    
    List<Long> findIdsByDisciplinaWithPreferences(Long disciplinaId, List<Long> avoidTemaIds, List<Long> avoidSubtemaIds, SimuladoGenerationRequest request, List<Long> excludeIds, Pageable pageable);
}
