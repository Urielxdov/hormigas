package com.example.hormigas.movimiento.dto;

import com.example.hormigas.movimiento.entity.TipoMovimiento;

public record CrearMovimientoDTO(
        Long sucursalId,
        Long productoId,
        TipoMovimiento tipoMovimiento,
        int cantidad,
        String referencia
) {}