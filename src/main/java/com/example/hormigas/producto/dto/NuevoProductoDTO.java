package com.example.hormigas.producto.dto;

import java.math.BigDecimal;

public record NuevoProductoDTO(
        Long empresaId,
        String nombre,
        String descripcion,
        String sku,
        BigDecimal precio
) {
}
