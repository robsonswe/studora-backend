package com.studora.controller;

import com.studora.dto.CargoDto;
import com.studora.service.CargoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cargos")
public class CargoController {

    @Autowired
    private CargoService cargoService;

    @GetMapping
    public List<CargoDto> getAllCargos() {
        return cargoService.findAll();
    }

    @GetMapping("/{id}")
    public CargoDto getCargoById(@PathVariable Long id) {
        return cargoService.findById(id);
    }

    @PostMapping
    public CargoDto createCargo(@RequestBody CargoDto cargoDto) {
        return cargoService.save(cargoDto);
    }

    @PutMapping("/{id}")
    public CargoDto updateCargo(@PathVariable Long id, @RequestBody CargoDto cargoDto) {
        cargoDto.setId(id);
        return cargoService.save(cargoDto);
    }

    @DeleteMapping("/{id}")
    public void deleteCargo(@PathVariable Long id) {
        cargoService.deleteById(id);
    }
}
