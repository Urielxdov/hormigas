package com.example.hormigas.inventario.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record CrearInventarioDTO(
        @NotNull Long sucursalId,
        @NotNull Long productoId,
        @NotNull @PositiveOrZero Integer stockActual,
        @PositiveOrZero Integer stockMinimo,
        @NotNull @PositiveOrZero Integer stockMaximo
) {}
