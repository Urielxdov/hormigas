package com.example.hormigas.producto.dto;

import java.math.BigDecimal;

public record ProductoActualizadoDTO(
        String nombre,
        String descripcion,
        BigDecimal precio,
        Boolean activo,
        Long categoriaId
) {
}
