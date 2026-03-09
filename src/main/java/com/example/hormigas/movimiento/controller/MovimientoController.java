package com.example.hormigas.movimiento.controller;

import com.example.hormigas.movimiento.dto.CrearMovimientoDTO;
import com.example.hormigas.movimiento.dto.MovimientoResponseDTO;
import com.example.hormigas.movimiento.service.MovimientoService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/movimiento")
public class MovimientoController {
    private MovimientoService movimientoService;

    public MovimientoController (
            MovimientoService movimientoService
    ) {
        this.movimientoService = movimientoService;
    }


    @GetMapping("/nuevo_movimiento/{usuarioId}")
    public MovimientoResponseDTO createMovimiento (@RequestBody CrearMovimientoDTO dto, @PathVariable Long usuarioId) {
        return movimientoService.registrarMovimiento(dto, usuarioId);
    }
}
