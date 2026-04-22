package com.studora.service;

import com.studora.dto.MetricsLevel;
import com.studora.dto.QuestaoStatsDto;
import com.studora.dto.banca.BancaDetailDto;
import com.studora.dto.banca.BancaSummaryDto;
import com.studora.dto.request.BancaCreateRequest;
import com.studora.dto.request.BancaUpdateRequest;
import com.studora.entity.Banca;
import com.studora.exception.ConflictException;
import com.studora.exception.ResourceNotFoundException;
import com.studora.mapper.BancaMapper;
import com.studora.repository.BancaRepository;
import com.studora.repository.ConcursoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class BancaService {

    private final BancaRepository bancaRepository;
    private final BancaMapper bancaMapper;
    private final ConcursoRepository concursoRepository;
    private final StatsAssembler statsAssembler;

    @Cacheable(value = "banca-stats", key = "T(java.util.Objects).hash(#nome, #pageable.pageNumber, #pageable.pageSize, #pageable.sort.toString(), #metrics)")
    @Transactional(readOnly = true)
    public Page<BancaSummaryDto> findAll(String nome, Pageable pageable, MetricsLevel metrics) {
        Page<Banca> page;
        if (nome != null && !nome.isBlank()) {
            String normalized = com.studora.util.StringUtils.normalizeForSearch(nome);
            page = bancaRepository.findByNomeNormalizedContaining(normalized, pageable);
        } else {
            page = bancaRepository.findAll(pageable);
        }

        return page.map(banca -> {
            BancaSummaryDto dto = bancaMapper.toSummaryDto(banca);
            if (metrics != null) {
                dto.setQuestaoStats(statsAssembler.buildStats(banca.getId(), "BANCA", metrics));
            }
            return dto;
        });
    }

    @Transactional(readOnly = true)
    public BancaDetailDto getBancaDetailById(Long id, MetricsLevel metrics) {
        Banca banca = bancaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Banca", "ID", id));

        BancaDetailDto dto = bancaMapper.toDetailDto(banca);
        if (metrics != null) {
            dto.setQuestaoStats(statsAssembler.buildStats(id, "BANCA", metrics));
        }

        return dto;
    }

    @CacheEvict(value = "banca-stats", allEntries = true)
    public Long create(BancaCreateRequest request) {
        log.info("Criando nova banca: {}", request.getNome());

        Optional<Banca> existing = bancaRepository.findByNomeIgnoreCase(request.getNome());
        if (existing.isPresent()) {
            throw new ConflictException("Já existe uma banca com o nome '" + request.getNome() + "'");
        }

        Banca banca = bancaMapper.toEntity(request);
        return bancaRepository.save(banca).getId();
    }

    @CacheEvict(value = "banca-stats", allEntries = true)
    public void update(Long id, BancaUpdateRequest request) {
        log.info("Atualizando banca ID: {}", id);

        Banca banca = bancaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Banca", "ID", id));

        if (request.getNome() != null) {
            Optional<Banca> existing = bancaRepository.findByNomeIgnoreCase(request.getNome());
            if (existing.isPresent() && !existing.get().getId().equals(id)) {
                throw new ConflictException("Já existe uma banca com o nome '" + request.getNome() + "'");
            }
        }

        bancaMapper.updateEntityFromDto(request, banca);
        bancaRepository.save(banca);
    }

    @CacheEvict(value = "banca-stats", allEntries = true)
    public void delete(Long id) {
        log.info("Excluindo banca ID: {}", id);
        if (!bancaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Banca", "ID", id);
        }

        if (concursoRepository.existsByBancaId(id)) {
            throw new ConflictException("Não é possível excluir uma banca que possui concursos associados");
        }

        bancaRepository.deleteById(id);
    }
}
