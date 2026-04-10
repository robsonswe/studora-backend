package com.studora.service;

import com.studora.dto.DificuldadeStatDto;
import com.studora.dto.MetricsLevel;
import com.studora.dto.QuestaoStatsDto;
import com.studora.dto.StatSliceDto;
import com.studora.entity.Dificuldade;
import com.studora.repository.BancaRepository;
import com.studora.repository.CargoRepository;
import com.studora.repository.InstituicaoRepository;
import com.studora.repository.QuestaoRepository;
import com.studora.repository.RespostaRepository;
import org.springframework.stereotype.Component;

import java.util.*;

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
