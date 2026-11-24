package com.example.hormigas.producto.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Actualizacion parcial: un campo nulo significa "no cambiar".
 * nombre y sku, si vienen, no pueden estar en blanco.
 */
public record ProductoActualizadoDTO(
        @Pattern(regexp = ".*\\S.*", message = "no puede estar en blanco") @Size(max = 255) String nombre,
        @Size(max = 255) String descripcion,
        @Pattern(regexp = ".*\\S.*", message = "no puede estar en blanco") @Size(max = 255) String sku,
        @PositiveOrZero @Digits(integer = 8, fraction = 2) BigDecimal precio,
        @Size(max = 255) String categoria,
        Boolean activo,
        Long categoriaId
) {
}
