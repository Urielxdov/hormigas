package com.example.hormigas.movimiento.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

/**
 * @param precioUnitario precio con el que se cobro en el cliente; si falta se usa el precio actual del producto
 */
public record VentaBatchItemDTO(
        @NotNull Long productoId,
        @Positive int cantidad,
        @PositiveOrZero BigDecimal precioUnitario
) {}
