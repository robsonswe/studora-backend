package com.studora.service;

import com.studora.dto.analytics.*;
import com.studora.entity.*;
import com.studora.exception.ResourceNotFoundException;
import com.studora.repository.DisciplinaRepository;
import com.studora.repository.RespostaRepository;
import com.studora.repository.SubtemaRepository;
import com.studora.repository.TemaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.IsoFields;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final RespostaRepository respostaRepository;
    private final DisciplinaRepository disciplinaRepository;
    private final TemaRepository temaRepository;
    private final SubtemaRepository subtemaRepository;

    @Transactional(readOnly = true)
    public List<ConsistencyDto> getConsistencia(int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        List<Resposta> responses = respostaRepository.findAllWithFullDetailsSince(since);

        Map<LocalDate, List<Resposta>> byDate = responses.stream()
                .collect(Collectors.groupingBy(r -> r.getCreatedAt().toLocalDate()));

        List<ConsistencyDto> result = new ArrayList<>();
        
        // Fill gaps and calculate streaks
        LocalDate start = since.toLocalDate();
        LocalDate end = LocalDate.now();
        int currentStreak = 0;

        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            List<Resposta> dayResponses = byDate.getOrDefault(date, Collections.emptyList());
            
            int total = dayResponses.size();
            int correct = (int) dayResponses.stream().filter(this::isCorrect).count();
            int time = dayResponses.stream()
                    .mapToInt(r -> r.getTempoRespostaSegundos() != null ? r.getTempoRespostaSegundos() : 0)
                    .sum();

            if (total > 0) {
                currentStreak++;
            } else {
                currentStreak = 0;
            }

            result.add(ConsistencyDto.builder()
                    .date(date)
                    .totalAnswered(total)
                    .totalCorrect(correct)
                    .totalTimeSeconds(time)
                    .activeStreak(currentStreak)
                    .build());
        }

        return result;
    }

    @Transactional(readOnly = true)
    public List<TopicMasteryDto> getDisciplinasMastery() {
        List<Resposta> allResponses = respostaRepository.findAllWithFullDetails();
        return calculateMasteryForDisciplinas(allResponses).stream()
                .filter(m -> m.getTotalAttempts() > 0)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TopicMasteryDto getDisciplinaMasteryDetail(Long id) {
        Disciplina disciplina = disciplinaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Disciplina", "ID", id));

        List<Resposta> allResponses = respostaRepository.findAllWithFullDetails();
        
        // Filter responses for this disciplina
        List<Resposta> discResponses = allResponses.stream()
                .filter(r -> r.getQuestao().getSubtemas().stream()
                        .anyMatch(s -> s.getTema().getDisciplina().getId().equals(id)))
                .toList();

        TopicMasteryDto dto = buildMasteryDto(disciplina.getId(), disciplina.getNome(), discResponses);
        
        // Calculate Themes for this Disciplina
        List<TopicMasteryDto> temas = calculateMasteryForTemas(allResponses, id).stream()
                .filter(t -> t.getTotalAttempts() > 0)
                .peek(temaDto -> {
                    // For each theme, calculate its subthemes
                    List<TopicMasteryDto> subtemas = calculateMasteryForSubtemas(allResponses, temaDto.getId()).stream()
                            .filter(s -> s.getTotalAttempts() > 0)
                            .collect(Collectors.toList());
                    temaDto.setChildren(subtemas);
                })
                .collect(Collectors.toList());
        
        dto.setChildren(temas);
        return dto;
    }

    @Transactional(readOnly = true)
    public List<EvolutionDto> getEvolucao() {
        List<Resposta> responses = respostaRepository.findAllWithFullDetails();
        
        // Group by Year-Week
        Map<String, List<Resposta>> byWeek = responses.stream()
                .collect(Collectors.groupingBy(r -> {
                    LocalDateTime dt = r.getCreatedAt();
                    return dt.getYear() + "-W" + String.format("%02d", dt.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR));
                }, TreeMap::new, Collectors.toList()));

        return byWeek.entrySet().stream().map(entry -> {
            List<Resposta> weekResponses = entry.getValue();
            int total = weekResponses.size();
            long correct = weekResponses.stream().filter(this::isCorrect).count();
            double accuracy = total > 0 ? (double) correct / total : 0.0;
            
            double avgTime = weekResponses.stream()
                    .mapToInt(r -> r.getTempoRespostaSegundos() != null ? r.getTempoRespostaSegundos() : 0)
                    .average().orElse(0.0);

            Map<String, Double> dist = weekResponses.stream()
                    .collect(Collectors.groupingBy(r -> r.getDificuldade() != null ? r.getDificuldade().name() : "MEDIA",
                            Collectors.collectingAndThen(Collectors.counting(), count -> (double) count / total)));

            return EvolutionDto.builder()
                    .period(entry.getKey())
                    .overallAccuracy(accuracy)
                    .avgResponseTime((int) avgTime)
                    .difficultyDistribution(dist)
                    .build();
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public LearningRateDto getTaxaAprendizado() {
        List<Resposta> all = respostaRepository.findAllWithFullDetails();
        
        // Group by question
        Map<Long, List<Resposta>> byQuestao = all.stream()
                .collect(Collectors.groupingBy(r -> r.getQuestao().getId()));

        List<List<Resposta>> repeated = byQuestao.values().stream()
                .filter(list -> list.size() > 1)
                .toList();

        int totalRepeated = repeated.size();
        if (totalRepeated == 0) return new LearningRateDto(0, 0.0, 0.0, Collections.emptyList());

        int recoveryAttempts = 0;
        int recoverySuccesses = 0;
        int retentionAttempts = 0;
        int retentionSuccesses = 0;

        Map<Integer, List<Boolean>> accuracyByAttempt = new HashMap<>();

        for (List<Resposta> attempts : repeated) {
            // Assumes attempts are sorted by createdAt ASC due to findAllWithFullDetails ORDER BY
            for (int i = 0; i < attempts.size(); i++) {
                Resposta current = attempts.get(i);
                boolean currentCorrect = isCorrect(current);
                
                accuracyByAttempt.computeIfAbsent(i + 1, k -> new ArrayList<>()).add(currentCorrect);

                if (i > 0) {
                    Resposta previous = attempts.get(i - 1);
                    boolean prevCorrect = isCorrect(previous);

                    if (!prevCorrect) {
                        recoveryAttempts++;
                        if (currentCorrect) recoverySuccesses++;
                    } else {
                        retentionAttempts++;
                        if (currentCorrect) retentionSuccesses++;
                    }
                }
            }
        }

        List<LearningRateDto.AttemptData> data = accuracyByAttempt.entrySet().stream()
                .map(e -> {
                    long correct = e.getValue().stream().filter(b -> b).count();
                    return new LearningRateDto.AttemptData(e.getKey(), (double) correct / e.getValue().size());
                })
                .sorted(Comparator.comparing(LearningRateDto.AttemptData::getAttemptNumber))
                .toList();

        return LearningRateDto.builder()
                .totalRepeatedQuestions(totalRepeated)
                .recoveryRate(recoveryAttempts > 0 ? (double) recoverySuccesses / recoveryAttempts : 0.0)
                .retentionRate(retentionAttempts > 0 ? (double) retentionSuccesses / retentionAttempts : 0.0)
                .data(data)
                .build();
    }

    private List<TopicMasteryDto> calculateMasteryForDisciplinas(List<Resposta> responses) {
        Map<Long, List<Resposta>> byDisc = responses.stream()
                .flatMap(r -> r.getQuestao().getSubtemas().stream().map(s -> new AbstractMap.SimpleEntry<>(s.getTema().getDisciplina().getId(), r)))
                .collect(Collectors.groupingBy(Map.Entry::getKey, Collectors.mapping(Map.Entry::getValue, Collectors.toList())));

        return disciplinaRepository.findAll().stream()
                .map(d -> buildMasteryDto(d.getId(), d.getNome(), byDisc.getOrDefault(d.getId(), Collections.emptyList())))
                .collect(Collectors.toList());
    }

    private List<TopicMasteryDto> calculateMasteryForTemas(List<Resposta> responses, Long disciplinaId) {
        Map<Long, List<Resposta>> byTema = responses.stream()
                .flatMap(r -> r.getQuestao().getSubtemas().stream()
                        .filter(s -> disciplinaId == null || s.getTema().getDisciplina().getId().equals(disciplinaId))
                        .map(s -> new AbstractMap.SimpleEntry<>(s.getTema().getId(), r)))
                .collect(Collectors.groupingBy(Map.Entry::getKey, Collectors.mapping(Map.Entry::getValue, Collectors.toList())));

        List<Tema> temas = (disciplinaId != null) ? temaRepository.findByDisciplinaId(disciplinaId) : temaRepository.findAll();
        
        return temas.stream()
                .map(t -> buildMasteryDto(t.getId(), t.getNome(), byTema.getOrDefault(t.getId(), Collections.emptyList())))
                .collect(Collectors.toList());
    }

    private List<TopicMasteryDto> calculateMasteryForSubtemas(List<Resposta> responses, Long temaId) {
        Map<Long, List<Resposta>> bySubtema = responses.stream()
                .flatMap(r -> r.getQuestao().getSubtemas().stream()
                        .filter(s -> temaId == null || s.getTema().getId().equals(temaId))
                        .map(s -> new AbstractMap.SimpleEntry<>(s.getId(), r)))
                .collect(Collectors.groupingBy(Map.Entry::getKey, Collectors.mapping(Map.Entry::getValue, Collectors.toList())));

        List<Subtema> subtemas = (temaId != null) ? subtemaRepository.findByTemaId(temaId) : subtemaRepository.findAll();

        return subtemas.stream()
                .map(s -> buildMasteryDto(s.getId(), s.getNome(), bySubtema.getOrDefault(s.getId(), Collections.emptyList())))
                .collect(Collectors.toList());
    }

    private TopicMasteryDto buildMasteryDto(Long id, String nome, List<Resposta> topicResponses) {
        int total = topicResponses.size();
        int correct = (int) topicResponses.stream().filter(this::isCorrect).count();
        int guesses = (int) topicResponses.stream().filter(r -> r.getDificuldade() == Dificuldade.CHUTE).count();
        double avgTime = topicResponses.stream()
                .mapToInt(r -> r.getTempoRespostaSegundos() != null ? r.getTempoRespostaSegundos() : 0)
                .average().orElse(0.0);

        Map<String, TopicMasteryDto.DifficultyStat> diffStats = new HashMap<>();
        for (Dificuldade d : Dificuldade.values()) {
            if (d == Dificuldade.CHUTE) continue;
            List<Resposta> filtered = topicResponses.stream().filter(r -> r.getDificuldade() == d).toList();
            if (!filtered.isEmpty()) {
                long c = filtered.stream().filter(this::isCorrect).count();
                diffStats.put(d.name(), new TopicMasteryDto.DifficultyStat(filtered.size(), (int) c));
            } else {
                diffStats.put(d.name(), new TopicMasteryDto.DifficultyStat(0, 0));
            }
        }

        return TopicMasteryDto.builder()
                .id(id)
                .nome(nome)
                .totalAttempts(total)
                .correctAttempts(correct)
                .avgTimeSeconds((int) avgTime)
                .guessCount(guesses)
                .difficultyStats(diffStats)
                .masteryScore(calculateMasteryScore(topicResponses))
                .build();
    }

    private double calculateMasteryScore(List<Resposta> responses) {
        if (responses.isEmpty()) return 0.0;
        
        double weightedPoints = 0;
        double maxPossiblePoints = 0;

        for (Resposta r : responses) {
            double weight = 1.0;
            if (r.getDificuldade() == Dificuldade.MEDIA) weight = 2.0;
            if (r.getDificuldade() == Dificuldade.DIFICIL) weight = 3.0;
            
            maxPossiblePoints += weight;
            
            if (isCorrect(r)) {
                if (r.getDificuldade() == Dificuldade.CHUTE) {
                    weightedPoints += 0.5; // Guess only gives half point
                } else {
                    weightedPoints += weight;
                }
            }
        }

        return (weightedPoints / maxPossiblePoints) * 100.0;
    }

    private boolean isCorrect(Resposta r) {
        if (r.getAlternativaEscolhida() == null) return false;
        return Boolean.TRUE.equals(r.getAlternativaEscolhida().getCorreta());
    }
}
