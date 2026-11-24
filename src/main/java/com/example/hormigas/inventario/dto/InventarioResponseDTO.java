package com.example.hormigas.inventario.dto;

import java.math.BigDecimal;

public record InventarioResponseDTO(
        Long id,
        Long productoId,
        String productoNombre,
        BigDecimal precio,
        Long sucursalId,
        String sucursalNombre,
        int stockActual,
        Integer stockMinimo,
        Integer stockMaximo
) {}
