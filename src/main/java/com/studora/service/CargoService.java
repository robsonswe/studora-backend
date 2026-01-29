package com.studora.service;

import com.studora.dto.CargoDto;
import com.studora.entity.Cargo;
import com.studora.repository.CargoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CargoService {

    @Autowired
    private CargoRepository cargoRepository;

    public List<CargoDto> findAll() {
        return cargoRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public CargoDto findById(Long id) {
        return cargoRepository.findById(id)
                .map(this::convertToDto)
                .orElse(null);
    }

    public CargoDto save(CargoDto cargoDto) {
        Cargo cargo = convertToEntity(cargoDto);
        return convertToDto(cargoRepository.save(cargo));
    }

    public void deleteById(Long id) {
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
