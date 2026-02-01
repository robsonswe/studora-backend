package com.studora.service;

import com.studora.dto.CargoDto;
import com.studora.entity.Cargo;
import com.studora.entity.NivelCargo;
import com.studora.exception.ConflictException;
import com.studora.exception.ResourceNotFoundException;
import com.studora.mapper.CargoMapper;
import com.studora.repository.CargoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CargoService {

    private final CargoRepository cargoRepository;
    private final CargoMapper cargoMapper;
    private final com.studora.repository.ConcursoCargoRepository concursoCargoRepository;

    public Page<CargoDto> findAll(Pageable pageable) {
        return cargoRepository.findAll(pageable)
                .map(cargoMapper::toDto);
    }

    public CargoDto findById(Long id) {
        Cargo cargo = cargoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cargo", "ID", id));
        return cargoMapper.toDto(cargo);
    }

    public CargoDto save(CargoDto cargoDto) {
        // Check for duplicate cargo (same name, nivel, and area) with case-insensitive comparison
        Optional<Cargo> existingCargo = cargoRepository.findByNomeAndNivelAndArea(cargoDto.getNome(), cargoDto.getNivel(), cargoDto.getArea());
        if (existingCargo.isPresent() && !existingCargo.get().getId().equals(cargoDto.getId())) {
            throw new ConflictException("Já existe um cargo com o nome '" + cargoDto.getNome() + "', nível '" + cargoDto.getNivel() + "' e área '" + cargoDto.getArea() + "'");
        }

        Cargo cargo;
        if (cargoDto.getId() != null) {
            cargo = cargoRepository.findById(cargoDto.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Cargo", "ID", cargoDto.getId()));
            cargoMapper.updateEntityFromDto(cargoDto, cargo);
        } else {
            cargo = cargoMapper.toEntity(cargoDto);
        }
        return cargoMapper.toDto(cargoRepository.save(cargo));
    }

    public void deleteById(Long id) {
        if (!cargoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Cargo", "ID", id);
        }
        if (concursoCargoRepository.existsByCargoId(id)) {
            throw new com.studora.exception.ConflictException("Não é possível excluir o cargo pois existem concursos associados a ele.");
        }
        cargoRepository.deleteById(id);
    }
}
