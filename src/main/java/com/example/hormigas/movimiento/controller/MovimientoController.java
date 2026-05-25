package com.example.hormigas.movimiento.controller;

import com.example.hormigas.movimiento.dto.CrearMovimientoDTO;
import com.example.hormigas.movimiento.dto.MovimientoFiltroDTO;
import com.example.hormigas.movimiento.dto.MovimientoResponseDTO;
import com.example.hormigas.movimiento.dto.VentaBatchDTO;
import com.example.hormigas.movimiento.entity.TipoMovimiento;
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

    @PostMapping("/venta/batch")
    public List<MovimientoResponseDTO> registrarVentaBatch(@RequestBody VentaBatchDTO dto) {
        return movimientoService.registrarVentaBatch(dto);
    }

    @GetMapping("/buscar")
    public List<MovimientoResponseDTO> obtenerMovimientos(
            @RequestParam(required = false) Long sucursalId,
            @RequestParam(required = false) Long productoId,
            @RequestParam(required = false) Long inventarioId,
            @RequestParam(required = false) TipoMovimiento tipo
            ) {
        MovimientoFiltroDTO filtro = new MovimientoFiltroDTO(sucursalId, productoId, inventarioId, tipo);
        return movimientoService.obtenerMovimientos(filtro);
    }

}
