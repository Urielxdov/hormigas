package com.example.hormigas.movimiento.dto;

import com.example.hormigas.movimiento.entity.TipoMovimiento;

public record MovimientoFiltroDTO(
        Long sucursalId,
        Long productoId,
        Long inventarioId,
        TipoMovimiento tipo
) {}
