package com.studora.service;

import com.studora.dto.DificuldadeStatDto;
import com.studora.dto.MetricsLevel;
import com.studora.dto.QuestaoStatsDto;
import com.studora.dto.StatSliceDto;
import com.studora.dto.concurso.QuestaoEstatisticasConcursoCargoDto;
import com.studora.entity.Dificuldade;
import com.studora.repository.BancaRepository;
import com.studora.repository.CargoRepository;
import com.studora.repository.InstituicaoRepository;
import com.studora.repository.QuestaoRepository;
import com.studora.repository.RespostaRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class StatsAssembler {

    private final QuestaoRepository questaoRepository;
    private final RespostaRepository respostaRepository;
    private final BancaRepository bancaRepository;
    private final InstituicaoRepository instituicaoRepository;
    private final CargoRepository cargoRepository;

    public StatsAssembler(QuestaoRepository questaoRepository, RespostaRepository respostaRepository,
                         BancaRepository bancaRepository, InstituicaoRepository instituicaoRepository,
                         CargoRepository cargoRepository) {
        this.questaoRepository = questaoRepository;
        this.respostaRepository = respostaRepository;
        this.bancaRepository = bancaRepository;
        this.instituicaoRepository = instituicaoRepository;
        this.cargoRepository = cargoRepository;
    }

    public QuestaoStatsDto buildStats(Long scopeId, String scopeType, MetricsLevel metrics) {
        if (metrics == null) return null;
        
        QuestaoStatsDto stats = new QuestaoStatsDto();
        boolean isFull = metrics == MetricsLevel.FULL;
        
        // --- Total ---
        stats.setTotal(fetchTotalStats(scopeId, scopeType));

        // --- Autoral breakdown for taxonomy scopes (only when FULL) ---
        if (isFull && (scopeType.equals("DISCIPLINA") || scopeType.equals("TEMA") || scopeType.equals("SUBTEMA"))) {
            Long autoralCount = fetchAutoralCount(scopeId, scopeType);
            if (autoralCount != null && autoralCount > 0) {
                StatSliceDto autoralSlice = new StatSliceDto();
                autoralSlice.setTotalQuestoes(autoralCount);

                List<Object[]> autoralResp = fetchAutoralRespondidas(scopeId, scopeType);
                List<Object[]> autoralAcert = fetchAutoralAcertadas(scopeId, scopeType);
                if (!autoralResp.isEmpty()) {
                    long acertadas = 0;
                    if (!autoralAcert.isEmpty()) acertadas = ((Number) autoralAcert.get(0)[1]).longValue();
                    autoralSlice.setRespondidas(((Number) autoralResp.get(0)[1]).longValue());
                    autoralSlice.setAcertadas(acertadas);
                }

                List<Object[]> autoralTempo = fetchAutoralTempo(scopeId, scopeType);
                if (!autoralTempo.isEmpty() && autoralTempo.get(0)[1] != null) {
                    autoralSlice.setMediaTempoResposta(((Double) autoralTempo.get(0)[1]).intValue());
                }

                List<Object[]> autoralLatest = fetchAutoralLatest(scopeId, scopeType);
                if (!autoralLatest.isEmpty()) {
                    autoralSlice.setUltimaQuestao((java.time.LocalDateTime) autoralLatest.get(0)[1]);
                }

                List<Object[]> autoralDiff = fetchAutoralDificuldade(scopeId, scopeType);
                if (!autoralDiff.isEmpty()) {
                    Map<String, DificuldadeStatDto> diffMap = new HashMap<>();
                    for (Object[] row : autoralDiff) {
                        int dId = ((Number) row[1]).intValue();
                        String dName = Dificuldade.fromId(dId).name();
                        DificuldadeStatDto dDto = new DificuldadeStatDto();
                        dDto.setTotal(((Number) row[2]).longValue());
                        dDto.setCorretas(((Number) row[3]).longValue());
                        diffMap.put(dName, dDto);
                    }
                    autoralSlice.setDificuldade(diffMap);
                }

                stats.setPorAutoral(autoralSlice);
            }
        }

        // --- Breakdowns ---
        if (isFull) {
            switch (scopeType) {
                case "DISCIPLINA":
                    stats.setPorNivel(fetchPorNivelDisciplina(scopeId));
                    stats.setPorBanca(fetchPorBancaDisciplina(scopeId));
                    stats.setPorInstituicao(fetchPorInstituicaoDisciplina(scopeId));
                    stats.setPorAreaInstituicao(fetchPorAreaInstituicaoDisciplina(scopeId));
                    stats.setPorCargo(fetchPorCargoDisciplina(scopeId));
                    stats.setPorAreaCargo(fetchPorAreaCargoDisciplina(scopeId));
                    break;
                case "TEMA":
                    stats.setPorNivel(fetchPorNivelTema(scopeId));
                    stats.setPorBanca(fetchPorBancaTema(scopeId));
                    stats.setPorInstituicao(fetchPorInstituicaoTema(scopeId));
                    stats.setPorAreaInstituicao(fetchPorAreaInstituicaoTema(scopeId));
                    stats.setPorCargo(fetchPorCargoTema(scopeId));
                    stats.setPorAreaCargo(fetchPorAreaCargoTema(scopeId));
                    break;
                case "SUBTEMA":
                    stats.setPorNivel(fetchPorNivelSubtema(scopeId));
                    stats.setPorBanca(fetchPorBancaSubtema(scopeId));
                    stats.setPorInstituicao(fetchPorInstituicaoSubtema(scopeId));
                    stats.setPorAreaInstituicao(fetchPorAreaInstituicaoSubtema(scopeId));
                    stats.setPorCargo(fetchPorCargoSubtema(scopeId));
                    stats.setPorAreaCargo(fetchPorAreaCargoSubtema(scopeId));
                    break;
                case "BANCA":
                    stats.setPorNivel(fetchPorNivelBanca(scopeId));
                    stats.setPorAreaInstituicao(fetchPorAreaInstituicaoBanca(scopeId));
                    stats.setPorAreaCargo(fetchPorAreaCargoBanca(scopeId));
                    break;
                case "INSTITUICAO":
                    stats.setPorNivel(fetchPorNivelInstituicao(scopeId));
                    stats.setPorBanca(fetchPorBancaInstituicao(scopeId));
                    stats.setPorCargo(fetchPorCargoInstituicao(scopeId));
                    stats.setPorAreaCargo(fetchPorAreaCargoInstituicao(scopeId));
                    break;
                case "CARGO":
                    stats.setPorBanca(fetchPorBancaCargo(scopeId));
                    stats.setPorAreaCargo(fetchPorAreaCargoCargo(scopeId));
                    stats.setPorAreaInstituicao(fetchPorAreaInstituicaoCargo(scopeId));
                    break;
            }
        }
        
        return stats;
    }

    public Map<Long, QuestaoEstatisticasConcursoCargoDto> buildBatchConcursoCargoStats(Long concursoCargoId, List<Long> subtemaIds, MetricsLevel metrics) {
        if (metrics == null || subtemaIds == null || subtemaIds.isEmpty()) return Map.of();

        Map<Long, QuestaoEstatisticasConcursoCargoDto> resultMap = new HashMap<>();
        boolean isFull = metrics == MetricsLevel.FULL;

        // Fetch totals
        List<Object[]> totals = questaoRepository.countQuestoesByConcursoCargoAndSubtemaIds(concursoCargoId, subtemaIds);
        for (Object[] row : totals) {
            Long subId = ((Number) row[0]).longValue();
            Long total = ((Number) row[1]).longValue();
            QuestaoEstatisticasConcursoCargoDto dto = new QuestaoEstatisticasConcursoCargoDto();
            dto.setTotalQuestoes(total);
            resultMap.put(subId, dto);
        }

        // Fetch respondidas/acertadas
        List<Object[]> resp = respostaRepository.countRespondidasByConcursoCargoAndSubtemaIds(concursoCargoId, subtemaIds);
        List<Object[]> acert = respostaRepository.countAcertadasByConcursoCargoAndSubtemaIds(concursoCargoId, subtemaIds);
        Map<Long, Long> acertMap = new HashMap<>();
        for (Object[] row : acert) acertMap.put(((Number) row[0]).longValue(), ((Number) row[1]).longValue());

        for (Object[] row : resp) {
            Long subId = ((Number) row[0]).longValue();
            Long respondidas = ((Number) row[1]).longValue();
            QuestaoEstatisticasConcursoCargoDto dto = resultMap.computeIfAbsent(subId, k -> new QuestaoEstatisticasConcursoCargoDto());
            dto.setRespondidas(respondidas);
            dto.setAcertadas(acertMap.getOrDefault(subId, 0L));
        }

        if (isFull) {
            // Fetch tempo
            List<Object[]> tempo = respostaRepository.avgTempoByConcursoCargoAndSubtemaIds(concursoCargoId, subtemaIds);
            for (Object[] row : tempo) {
                Long subId = ((Number) row[0]).longValue();
                Double avg = (Double) row[1];
                QuestaoEstatisticasConcursoCargoDto dto = resultMap.get(subId);
                if (dto != null) dto.setMediaTempoResposta(avg != null ? avg.intValue() : null);
            }

            // Fetch latest
            List<Object[]> latest = respostaRepository.findLatestResponseDatesByConcursoCargoAndSubtemaIds(concursoCargoId, subtemaIds);
            for (Object[] row : latest) {
                Long subId = ((Number) row[0]).longValue();
                LocalDateTime date = (LocalDateTime) row[1];
                QuestaoEstatisticasConcursoCargoDto dto = resultMap.get(subId);
                if (dto != null) dto.setUltimaQuestao(date);
            }

            // Fetch dificuldade
            List<Object[]> diff = respostaRepository.getDificuldadeStatsByConcursoCargoAndSubtemaIds(concursoCargoId, subtemaIds);
            for (Object[] row : diff) {
                Long subId = ((Number) row[0]).longValue();
                int dId = ((Number) row[1]).intValue();
                Long dTotal = ((Number) row[2]).longValue();
                Long dCorr = ((Number) row[3]).longValue();

                QuestaoEstatisticasConcursoCargoDto dto = resultMap.get(subId);
                if (dto != null) {
                    if (dto.getDificuldade() == null) dto.setDificuldade(new HashMap<>());
                    DificuldadeStatDto dDto = new DificuldadeStatDto();
                    dDto.setTotal(dTotal);
                    dDto.setCorretas(dCorr);
                    dto.getDificuldade().put(Dificuldade.fromId(dId).name(), dDto);
                }
            }
        }

        return resultMap;
    }

    private StatSliceDto fetchTotalStats(Long scopeId, String scopeType) {
        List<Long> ids = List.of(scopeId);
        List<Object[]> totalQ;
        List<Object[]> respA;
        List<Object[]> avgT;
        List<Object[]> latest;
        List<Object[]> diff;

        switch (scopeType) {
            case "DISCIPLINA":
                totalQ = questaoRepository.countQuestoesByDisciplinaIds(ids);
                respA = mergeRespAcert(respostaRepository.countRespondidasByDisciplinaIds(ids), respostaRepository.countAcertadasByDisciplinaIds(ids));
                avgT = respostaRepository.avgTempoByDisciplinaIds(ids);
                latest = respostaRepository.findLatestResponseDatesByDisciplinaIds(ids);
                diff = respostaRepository.getDificuldadeStatsByDisciplinaIds(ids);
                break;
            case "TEMA":
                totalQ = questaoRepository.countQuestoesByTemaIds(ids);
                respA = mergeRespAcert(respostaRepository.countRespondidasByTemaIds(ids), respostaRepository.countAcertadasByTemaIds(ids));
                avgT = respostaRepository.avgTempoByTemaIds(ids);
                latest = respostaRepository.findLatestResponseDatesByTemaIds(ids);
                diff = respostaRepository.getDificuldadeStatsByTemaIds(ids);
                break;
            case "SUBTEMA":
                totalQ = questaoRepository.countQuestoesBySubtemaIds(ids);
                respA = mergeRespAcert(respostaRepository.countRespondidasBySubtemaIds(ids), respostaRepository.countAcertadasBySubtemaIds(ids));
                avgT = respostaRepository.avgTempoBySubtemaIds(ids);
                latest = respostaRepository.findLatestResponseDatesBySubtemaIds(ids);
                diff = respostaRepository.getDificuldadeStatsBySubtemaIds(ids);
                break;
            case "BANCA":
                totalQ = questaoRepository.countQuestoesByBancaIds(ids);
                respA = mergeRespAcert(respostaRepository.countRespondidasByBancaIds(ids), respostaRepository.countAcertadasByBancaIds(ids));
                avgT = respostaRepository.avgTempoByBancaIds(ids);
                latest = respostaRepository.findLatestResponseDatesByBancaIds(ids);
                diff = respostaRepository.getDificuldadeStatsByBancaIds(ids);
                break;
            case "INSTITUICAO":
                totalQ = questaoRepository.countQuestoesByInstituicaoIds(ids);
                respA = mergeRespAcert(respostaRepository.countRespondidasByInstituicaoIds(ids), respostaRepository.countAcertadasByInstituicaoIds(ids));
                avgT = respostaRepository.avgTempoByInstituicaoIds(ids);
                latest = respostaRepository.findLatestResponseDatesByInstituicaoIds(ids);
                diff = respostaRepository.getDificuldadeStatsByInstituicaoIds(ids);
                break;
            case "CARGO":
                totalQ = questaoRepository.countQuestoesByCargoIds(ids);
                respA = mergeRespAcert(respostaRepository.countRespondidasByCargoIds(ids), respostaRepository.countAcertadasByCargoIds(ids));
                avgT = respostaRepository.avgTempoByCargoIds(ids);
                latest = respostaRepository.findLatestResponseDatesByCargoIds(ids);
                diff = respostaRepository.getDificuldadeStatsByCargoIds(ids);
                break;
            default:
                return new StatSliceDto();
        }

        Map<Long, StatSliceDto> map = assembleStats(totalQ, respA, avgT, false);
        StatSliceDto slice = map.getOrDefault(scopeId, new StatSliceDto());
        // Clear nome/id from total - they don't make sense at the aggregate level
        slice.setNome(null);
        slice.setId(null);

        // Add latest response date
        if (!latest.isEmpty()) {
            slice.setUltimaQuestao((java.time.LocalDateTime) latest.get(0)[1]);
        }

        // Add difficulty stats
        if (!diff.isEmpty()) {
            Map<String, DificuldadeStatDto> diffMap = new HashMap<>();
            for (Object[] row : diff) {
                int dId = ((Number) row[1]).intValue(); // This is COALESCE(r.dificuldade_id, 2)
                String dName = Dificuldade.fromId(dId).name();
                DificuldadeStatDto dDto = new DificuldadeStatDto();
                dDto.setTotal(((Number) row[2]).longValue());
                dDto.setCorretas(((Number) row[3]).longValue());
                diffMap.put(dName, dDto);
            }
            slice.setDificuldade(diffMap);
        }

        return slice;
    }

    private List<Object[]> mergeRespAcert(List<Object[]> resp, List<Object[]> acert) {
        Map<Object, Long> acertMap = new HashMap<>();
        for (Object[] row : acert) acertMap.put(row[0], ((Number) row[1]).longValue());

        List<Object[]> result = new ArrayList<>();
        for (Object[] row : resp) {
            result.add(new Object[]{row[0], row[1], acertMap.getOrDefault(row[0], 0L)});
        }
        return result;
    }

    private Long fetchAutoralCount(Long scopeId, String scopeType) {
        return switch (scopeType) {
            case "DISCIPLINA" -> questaoRepository.countAutoralQuestoesByDisciplinaId(scopeId);
            case "TEMA" -> questaoRepository.countAutoralQuestoesByTemaId(scopeId);
            case "SUBTEMA" -> questaoRepository.countAutoralQuestoesBySubtemaId(scopeId);
            default -> null;
        };
    }

    private List<Object[]> fetchAutoralRespondidas(Long scopeId, String scopeType) {
        return switch (scopeType) {
            case "DISCIPLINA" -> respostaRepository.countRespondidasByDisciplinaIdsAutoral(List.of(scopeId));
            case "TEMA" -> respostaRepository.countRespondidasByTemaIdsAutoral(List.of(scopeId));
            case "SUBTEMA" -> respostaRepository.countRespondidasBySubtemaIdsAutoral(List.of(scopeId));
            default -> List.of();
        };
    }

    private List<Object[]> fetchAutoralAcertadas(Long scopeId, String scopeType) {
        return switch (scopeType) {
            case "DISCIPLINA" -> respostaRepository.countAcertadasByDisciplinaIdsAutoral(List.of(scopeId));
            case "TEMA" -> respostaRepository.countAcertadasByTemaIdsAutoral(List.of(scopeId));
            case "SUBTEMA" -> respostaRepository.countAcertadasBySubtemaIdsAutoral(List.of(scopeId));
            default -> List.of();
        };
    }

    private List<Object[]> fetchAutoralTempo(Long scopeId, String scopeType) {
        return switch (scopeType) {
            case "DISCIPLINA" -> respostaRepository.avgTempoByDisciplinaIdsAutoral(List.of(scopeId));
            case "TEMA" -> respostaRepository.avgTempoByTemaIdsAutoral(List.of(scopeId));
            case "SUBTEMA" -> respostaRepository.avgTempoBySubtemaIdsAutoral(List.of(scopeId));
            default -> List.of();
        };
    }

    private List<Object[]> fetchAutoralLatest(Long scopeId, String scopeType) {
        return switch (scopeType) {
            case "DISCIPLINA" -> respostaRepository.findLatestResponseDatesByDisciplinaIdsAutoral(List.of(scopeId));
            case "TEMA" -> respostaRepository.findLatestResponseDatesByTemaIdsAutoral(List.of(scopeId));
            case "SUBTEMA" -> respostaRepository.findLatestResponseDatesBySubtemaIdsAutoral(List.of(scopeId));
            default -> List.of();
        };
    }

    private List<Object[]> fetchAutoralDificuldade(Long scopeId, String scopeType) {
        return switch (scopeType) {
            case "DISCIPLINA" -> respostaRepository.getDificuldadeStatsByDisciplinaIdsAutoral(List.of(scopeId));
            case "TEMA" -> respostaRepository.getDificuldadeStatsByTemaIdsAutoral(List.of(scopeId));
            case "SUBTEMA" -> respostaRepository.getDificuldadeStatsBySubtemaIdsAutoral(List.of(scopeId));
            default -> List.of();
        };
    }

    // --- Disciplina Methods ---
    private Map<String, StatSliceDto> fetchPorNivelDisciplina(Long id) {
        return assembleStats(questaoRepository.countQuestoesByDisciplinaIdGroupByNivel(id),
                respostaRepository.countRespondidasAcertadasByDisciplinaIdGroupByNivel(id),
                respostaRepository.avgTempoByDisciplinaIdGroupByNivel(id), true);
    }
    private Map<Long, StatSliceDto> fetchPorBancaDisciplina(Long id) {
        return enrichAndAssembleStatsWithEntityNames(questaoRepository.countQuestoesByDisciplinaIdGroupByBanca(id),
                respostaRepository.countRespondidasAcertadasByDisciplinaIdGroupByBanca(id),
                respostaRepository.avgTempoByDisciplinaIdGroupByBanca(id), "BANCA");
    }
    private Map<Long, StatSliceDto> fetchPorInstituicaoDisciplina(Long id) {
        return enrichAndAssembleStatsWithEntityNames(questaoRepository.countQuestoesByDisciplinaIdGroupByInstituicao(id),
                respostaRepository.countRespondidasAcertadasByDisciplinaIdGroupByInstituicao(id),
                respostaRepository.avgTempoByDisciplinaIdGroupByInstituicao(id), "INSTITUICAO");
    }
    private Map<String, StatSliceDto> fetchPorAreaInstituicaoDisciplina(Long id) {
        return assembleStats(questaoRepository.countQuestoesByDisciplinaIdGroupByAreaInstituicao(id),
                respostaRepository.countRespondidasAcertadasByDisciplinaIdGroupByAreaInstituicao(id),
                respostaRepository.avgTempoByDisciplinaIdGroupByAreaInstituicao(id), true);
    }
    private Map<Long, StatSliceDto> fetchPorCargoDisciplina(Long id) {
        return enrichAndAssembleStatsWithEntityNames(questaoRepository.countQuestoesByDisciplinaIdGroupByCargo(id),
                respostaRepository.countRespondidasAcertadasByDisciplinaIdGroupByCargo(id),
                respostaRepository.avgTempoByDisciplinaIdGroupByCargo(id), "CARGO");
    }
    private Map<String, StatSliceDto> fetchPorAreaCargoDisciplina(Long id) {
        return assembleStats(questaoRepository.countQuestoesByDisciplinaIdGroupByAreaCargo(id),
                respostaRepository.countRespondidasAcertadasByDisciplinaIdGroupByAreaCargo(id),
                respostaRepository.avgTempoByDisciplinaIdGroupByAreaCargo(id), true);
    }

    // --- Tema Methods ---
    private Map<String, StatSliceDto> fetchPorNivelTema(Long id) {
        return assembleStats(questaoRepository.countQuestoesByTemaIdGroupByNivel(id),
                respostaRepository.countRespondidasAcertadasByTemaIdGroupByNivel(id),
                respostaRepository.avgTempoByTemaIdGroupByNivel(id), true);
    }
    private Map<Long, StatSliceDto> fetchPorBancaTema(Long id) {
        return enrichAndAssembleStatsWithEntityNames(questaoRepository.countQuestoesByTemaIdGroupByBanca(id),
                respostaRepository.countRespondidasAcertadasByTemaIdGroupByBanca(id),
                respostaRepository.avgTempoByTemaIdGroupByBanca(id), "BANCA");
    }
    private Map<Long, StatSliceDto> fetchPorInstituicaoTema(Long id) {
        return enrichAndAssembleStatsWithEntityNames(questaoRepository.countQuestoesByTemaIdGroupByInstituicao(id),
                respostaRepository.countRespondidasAcertadasByTemaIdGroupByInstituicao(id),
                respostaRepository.avgTempoByTemaIdGroupByInstituicao(id), "INSTITUICAO");
    }
    private Map<String, StatSliceDto> fetchPorAreaInstituicaoTema(Long id) {
        return assembleStats(questaoRepository.countQuestoesByTemaIdGroupByAreaInstituicao(id),
                respostaRepository.countRespondidasAcertadasByTemaIdGroupByAreaInstituicao(id),
                respostaRepository.avgTempoByTemaIdGroupByAreaInstituicao(id), true);
    }
    private Map<Long, StatSliceDto> fetchPorCargoTema(Long id) {
        return enrichAndAssembleStatsWithEntityNames(questaoRepository.countQuestoesByTemaIdGroupByCargo(id),
                respostaRepository.countRespondidasAcertadasByTemaIdGroupByCargo(id),
                respostaRepository.avgTempoByTemaIdGroupByCargo(id), "CARGO");
    }
    private Map<String, StatSliceDto> fetchPorAreaCargoTema(Long id) {
        return assembleStats(questaoRepository.countQuestoesByTemaIdGroupByAreaCargo(id),
                respostaRepository.countRespondidasAcertadasByTemaIdGroupByAreaCargo(id),
                respostaRepository.avgTempoByTemaIdGroupByAreaCargo(id), true);
    }

    // --- Subtema Methods ---
    private Map<String, StatSliceDto> fetchPorNivelSubtema(Long id) {
        return assembleStats(questaoRepository.countQuestoesBySubtemaIdGroupByNivel(id),
                respostaRepository.countRespondidasAcertadasBySubtemaIdGroupByNivel(id),
                respostaRepository.avgTempoBySubtemaIdGroupByNivel(id), true);
    }
    private Map<Long, StatSliceDto> fetchPorBancaSubtema(Long id) {
        return enrichAndAssembleStatsWithEntityNames(questaoRepository.countQuestoesBySubtemaIdGroupByBanca(id),
                respostaRepository.countRespondidasAcertadasBySubtemaIdGroupByBanca(id),
                respostaRepository.avgTempoBySubtemaIdGroupByBanca(id), "BANCA");
    }
    private Map<Long, StatSliceDto> fetchPorInstituicaoSubtema(Long id) {
        return enrichAndAssembleStatsWithEntityNames(questaoRepository.countQuestoesBySubtemaIdGroupByInstituicao(id),
                respostaRepository.countRespondidasAcertadasBySubtemaIdGroupByInstituicao(id),
                respostaRepository.avgTempoBySubtemaIdGroupByInstituicao(id), "INSTITUICAO");
    }
    private Map<String, StatSliceDto> fetchPorAreaInstituicaoSubtema(Long id) {
        return assembleStats(questaoRepository.countQuestoesBySubtemaIdGroupByAreaInstituicao(id),
                respostaRepository.countRespondidasAcertadasBySubtemaIdGroupByAreaInstituicao(id),
                respostaRepository.avgTempoBySubtemaIdGroupByAreaInstituicao(id), true);
    }
    private Map<Long, StatSliceDto> fetchPorCargoSubtema(Long id) {
        return enrichAndAssembleStatsWithEntityNames(questaoRepository.countQuestoesBySubtemaIdGroupByCargo(id),
                respostaRepository.countRespondidasAcertadasBySubtemaIdGroupByCargo(id),
                respostaRepository.avgTempoBySubtemaIdGroupByCargo(id), "CARGO");
    }
    private Map<String, StatSliceDto> fetchPorAreaCargoSubtema(Long id) {
        return assembleStats(questaoRepository.countQuestoesBySubtemaIdGroupByAreaCargo(id),
                respostaRepository.countRespondidasAcertadasBySubtemaIdGroupByAreaCargo(id),
                respostaRepository.avgTempoBySubtemaIdGroupByAreaCargo(id), true);
    }

    // --- Banca Methods ---
    private Map<String, StatSliceDto> fetchPorNivelBanca(Long id) {
        return assembleStats(questaoRepository.countQuestoesByBancaIdGroupByNivel(id),
                respostaRepository.countRespondidasAcertadasByBancaIdGroupByNivel(id),
                respostaRepository.avgTempoByBancaIdGroupByNivel(id), true);
    }
    private Map<String, StatSliceDto> fetchPorAreaInstituicaoBanca(Long id) {
        return assembleStats(questaoRepository.countQuestoesByBancaIdGroupByAreaInstituicao(id),
                respostaRepository.countRespondidasAcertadasByBancaIdGroupByAreaInstituicao(id),
                respostaRepository.avgTempoByBancaIdGroupByAreaInstituicao(id), true);
    }
    private Map<String, StatSliceDto> fetchPorAreaCargoBanca(Long id) {
        return assembleStats(questaoRepository.countQuestoesByBancaIdGroupByAreaCargo(id),
                respostaRepository.countRespondidasAcertadasByBancaIdGroupByAreaCargo(id),
                respostaRepository.avgTempoByBancaIdGroupByAreaCargo(id), true);
    }

    // --- Instituicao Methods ---
    private Map<String, StatSliceDto> fetchPorNivelInstituicao(Long id) {
        return assembleStats(questaoRepository.countQuestoesByInstituicaoIdGroupByNivel(id),
                respostaRepository.countRespondidasAcertadasByInstituicaoIdGroupByNivel(id),
                respostaRepository.avgTempoByInstituicaoIdGroupByNivel(id), true);
    }
    private Map<Long, StatSliceDto> fetchPorBancaInstituicao(Long id) {
        return enrichAndAssembleStatsWithEntityNames(
                questaoRepository.countQuestoesByInstituicaoIdGroupByBanca(id),
                respostaRepository.countRespondidasAcertadasByInstituicaoIdGroupByBanca(id),
                respostaRepository.avgTempoByInstituicaoIdGroupByBanca(id),
                "BANCA");
    }
    private Map<Long, StatSliceDto> fetchPorCargoInstituicao(Long id) {
        return enrichAndAssembleStatsWithEntityNames(
                questaoRepository.countQuestoesByInstituicaoIdGroupByCargo(id),
                respostaRepository.countRespondidasAcertadasByInstituicaoIdGroupByCargo(id),
                respostaRepository.avgTempoByInstituicaoIdGroupByCargo(id),
                "CARGO");
    }
    private Map<String, StatSliceDto> fetchPorAreaCargoInstituicao(Long id) {
        return assembleStats(questaoRepository.countQuestoesByInstituicaoIdGroupByAreaCargo(id),
                respostaRepository.countRespondidasAcertadasByInstituicaoIdGroupByAreaCargo(id),
                respostaRepository.avgTempoByInstituicaoIdGroupByAreaCargo(id), true);
    }

    // --- Cargo Methods ---
    private Map<String, StatSliceDto> fetchPorNivelCargo(Long id) {
        return assembleStats(questaoRepository.countQuestoesByCargoIdGroupByNivel(id),
                respostaRepository.countRespondidasAcertadasByCargoIdGroupByNivel(id),
                respostaRepository.avgTempoByCargoIdGroupByNivel(id), true);
    }
    private Map<Long, StatSliceDto> fetchPorBancaCargo(Long id) {
        return enrichAndAssembleStatsWithEntityNames(
                questaoRepository.countQuestoesByCargoIdGroupByBanca(id),
                respostaRepository.countRespondidasAcertadasByCargoIdGroupByBanca(id),
                respostaRepository.avgTempoByCargoIdGroupByBanca(id),
                "BANCA");
    }
    private Map<String, StatSliceDto> fetchPorAreaCargoCargo(Long id) {
        return assembleStats(questaoRepository.countQuestoesByCargoIdGroupByAreaCargo(id),
                respostaRepository.countRespondidasAcertadasByCargoIdGroupByAreaCargo(id),
                respostaRepository.avgTempoByCargoIdGroupByAreaCargo(id), true);
    }
    private Map<String, StatSliceDto> fetchPorAreaInstituicaoCargo(Long id) {
        return assembleStats(questaoRepository.countQuestoesByCargoIdGroupByAreaInstituicao(id),
                respostaRepository.countRespondidasAcertadasByCargoIdGroupByAreaInstituicao(id),
                respostaRepository.avgTempoByCargoIdGroupByAreaInstituicao(id), true);
    }

    // --- Disciplina/Tema/Subtema porBanca, porInstituicao, porCargo methods with name enrichment ---
    private Map<Long, StatSliceDto> enrichAndAssembleStatsWithEntityNames(List<Object[]> questoes, List<Object[]> respostas, List<Object[]> avgTempo, String entityType) {
        Map<Long, StatSliceDto> resultMap = new HashMap<>();

        for (Object[] row : questoes) {
            Long key = ((Number) row[0]).longValue();
            Long total = ((Number) row[1]).longValue();

            StatSliceDto slice = resultMap.computeIfAbsent(key, k -> {
                StatSliceDto s = new StatSliceDto();
                s.setId(k);
                s.setNome(getEntityName(entityType, k));
                s.setTotalQuestoes(0L);
                s.setRespondidas(0L);
                s.setAcertadas(0L);
                return s;
            });
            slice.setTotalQuestoes(total);
        }

        for (Object[] row : respostas) {
            Long key = ((Number) row[0]).longValue();
            Long respondidas = ((Number) row[1]).longValue();
            Long acertadas = ((Number) row[2]).longValue();

            StatSliceDto slice = resultMap.computeIfAbsent(key, k -> {
                StatSliceDto s = new StatSliceDto();
                s.setId(k);
                s.setNome(getEntityName(entityType, k));
                s.setTotalQuestoes(0L);
                return s;
            });
            slice.setRespondidas(respondidas);
            slice.setAcertadas(acertadas);
        }

        for (Object[] row : avgTempo) {
            Long key = ((Number) row[0]).longValue();
            Double avg = (Double) row[1];

            StatSliceDto slice = resultMap.get(key);
            if (slice != null) {
                slice.setMediaTempoResposta(avg != null ? avg.intValue() : null);
            }
        }
        return resultMap;
    }

    private String getEntityName(String entityType, Long id) {
        if (id == null) return null;
        return switch (entityType) {
            case "BANCA" -> bancaRepository.findById(id).map(b -> b.getNome()).orElse(id.toString());
            case "INSTITUICAO" -> instituicaoRepository.findById(id).map(i -> i.getNome()).orElse(id.toString());
            case "CARGO" -> cargoRepository.findById(id).map(c -> c.getNome()).orElse(id.toString());
            default -> id.toString();
        };
    }

    // Generic assembler
    private <K> Map<K, StatSliceDto> assembleStats(List<Object[]> questoes, List<Object[]> respostas, List<Object[]> avgTempo, boolean isStringKey) {
        Map<K, StatSliceDto> resultMap = new HashMap<>();

        for (Object[] row : questoes) {
            K key = (K) row[0];
            Long total = ((Number) row[1]).longValue();

            StatSliceDto slice = resultMap.computeIfAbsent(key, k -> {
                StatSliceDto s = new StatSliceDto();
                if (key instanceof Long) s.setId((Long) key);
                s.setNome(key.toString());
                s.setTotalQuestoes(0L);
                s.setRespondidas(0L);
                s.setAcertadas(0L);
                return s;
            });
            slice.setTotalQuestoes(total);
        }

        for (Object[] row : respostas) {
            K key = (K) row[0];
            Long respondidas = ((Number) row[1]).longValue();
            Long acertadas = ((Number) row[2]).longValue();

            StatSliceDto slice = resultMap.computeIfAbsent(key, k -> {
                StatSliceDto s = new StatSliceDto();
                if (key instanceof Long) s.setId((Long) key);
                s.setNome(key.toString());
                s.setTotalQuestoes(0L);
                return s;
            });
            slice.setRespondidas(respondidas);
            slice.setAcertadas(acertadas);
        }

        for (Object[] row : avgTempo) {
            K key = (K) row[0];
            Double avg = (Double) row[1];

            StatSliceDto slice = resultMap.get(key);
            if (slice != null) {
                slice.setMediaTempoResposta(avg != null ? avg.intValue() : null);
            }
        }
        return resultMap;
    }
}
