package com.studora.service;

import com.studora.dto.QuestaoDto;
import com.studora.dto.SimuladoDto;
import com.studora.dto.request.SimuladoGenerationRequest;
import com.studora.entity.*;
import com.studora.exception.ResourceNotFoundException;
import com.studora.exception.ValidationException;
import com.studora.mapper.QuestaoMapper;
import com.studora.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SimuladoService {

    private final SimuladoRepository simuladoRepository;
    private final QuestaoRepository questaoRepository;
    private final RespostaRepository respostaRepository;
    private final SubtemaRepository subtemaRepository;
    private final TemaRepository temaRepository;
    private final QuestaoMapper questaoMapper;

    @Transactional
    public SimuladoDto gerarSimulado(SimuladoGenerationRequest request) {
        log.info("Gerando simulado: {}", request.getNome());
        
        validarRequest(request);

        List<Long> allSelectedIds = new ArrayList<>();
        
        // Date filter logic
        Integer minAno = request.getAno();
        if (minAno != null) {
            int currentYear = LocalDate.now().getYear();
            if (minAno > currentYear) {
                minAno = null; // Ignore filter if future
            }
        }

        // --- Stage 1: Subtemas (Highest Priority) ---
        // ... (rest of the logic remains same until the final count check)
        if (request.getSubtemas() != null) {
            for (SimuladoGenerationRequest.ItemSelection subRequest : request.getSubtemas()) {
                List<Long> ids = questaoRepository.findIdsBySubtemaWithPreferences(
                        subRequest.getId(),
                        request,
                        allSelectedIds.isEmpty() ? List.of(-1L) : allSelectedIds,
                        PageRequest.of(0, subRequest.getQuantidade())
                );
                allSelectedIds.addAll(ids);
            }
        }

        // Prepare hierarchy maps for exclusions
        Map<Long, List<Long>> temaToSubtemasMap = new HashMap<>();
        if (request.getSubtemas() != null) {
            Set<Long> requestedSubtemaIds = request.getSubtemas().stream()
                    .map(SimuladoGenerationRequest.ItemSelection::getId)
                    .collect(Collectors.toSet());
            
            List<Subtema> subtemas = subtemaRepository.findAllById(requestedSubtemaIds);
            for (Subtema s : subtemas) {
                temaToSubtemasMap.computeIfAbsent(s.getTema().getId(), k -> new ArrayList<>()).add(s.getId());
            }
        }

        Map<Long, List<Long>> disciplinaToTemasMap = new HashMap<>();
        Map<Long, List<Long>> disciplinaToSubtemasMap = new HashMap<>();
        
        if (request.getTemas() != null) {
            Set<Long> requestedTemaIds = request.getTemas().stream()
                    .map(SimuladoGenerationRequest.ItemSelection::getId)
                    .collect(Collectors.toSet());
            List<Tema> temas = temaRepository.findAllById(requestedTemaIds);
            for (Tema t : temas) {
                disciplinaToTemasMap.computeIfAbsent(t.getDisciplina().getId(), k -> new ArrayList<>()).add(t.getId());
            }
        }
        
        if (request.getSubtemas() != null) {
            Set<Long> requestedSubtemaIds = request.getSubtemas().stream()
                    .map(SimuladoGenerationRequest.ItemSelection::getId)
                    .collect(Collectors.toSet());
            List<Subtema> subtemas = subtemaRepository.findAllById(requestedSubtemaIds);
            for (Subtema s : subtemas) {
                Long disciplinaId = s.getTema().getDisciplina().getId();
                disciplinaToSubtemasMap.computeIfAbsent(disciplinaId, k -> new ArrayList<>()).add(s.getId());
            }
        }

        // --- Stage 2: Temas ---
        if (request.getTemas() != null) {
            for (SimuladoGenerationRequest.ItemSelection temaRequest : request.getTemas()) {
                Long temaId = temaRequest.getId();
                List<Long> avoidSubtemaIds = temaToSubtemasMap.getOrDefault(temaId, List.of(-1L));
                
                List<Long> primaryIds = questaoRepository.findIdsByTemaWithPreferences(
                        temaId,
                        avoidSubtemaIds,
                        request,
                        allSelectedIds.isEmpty() ? List.of(-1L) : allSelectedIds,
                        PageRequest.of(0, temaRequest.getQuantidade())
                );
                
                allSelectedIds.addAll(primaryIds);
                
                int needed = temaRequest.getQuantidade() - primaryIds.size();
                if (needed > 0) {
                    List<Long> backfillIds = questaoRepository.findIdsByTemaWithPreferences(
                            temaId,
                            null,
                            request,
                            allSelectedIds.isEmpty() ? List.of(-1L) : allSelectedIds,
                            PageRequest.of(0, needed)
                    );
                    allSelectedIds.addAll(backfillIds);
                }
            }
        }

        // --- Stage 3: Disciplinas ---
        if (request.getDisciplinas() != null) {
            for (SimuladoGenerationRequest.ItemSelection discRequest : request.getDisciplinas()) {
                Long discId = discRequest.getId();
                List<Long> avoidTemaIds = disciplinaToTemasMap.getOrDefault(discId, List.of(-1L));
                List<Long> avoidSubtemaIds = disciplinaToSubtemasMap.getOrDefault(discId, List.of(-1L));
                
                List<Long> primaryIds = questaoRepository.findIdsByDisciplinaWithPreferences(
                        discId,
                        avoidTemaIds,
                        avoidSubtemaIds,
                        request,
                        allSelectedIds.isEmpty() ? List.of(-1L) : allSelectedIds,
                        PageRequest.of(0, discRequest.getQuantidade())
                );
                
                allSelectedIds.addAll(primaryIds);
                
                int needed = discRequest.getQuantidade() - primaryIds.size();
                if (needed > 0) {
                    List<Long> backfillIds = questaoRepository.findIdsByDisciplinaWithPreferences(
                            discId,
                            null, null,
                            request,
                            allSelectedIds.isEmpty() ? List.of(-1L) : allSelectedIds,
                            PageRequest.of(0, needed)
                    );
                    allSelectedIds.addAll(backfillIds);
                }
            }
        }

        if (allSelectedIds.size() < 20) {
            throw new ValidationException("Não foi possível encontrar o mínimo de 20 questões que atendam aos critérios (encontradas: " + allSelectedIds.size() + ").");
        }

        Simulado simulado = new Simulado();
        simulado.setNome(request.getNome());
        
        List<Questao> questoes = questaoRepository.findAllById(allSelectedIds);
        simulado.setQuestoes(new LinkedHashSet<>(questoes));

        Simulado savedSimulado = simuladoRepository.save(simulado);
        return toDto(savedSimulado);
    }

    private void validarRequest(SimuladoGenerationRequest request) {
        boolean hasDisciplinas = request.getDisciplinas() != null && !request.getDisciplinas().isEmpty();
        boolean hasTemas = request.getTemas() != null && !request.getTemas().isEmpty();
        boolean hasSubtemas = request.getSubtemas() != null && !request.getSubtemas().isEmpty();

        // Specific rule: if no subtema and no tema, must have at least 1 disciplina
        if (!hasSubtemas && !hasTemas && !hasDisciplinas) {
            throw new ValidationException("Você deve selecionar pelo menos uma disciplina caso não selecione temas ou subtemas.");
        }

        int totalSolicitado = 0;
        if (hasDisciplinas) totalSolicitado += request.getDisciplinas().stream().mapToInt(SimuladoGenerationRequest.ItemSelection::getQuantidade).sum();
        if (hasTemas) totalSolicitado += request.getTemas().stream().mapToInt(SimuladoGenerationRequest.ItemSelection::getQuantidade).sum();
        if (hasSubtemas) totalSolicitado += request.getSubtemas().stream().mapToInt(SimuladoGenerationRequest.ItemSelection::getQuantidade).sum();

        if (totalSolicitado < 20) {
            throw new ValidationException("O simulado deve ter no total pelo menos 20 questões solicitadas (total atual: " + totalSolicitado + ").");
        }
    }
    
    // ... existing methods: iniciarSimulado, finalizarSimulado, getSimuladoResult, deleteSimulado, toDto ...

    @Transactional
    public SimuladoDto iniciarSimulado(Long id) {
        Simulado simulado = simuladoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Simulado", "ID", id));
        
        if (simulado.getStartedAt() != null) {
            throw new ValidationException("Este simulado já foi iniciado.");
        }

        simulado.setStartedAt(LocalDateTime.now());
        return toDto(simuladoRepository.save(simulado));
    }

    @Transactional
    public SimuladoDto finalizarSimulado(Long id) {
        Simulado simulado = simuladoRepository.findByIdWithQuestoes(id)
                .orElseThrow(() -> new ResourceNotFoundException("Simulado", "ID", id));
        
        if (simulado.getStartedAt() == null) {
            throw new ValidationException("Não é possível finalizar um simulado que não foi iniciado.");
        }
        
        if (simulado.getFinishedAt() != null) {
            throw new ValidationException("Este simulado já foi finalizado.");
        }

        // Validate that all questions have been answered
        List<Resposta> respostas = respostaRepository.findBySimuladoId(id);
        Set<Long> answeredQuestaoIds = respostas.stream()
                .map(r -> r.getQuestao().getId())
                .collect(Collectors.toSet());

        if (answeredQuestaoIds.size() < simulado.getQuestoes().size()) {
            throw new ValidationException("Não é possível finalizar o simulado: existem questões sem resposta.");
        }

        simulado.setFinishedAt(LocalDateTime.now());
        return toDto(simuladoRepository.save(simulado));
    }

    @Transactional(readOnly = true)
    public SimuladoDto getSimuladoResult(Long id) {
        Simulado simulado = simuladoRepository.findByIdWithQuestoes(id)
                .orElseThrow(() -> new ResourceNotFoundException("Simulado", "ID", id));
        return toDto(simulado);
    }

    @Transactional
    public void deleteSimulado(Long id) {
        if (!simuladoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Simulado", "ID", id);
        }
        // Detach related answers instead of deleting them
        respostaRepository.detachSimulado(id);
        simuladoRepository.deleteById(id);
    }

    private final com.studora.mapper.RespostaMapper respostaMapper;

    private SimuladoDto toDto(Simulado simulado) {
        SimuladoDto dto = new SimuladoDto();
        dto.setId(simulado.getId());
        dto.setNome(simulado.getNome());
        dto.setStartedAt(simulado.getStartedAt());
        dto.setFinishedAt(simulado.getFinishedAt());

        if (simulado.getQuestoes() != null) {
            Map<Long, Resposta> respostaMap = new HashMap<>();
            if (simulado.getFinishedAt() != null) {
                List<Resposta> respostas = respostaRepository.findBySimuladoId(simulado.getId());
                respostas.forEach(r -> respostaMap.put(r.getQuestao().getId(), r));
            }

            dto.setQuestoes(simulado.getQuestoes().stream()
                    .map(q -> {
                        QuestaoDto qDto = questaoMapper.toDto(q);
                        boolean answered = false;
                        if (simulado.getFinishedAt() != null && respostaMap.containsKey(q.getId())) {
                            qDto.setResposta(respostaMapper.toDto(respostaMap.get(q.getId())));
                            answered = true;
                        }
                        
                        // Dynamic visibility within Simulado: hide correction details if not answered IN THIS simulado
                        if (!answered && qDto.getAlternativas() != null) {
                            qDto.getAlternativas().forEach(a -> {
                                a.setCorreta(null);
                                a.setJustificativa(null);
                            });
                        }
                        return qDto;
                    })
                    .collect(Collectors.toList()));
        }

        return dto;
    }
}
