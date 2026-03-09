package com.example.hormigas.movimiento.dto;

import com.example.hormigas.movimiento.entity.TipoMovimiento;

public record CrearMovimientoDTO(
        Long sucursalId,
        Long productoId,
        Long motivoId,
        int cantidad,
        String referencia
) {}