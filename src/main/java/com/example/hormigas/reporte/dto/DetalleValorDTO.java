package com.example.hormigas.reporte.dto;

import java.math.BigDecimal;

public record DetalleValorDTO(
        Long productoId,
        String nombre,
        String sku,
        int stockActual,
        BigDecimal precio,
        BigDecimal valorLinea,
        boolean sinPrecio
) {}
