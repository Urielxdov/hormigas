package com.example.hormigas.inventario.dto;

public record CrearInventarioDTO(
        Long sucursalId,
        Long productoId,
        int stockActual,
        Integer stockMinimo,
        int stockMaximo
) {}