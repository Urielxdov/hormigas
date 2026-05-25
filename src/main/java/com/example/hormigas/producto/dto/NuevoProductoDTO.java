package com.example.hormigas.producto.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record NuevoProductoDTO(
        @NotBlank String nombre,
        String descripcion,
        @NotBlank String sku,
        @PositiveOrZero BigDecimal precio,
        String categoria,
        Long categoriaId
) {}
