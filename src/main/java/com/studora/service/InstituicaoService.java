package com.studora.service;

import com.studora.dto.MetricsLevel;
import com.studora.dto.QuestaoStatsDto;
import com.studora.dto.instituicao.InstituicaoDetailDto;
import com.studora.dto.instituicao.InstituicaoSummaryDto;
import com.studora.dto.request.InstituicaoCreateRequest;
import com.studora.dto.request.InstituicaoUpdateRequest;
import com.studora.entity.Instituicao;
import com.studora.exception.ConflictException;
import com.studora.exception.ResourceNotFoundException;
import com.studora.exception.ValidationException;
import com.studora.mapper.InstituicaoMapper;
import com.studora.repository.ConcursoRepository;
import com.studora.repository.InstituicaoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class InstituicaoService {

    private final InstituicaoRepository instituicaoRepository;
    private final InstituicaoMapper instituicaoMapper;
    private final ConcursoRepository concursoRepository;
    private final StatsAssembler statsAssembler;

    @Transactional(readOnly = true)
    public Page<InstituicaoSummaryDto> findAll(String nome, Pageable pageable, MetricsLevel metrics) {
        Page<Instituicao> page;
        if (nome != null && !nome.isBlank()) {
            String normalized = com.studora.util.StringUtils.normalizeForSearch(nome);
            page = instituicaoRepository.findByNomeNormalizedContaining(normalized, pageable);
        } else {
            page = instituicaoRepository.findAll(pageable);
        }

        return page.map(instituicao -> {
            InstituicaoSummaryDto dto = instituicaoMapper.toSummaryDto(instituicao);
            if (metrics != null) {
                dto.setQuestaoStats(statsAssembler.buildStats(instituicao.getId(), "INSTITUICAO", metrics));
            }
            return dto;
        });
    }

    @Transactional(readOnly = true)
    public InstituicaoDetailDto getInstituicaoDetailById(Long id, MetricsLevel metrics) {
        Instituicao instituicao = instituicaoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Instituição", "ID", id));

        InstituicaoDetailDto dto = instituicaoMapper.toDetailDto(instituicao);
        if (metrics != null) {
            dto.setQuestaoStats(statsAssembler.buildStats(id, "INSTITUICAO", metrics));
        }
        
        return dto;
    }

    public Long create(InstituicaoCreateRequest request) {
        log.info("Criando nova instituição: {}", request.getNome());
        
        Optional<Instituicao> existing = instituicaoRepository.findByNomeIgnoreCase(request.getNome());
        if (existing.isPresent()) {
            throw new ConflictException("Já existe uma instituição com o nome '" + request.getNome() + "'");
        }

        Instituicao instituicao = instituicaoMapper.toEntity(request);
        return instituicaoRepository.save(instituicao).getId();
    }

    public void update(Long id, InstituicaoUpdateRequest request) {
        log.info("Atualizando instituição ID: {}", id);
        
        Instituicao instituicao = instituicaoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Instituição", "ID", id));

        if (request.getNome() != null) {
            Optional<Instituicao> existing = instituicaoRepository.findByNomeIgnoreCase(request.getNome());
            if (existing.isPresent() && !existing.get().getId().equals(id)) {
                throw new ConflictException("Já existe uma instituição com o nome '" + request.getNome() + "'");
            }
        }

        instituicaoMapper.updateEntityFromDto(request, instituicao);
        instituicaoRepository.save(instituicao);
    }

    public void delete(Long id) {
        log.info("Excluindo instituição ID: {}", id);
        if (!instituicaoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Instituição", "ID", id);
        }
        
        if (concursoRepository.existsByInstituicaoId(id)) {
            throw new ValidationException("Não é possível excluir uma instituição que possui concursos associados");
        }
        
        instituicaoRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<String> findAllAreas(String query) {
        if (query != null && !query.isBlank()) {
            String normalized = com.studora.util.StringUtils.normalizeForSearch(query);
            return instituicaoRepository.findDistinctAreas(normalized);
        }
        return instituicaoRepository.findDistinctAreas();
    }
}
