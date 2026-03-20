package com.example.hormigas.inventario.dto;

public record InventarioResponseDTO(
        Long id,
        Long productoId,
        String productoNombre,
        Long sucursalId,
        String sucursalNombre,
        int stockActual,
        int stockMinimo,
        int stockMaximo
) {}
