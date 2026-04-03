package com.studora.service;

import com.studora.dto.PageResponse;
import com.studora.dto.simulado.SimuladoDetailDto;
import com.studora.dto.request.SimuladoGenerationRequest;
import com.studora.dto.simulado.SimuladoSummaryDto;
import com.studora.entity.Questao;
import com.studora.entity.Simulado;
import com.studora.exception.ResourceNotFoundException;
import com.studora.exception.ValidationException;
import com.studora.mapper.SimuladoMapper;
import com.studora.repository.DisciplinaRepository;
import com.studora.repository.QuestaoRepository;
import com.studora.repository.RespostaRepository;
import com.studora.repository.SimuladoRepository;
import com.studora.repository.SubtemaRepository;
import com.studora.repository.TemaRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class SimuladoService {

    private final SimuladoRepository simuladoRepository;
    private final QuestaoRepository questaoRepository;
    private final RespostaRepository respostaRepository;
    private final SimuladoMapper simuladoMapper;
    private final DisciplinaRepository disciplinaRepository;
    private final TemaRepository temaRepository;
    private final SubtemaRepository subtemaRepository;
    private final com.studora.repository.BancaRepository bancaRepository;
    private final com.studora.repository.CargoRepository cargoRepository;
    private final com.studora.mapper.BancaMapper bancaMapper;
    private final com.studora.mapper.CargoMapper cargoMapper;

    public SimuladoService(SimuladoRepository simuladoRepository,
                           QuestaoRepository questaoRepository,
                           RespostaRepository respostaRepository,
                           SimuladoMapper simuladoMapper,
                           DisciplinaRepository disciplinaRepository,
                           TemaRepository temaRepository,
                           SubtemaRepository subtemaRepository,
                           com.studora.repository.BancaRepository bancaRepository,
                           com.studora.repository.CargoRepository cargoRepository,
                           com.studora.mapper.BancaMapper bancaMapper,
                           com.studora.mapper.CargoMapper cargoMapper) {
        this.simuladoRepository = simuladoRepository;
        this.questaoRepository = questaoRepository;
        this.respostaRepository = respostaRepository;
        this.simuladoMapper = simuladoMapper;
        this.disciplinaRepository = disciplinaRepository;
        this.temaRepository = temaRepository;
        this.subtemaRepository = subtemaRepository;
        this.bancaRepository = bancaRepository;
        this.cargoRepository = cargoRepository;
        this.bancaMapper = bancaMapper;
        this.cargoMapper = cargoMapper;
    }

    @Cacheable(value = "simulado-stats", key = "T(java.util.Objects).hash(#pageable.pageNumber, #pageable.pageSize, #pageable.sort.toString())")
    @Transactional(readOnly = true)
    public Page<SimuladoSummaryDto> findAll(Pageable pageable) {
        return simuladoRepository.findAll(pageable)
                .map(s -> {
                    SimuladoSummaryDto dto = simuladoMapper.toSummaryDto(s);
                    enrichSimuladoDto(s, dto);
                    return dto;
                });
    }

    @Transactional(readOnly = true)
    public SimuladoDetailDto getSimuladoDetailById(Long id) {
        Simulado simulado = simuladoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Simulado", "ID", id));

        SimuladoDetailDto dto = simuladoMapper.toDetailDto(simulado);
        
        // Process the DTO to filter responses to only include those for this simulado
        if (dto.getQuestoes() != null) {
            for (var questaoDto : dto.getQuestoes()) {
                if (questaoDto.getRespostas() != null) {
                    // Filter responses to only include those that belong to this simulado
                    List<com.studora.dto.resposta.RespostaSummaryDto> filteredResponses = 
                        questaoDto.getRespostas().stream()
                            .filter(resp -> resp.getSimuladoId() != null && resp.getSimuladoId().equals(id))
                            .collect(Collectors.toList());
                    
                    questaoDto.setRespostas(filteredResponses);

                    // If not answered in this simulado, hide the correct answers and justifications
                    if (filteredResponses.isEmpty()) {
                        if (questaoDto.getAlternativas() != null) {
                            questaoDto.getAlternativas().forEach(a -> {
                                a.setCorreta(null);
                                a.setJustificativa(null);
                            });
                        }
                    }
                }
            }
        }
        
        enrichSimuladoDto(simulado, dto);
        return dto;
    }

    @CacheEvict(value = "simulado-stats", allEntries = true)
    public SimuladoDetailDto create(SimuladoSummaryDto request) {
        Simulado simulado = simuladoMapper.toEntity(request);
        Simulado saved = simuladoRepository.save(simulado);
        SimuladoDetailDto dto = simuladoMapper.toDetailDto(saved);
        enrichSimuladoDto(saved, dto);
        return dto;
    }

    @CacheEvict(value = "simulado-stats", allEntries = true)
    public SimuladoDetailDto gerarSimulado(SimuladoGenerationRequest request) {
        log.info("Gerando simulado: {}", request.getNome());
        
        boolean atLeastOneSelection = (request.getDisciplinas() != null && !request.getDisciplinas().isEmpty()) ||
                                     (request.getTemas() != null && !request.getTemas().isEmpty()) ||
                                     (request.getSubtemas() != null && !request.getSubtemas().isEmpty());
        
        if (!atLeastOneSelection) {
            throw new ValidationException("Pelo menos uma seleção (disciplinas, temas ou subtemas) deve ser fornecida.");
        }

        int totalSubtemas = request.getSubtemas() != null ? request.getSubtemas().stream().mapToInt(SimuladoGenerationRequest.ItemSelection::getQuantidade).sum() : 0;
        int totalTemas = request.getTemas() != null ? request.getTemas().stream().mapToInt(SimuladoGenerationRequest.ItemSelection::getQuantidade).sum() : 0;
        int totalDisciplinas = request.getDisciplinas() != null ? request.getDisciplinas().stream().mapToInt(SimuladoGenerationRequest.ItemSelection::getQuantidade).sum() : 0;
        
        int totalRequested = totalSubtemas + totalTemas + totalDisciplinas;
        if (totalRequested < 20) {
            throw new ValidationException("O simulado deve ter no total pelo menos 20 questões solicitadas (total atual: " + totalRequested + ").");
        }

        Simulado simulado = new Simulado();
        simulado.setNome(request.getNome());
        simulado.setBancaId(request.getBancaId());
        simulado.setCargoId(request.getCargoId());
        simulado.setAreas(request.getAreas());
        simulado.setNivel(request.getNivel());
        simulado.setIgnorarRespondidas(request.getIgnorarRespondidas());
        
        if (request.getDisciplinas() != null) {
            simulado.setDisciplinas(request.getDisciplinas().stream()
                .map(i -> new com.studora.entity.SimuladoItemSelection(i.getId(), i.getQuantidade()))
                .collect(Collectors.toList()));
        }
        if (request.getTemas() != null) {
            simulado.setTemas(request.getTemas().stream()
                .map(i -> new com.studora.entity.SimuladoItemSelection(i.getId(), i.getQuantidade()))
                .collect(Collectors.toList()));
        }
        if (request.getSubtemas() != null) {
            simulado.setSubtemas(request.getSubtemas().stream()
                .map(i -> new com.studora.entity.SimuladoItemSelection(i.getId(), i.getQuantidade()))
                .collect(Collectors.toList()));
        }

        Set<Long> collectedIds = new HashSet<>();

        // 1. Subtemas (Mais específicos)
        if (request.getSubtemas() != null) {
            for (var sel : request.getSubtemas()) {
                List<Long> ids = questaoRepository.findIdsBySubtemaWithPreferences(sel.getId(), request, new ArrayList<>(collectedIds), PageRequest.of(0, sel.getQuantidade()));
                collectedIds.addAll(ids);
            }
        }

        // 2. Temas (Excluindo os subtemas já processados se possível)
        if (request.getTemas() != null) {
            List<Long> subtemaIdsToAvoid = request.getSubtemas() != null ? request.getSubtemas().stream().map(SimuladoGenerationRequest.ItemSelection::getId).toList() : List.of();
            for (var sel : request.getTemas()) {
                List<Long> ids = questaoRepository.findIdsByTemaWithPreferences(sel.getId(), subtemaIdsToAvoid, request, new ArrayList<>(collectedIds), PageRequest.of(0, sel.getQuantidade()));
                collectedIds.addAll(ids);
            }
        }

        // 3. Disciplinas (Excluindo temas e subtemas já processados se possível)
        if (request.getDisciplinas() != null) {
            List<Long> temaIdsToAvoid = request.getTemas() != null ? request.getTemas().stream().map(SimuladoGenerationRequest.ItemSelection::getId).toList() : List.of();
            List<Long> subtemaIdsToAvoid = request.getSubtemas() != null ? request.getSubtemas().stream().map(SimuladoGenerationRequest.ItemSelection::getId).toList() : List.of();
            for (var sel : request.getDisciplinas()) {
                List<Long> ids = questaoRepository.findIdsByDisciplinaWithPreferences(sel.getId(), temaIdsToAvoid, subtemaIdsToAvoid, request, new ArrayList<>(collectedIds), PageRequest.of(0, sel.getQuantidade()));
                collectedIds.addAll(ids);
            }
        }

        if (collectedIds.size() < 20) {
            throw new ValidationException("Não foi possível encontrar o número mínimo de 20 questões para gerar o simulado. Encontradas: " + collectedIds.size());
        }

        List<Questao> questions = questaoRepository.findAllById(collectedIds);
        simulado.setQuestoes(new ArrayList<>(questions));

        Simulado saved = simuladoRepository.save(simulado);
        SimuladoDetailDto dto = simuladoMapper.toDetailDto(saved);
        enrichSimuladoDto(saved, dto);
        return dto;
    }

    @CacheEvict(value = "simulado-stats", allEntries = true)
    public void delete(Long id) {
        log.info("Excluindo simulado ID: {}", id);
        if (!simuladoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Simulado", "ID", id);
        }
        
        respostaRepository.detachSimulado(id);
        simuladoRepository.deleteById(id);
    }

    @CacheEvict(value = "simulado-stats", allEntries = true)
    public SimuladoDetailDto iniciarSimulado(Long id) {
        Simulado simulado = simuladoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Simulado", "ID", id));
        
        if (simulado.getStartedAt() != null) {
            throw new ValidationException("Este simulado já foi iniciado.");
        }

        simulado.setStartedAt(LocalDateTime.now());
        Simulado saved = simuladoRepository.save(simulado);
        SimuladoDetailDto dto = simuladoMapper.toDetailDto(saved);
        enrichSimuladoDto(saved, dto);
        return dto;
    }

    @CacheEvict(value = "simulado-stats", allEntries = true)
    public SimuladoDetailDto finalizarSimulado(Long id) {
        Simulado simulado = simuladoRepository.findByIdWithQuestoes(id)
                .orElseThrow(() -> new ResourceNotFoundException("Simulado", "ID", id));
        
        if (simulado.getFinishedAt() != null) {
            throw new ValidationException("Este simulado já foi finalizado.");
        }

        int totalQuestions = simulado.getQuestoes().size();
        int answeredCount = respostaRepository.countBySimuladoId(id);

        if (answeredCount < totalQuestions) {
            throw new ValidationException("Não é possível finalizar o simulado: existem " + (totalQuestions - answeredCount) + " questões sem resposta.");
        }

        simulado.setFinishedAt(LocalDateTime.now());
        Simulado saved = simuladoRepository.save(simulado);
        SimuladoDetailDto dto = simuladoMapper.toDetailDto(saved);
        enrichSimuladoDto(saved, dto);
        return dto;
    }

    private void enrichSimuladoDto(Simulado simulado, Object dtoObj) {
        if (simulado.getBancaId() != null) {
            bancaRepository.findById(simulado.getBancaId())
                .ifPresent(b -> setBanca(dtoObj, bancaMapper.toSummaryDto(b)));
        }
        if (simulado.getCargoId() != null) {
            cargoRepository.findById(simulado.getCargoId())
                .ifPresent(c -> setCargo(dtoObj, cargoMapper.toSummaryDto(c)));
        }
        
        setDisciplinas(dtoObj, mapDisciplinas(simulado.getDisciplinas()));
        setTemas(dtoObj, mapTemas(simulado.getTemas()));
        setSubtemas(dtoObj, mapSubtemas(simulado.getSubtemas()));
    }

    private void setBanca(Object dto, com.studora.dto.banca.BancaSummaryDto value) {
        if (dto instanceof SimuladoSummaryDto s) s.setBanca(value);
        else if (dto instanceof SimuladoDetailDto d) d.setBanca(value);
    }

    private void setCargo(Object dto, com.studora.dto.cargo.CargoSummaryDto value) {
        if (dto instanceof SimuladoSummaryDto s) s.setCargo(value);
        else if (dto instanceof SimuladoDetailDto d) d.setCargo(value);
    }

    private void setDisciplinas(Object dto, List<com.studora.dto.simulado.DisciplinaSimuladoDto> value) {
        if (dto instanceof SimuladoSummaryDto s) s.setDisciplinas(value);
        else if (dto instanceof SimuladoDetailDto d) d.setDisciplinas(value);
    }

    private void setTemas(Object dto, List<com.studora.dto.simulado.TemaSimuladoDto> value) {
        if (dto instanceof SimuladoSummaryDto s) s.setTemas(value);
        else if (dto instanceof SimuladoDetailDto d) d.setTemas(value);
    }

    private void setSubtemas(Object dto, List<com.studora.dto.simulado.SubtemaSimuladoDto> value) {
        if (dto instanceof SimuladoSummaryDto s) s.setSubtemas(value);
        else if (dto instanceof SimuladoDetailDto d) d.setSubtemas(value);
    }

    private List<com.studora.dto.simulado.DisciplinaSimuladoDto> mapDisciplinas(List<com.studora.entity.SimuladoItemSelection> selections) {
        if (selections == null) return List.of();

        List<Long> ids = selections.stream().map(com.studora.entity.SimuladoItemSelection::getItemId).toList();
        Map<Long, String> nomeMap = disciplinaRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(com.studora.entity.Disciplina::getId, com.studora.entity.Disciplina::getNome));

        return selections.stream().map(s -> {
            var dto = new com.studora.dto.simulado.DisciplinaSimuladoDto();
            dto.setId(s.getItemId());
            dto.setQuantidade(s.getQuantidade());
            dto.setNome(nomeMap.get(s.getItemId()));
            return dto;
        }).collect(Collectors.toList());
    }

    private List<com.studora.dto.simulado.TemaSimuladoDto> mapTemas(List<com.studora.entity.SimuladoItemSelection> selections) {
        if (selections == null) return List.of();

        List<Long> ids = selections.stream().map(com.studora.entity.SimuladoItemSelection::getItemId).toList();
        Map<Long, com.studora.entity.Tema> temaMap = temaRepository.findAllByIdWithDisciplina(ids).stream()
                .collect(Collectors.toMap(com.studora.entity.Tema::getId, t -> t));

        return selections.stream().map(s -> {
            var dto = new com.studora.dto.simulado.TemaSimuladoDto();
            dto.setId(s.getItemId());
            dto.setQuantidade(s.getQuantidade());
            com.studora.entity.Tema tema = temaMap.get(s.getItemId());
            if (tema != null) {
                dto.setNome(tema.getNome());
                if (tema.getDisciplina() != null) {
                    dto.setDisciplinaId(tema.getDisciplina().getId());
                    dto.setDisciplinaNome(tema.getDisciplina().getNome());
                }
            }
            return dto;
        }).collect(Collectors.toList());
    }

    private List<com.studora.dto.simulado.SubtemaSimuladoDto> mapSubtemas(List<com.studora.entity.SimuladoItemSelection> selections) {
        if (selections == null) return List.of();

        List<Long> ids = selections.stream().map(com.studora.entity.SimuladoItemSelection::getItemId).toList();
        Map<Long, com.studora.entity.Subtema> subtemaMap = subtemaRepository.findAllByIdWithTemaAndDisciplina(ids).stream()
                .collect(Collectors.toMap(com.studora.entity.Subtema::getId, s -> s));

        return selections.stream().map(s -> {
            var dto = new com.studora.dto.simulado.SubtemaSimuladoDto();
            dto.setId(s.getItemId());
            dto.setQuantidade(s.getQuantidade());
            com.studora.entity.Subtema subtema = subtemaMap.get(s.getItemId());
            if (subtema != null) {
                dto.setNome(subtema.getNome());
                if (subtema.getTema() != null) {
                    dto.setTemaId(subtema.getTema().getId());
                    dto.setTemaNome(subtema.getTema().getNome());
                    if (subtema.getTema().getDisciplina() != null) {
                        dto.setDisciplinaId(subtema.getTema().getDisciplina().getId());
                        dto.setDisciplinaNome(subtema.getTema().getDisciplina().getNome());
                    }
                }
            }
            return dto;
        }).collect(Collectors.toList());
    }
}
