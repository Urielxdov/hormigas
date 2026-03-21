package com.example.hormigas.producto.dto;

import java.math.BigDecimal;

public record NuevoProductoDTO(
        String nombre,
        String descripcion,
        String sku,
        BigDecimal precio
) {
}
