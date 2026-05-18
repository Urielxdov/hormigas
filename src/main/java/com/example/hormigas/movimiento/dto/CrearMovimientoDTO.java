package com.example.hormigas.movimiento.dto;

import com.example.hormigas.movimiento.entity.TipoMovimiento;
import jakarta.validation.constraints.Positive;

public record CrearMovimientoDTO(
        Long sucursalId,
        Long productoId,
        TipoMovimiento tipoMovimiento,
        @Positive int cantidad,
        String referencia,
        Long motivoId
) {}
