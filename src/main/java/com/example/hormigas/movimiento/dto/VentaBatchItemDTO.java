package com.example.hormigas.movimiento.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record VentaBatchItemDTO(
        @NotNull Long productoId,
        @Positive int cantidad
) {}
