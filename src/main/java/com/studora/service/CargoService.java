package com.studora.service;

import com.studora.dto.cargo.CargoDetailDto;
import com.studora.dto.cargo.CargoSummaryDto;
import com.studora.dto.request.CargoCreateRequest;
import com.studora.dto.request.CargoUpdateRequest;
import com.studora.entity.Cargo;
import com.studora.exception.ConflictException;
import com.studora.exception.ResourceNotFoundException;
import com.studora.exception.ValidationException;
import com.studora.mapper.CargoMapper;
import com.studora.repository.CargoRepository;
import com.studora.repository.ConcursoCargoRepository;
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
public class CargoService {

    private final CargoRepository cargoRepository;
    private final CargoMapper cargoMapper;
    private final ConcursoCargoRepository concursoCargoRepository;

    @Transactional(readOnly = true)
    public Page<CargoSummaryDto> findAll(String nome, Pageable pageable) {
        if (nome != null && !nome.isBlank()) {
            return cargoRepository.findByNomeContainingIgnoreCase(nome, pageable)
                    .map(cargoMapper::toSummaryDto);
        }
        return cargoRepository.findAll(pageable)
                .map(cargoMapper::toSummaryDto);
    }

    @Transactional(readOnly = true)
    public CargoDetailDto getCargoDetailById(Long id) {
        Cargo cargo = cargoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cargo", "ID", id));
        return cargoMapper.toDetailDto(cargo);
    }

    public CargoDetailDto create(CargoCreateRequest request) {
        log.info("Criando novo cargo: {} ({})", request.getNome(), request.getNivel());
        
        Optional<Cargo> existing = cargoRepository.findByNomeAndNivelAndArea(
                request.getNome(), request.getNivel(), request.getArea());
        
        if (existing.isPresent()) {
            throw new ConflictException(String.format("Já existe um cargo com o nome '%s', nível '%s' e área '%s'", 
                    request.getNome(), request.getNivel(), request.getArea()));
        }

        Cargo cargo = cargoMapper.toEntity(request);
        return cargoMapper.toDetailDto(cargoRepository.save(cargo));
    }

    public CargoDetailDto update(Long id, CargoUpdateRequest request) {
        log.info("Atualizando cargo ID: {}", id);
        
        Cargo cargo = cargoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cargo", "ID", id));

        if (request.getNome() != null || request.getNivel() != null || request.getArea() != null) {
            String nome = request.getNome() != null ? request.getNome() : cargo.getNome();
            com.studora.entity.NivelCargo nivel = request.getNivel() != null ? request.getNivel() : cargo.getNivel();
            String area = request.getArea() != null ? request.getArea() : cargo.getArea();

            Optional<Cargo> existing = cargoRepository.findByNomeAndNivelAndArea(nome, nivel, area);
            if (existing.isPresent() && !existing.get().getId().equals(id)) {
                throw new ConflictException(String.format("Já existe um cargo com o nome '%s', nível '%s' e área '%s'", 
                        nome, nivel, area));
            }
        }

        cargoMapper.updateEntityFromDto(request, cargo);
        return cargoMapper.toDetailDto(cargoRepository.save(cargo));
    }

    public void delete(Long id) {
        log.info("Excluindo cargo ID: {}", id);
        if (!cargoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Cargo", "ID", id);
        }
        
        if (concursoCargoRepository.existsByCargoId(id)) {
            throw new ValidationException("Não é possível excluir um cargo que possui concursos associados");
        }
        
        cargoRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<String> findAllAreas(String query) {
        if (query != null && !query.isBlank()) {
            return cargoRepository.findDistinctAreas(query);
        }
        return cargoRepository.findDistinctAreas();
    }
}
