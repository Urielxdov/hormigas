package com.example.hormigas.movimiento.dto;

import com.example.hormigas.movimiento.entity.TipoMovimiento;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CrearMovimientoDTO(
        @NotNull Long sucursalId,
        @NotNull Long productoId,
        @NotNull TipoMovimiento tipoMovimiento,
        @Positive int cantidad,
        String referencia
) {}
