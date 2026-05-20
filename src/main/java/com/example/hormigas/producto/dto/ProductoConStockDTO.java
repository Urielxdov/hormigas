package com.example.hormigas.producto.dto;

import java.math.BigDecimal;

public record ProductoConStockDTO(
        Long id,
        Long inventarioId,
        String nombre,
        String sku,
        BigDecimal precio,
        int stockActual
) {}
