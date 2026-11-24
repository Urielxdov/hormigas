package com.example.hormigas.producto.dto;

import java.math.BigDecimal;

public record ProductoResponseDTO(
        Long id,
        String categoria, // Este debe ser el nombre de la categoria
        String nombre,
        String descripcion,
        String sku,
        BigDecimal precio,
        boolean activo,
        Long categoriaId,
        /** Suma del stock en todas las sucursales de la empresa (null si no se calculo). */
        Long stockTotal
) {
}
