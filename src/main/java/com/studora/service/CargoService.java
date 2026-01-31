package com.studora.service;

import com.studora.dto.CargoDto;
import com.studora.entity.Cargo;
import com.studora.entity.NivelCargo;
import com.studora.exception.ConflictException;
import com.studora.exception.ResourceNotFoundException;
import com.studora.repository.CargoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CargoService {

    @Autowired
    private CargoRepository cargoRepository;

    @Autowired
    private com.studora.repository.ConcursoCargoRepository concursoCargoRepository;

    public List<CargoDto> findAll() {
        return cargoRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public CargoDto findById(Long id) {
        Cargo cargo = cargoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cargo", "ID", id));
        return convertToDto(cargo);
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
            cargo.setNome(cargoDto.getNome());
            cargo.setNivel(cargoDto.getNivel());
            cargo.setArea(cargoDto.getArea());
        } else {
            cargo = convertToEntity(cargoDto);
        }
        return convertToDto(cargoRepository.save(cargo));
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

    private CargoDto convertToDto(Cargo cargo) {
        CargoDto cargoDto = new CargoDto();
        cargoDto.setId(cargo.getId());
        cargoDto.setNome(cargo.getNome());
        cargoDto.setNivel(cargo.getNivel());
        cargoDto.setArea(cargo.getArea());
        return cargoDto;
    }

    private Cargo convertToEntity(CargoDto cargoDto) {
        Cargo cargo = new Cargo();
        cargo.setId(cargoDto.getId());
        cargo.setNome(cargoDto.getNome());
        cargo.setNivel(cargoDto.getNivel());
        cargo.setArea(cargoDto.getArea());
        return cargo;
    }
}
