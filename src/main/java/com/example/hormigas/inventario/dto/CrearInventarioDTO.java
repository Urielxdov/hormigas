package com.example.hormigas.inventario.dto;

public record CrearInventarioDTO(
        Long productoId,
        int stockActual,
        Integer stockMinimo,
        int stockMaximo
) {}