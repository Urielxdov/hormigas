package com.example.hormigas.movimiento.controller;

import com.example.hormigas.movimiento.dto.CrearMovimientoDTO;
import com.example.hormigas.movimiento.dto.MovimientoResponseDTO;
import com.example.hormigas.movimiento.service.MovimientoService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/movimiento")
public class MovimientoController {
    private final MovimientoService movimientoService;

    public MovimientoController (
            MovimientoService movimientoService
    ) {
        this.movimientoService = movimientoService;
    }


    @PostMapping("/crear")
    public MovimientoResponseDTO createMovimiento (@RequestBody CrearMovimientoDTO dto) {
        return movimientoService.registrarMovimiento(dto);
    }

    @GetMapping("/")
    public List<MovimientoResponseDTO> obtenerMovimientos() {
        return movimientoService.obtenerMovimientos();
    }

    @GetMapping("/{id}")
    public MovimientoResponseDTO obtenerMovimiento(@PathVariable Long id) {
        return movimientoService.obtenerMovimiento(id);
    }

    @GetMapping("/producto/{productoId}")
    public List<MovimientoResponseDTO> obtenerMovimientosProducto(@PathVariable Long productoId) {
        return movimientoService.obtenerMovimientosProducto(productoId);
    }

    @GetMapping("/sucursal/{sucursalId}")
    public List<MovimientoResponseDTO> obtenerMovimientosSucursal(@PathVariable Long sucursalId) {
        return movimientoService.obtenerMovimientos(sucursalId);
    }
}
