package com.example.hormigas.movimiento.dto;

import com.example.hormigas.movimiento.entity.TipoMovimiento;

import java.time.LocalDateTime;

public record MovimientoFiltroDTO(
        Long sucursalId,
        Long productoId,
        Long inventarioId,
        TipoMovimiento tipo,
        LocalDateTime fechaInicio,
        LocalDateTime fechaFin
) {}
