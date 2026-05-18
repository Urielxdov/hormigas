package com.example.hormigas.reporte.controller;

import com.example.hormigas.movimiento.dto.MovimientoResponseDTO;
import com.example.hormigas.reporte.dto.ProductoTopDTO;
import com.example.hormigas.reporte.dto.ValorInventarioDTO;
import com.example.hormigas.reporte.service.ReporteService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/reportes")
public class ReporteController {

    private final ReporteService reporteService;

    public ReporteController(ReporteService reporteService) {
        this.reporteService = reporteService;
    }

    @GetMapping("/movimientos")
    public List<MovimientoResponseDTO> movimientosPorPeriodo(
            @RequestParam(required = false) LocalDateTime fechaInicio,
            @RequestParam(required = false) LocalDateTime fechaFin,
            @RequestParam(required = false) Long sucursalId,
            @RequestParam(required = false) Long productoId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return reporteService.movimientosPorPeriodo(fechaInicio, fechaFin, sucursalId, productoId, page, size);
    }

    @GetMapping("/productos-top")
    public List<ProductoTopDTO> productosTop(
            @RequestParam(required = false) LocalDateTime fechaInicio,
            @RequestParam(required = false) LocalDateTime fechaFin,
            @RequestParam(required = false) Long sucursalId,
            @RequestParam(defaultValue = "10") int limite
    ) {
        return reporteService.productosTop(fechaInicio, fechaFin, sucursalId, limite);
    }

    @GetMapping("/valor-inventario")
    public ValorInventarioDTO valorInventario(@RequestParam Long sucursalId) {
        return reporteService.valorInventario(sucursalId);
    }
}
