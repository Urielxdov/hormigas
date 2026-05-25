package com.example.hormigas.inventario.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record CrearInventarioDTO(
        @NotNull Long sucursalId,
        @NotNull Long productoId,
        int stockActual,
        @PositiveOrZero Integer stockMinimo,
        @PositiveOrZero int stockMaximo
) {}
