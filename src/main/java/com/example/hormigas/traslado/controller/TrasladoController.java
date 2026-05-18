package com.example.hormigas.traslado.controller;

import com.example.hormigas.traslado.dto.CrearTrasladoDTO;
import com.example.hormigas.traslado.dto.TrasladoResponseDTO;
import com.example.hormigas.traslado.service.TrasladoService;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/traslado")
@Validated
public class TrasladoController {

    private final TrasladoService trasladoService;

    public TrasladoController(TrasladoService trasladoService) {
        this.trasladoService = trasladoService;
    }

    @PostMapping("/crear")
    public TrasladoResponseDTO crear(@Valid @RequestBody CrearTrasladoDTO dto) {
        return trasladoService.crear(dto);
    }
}
