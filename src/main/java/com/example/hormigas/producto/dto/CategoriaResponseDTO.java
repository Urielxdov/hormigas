package com.example.hormigas.producto.dto;

public record CategoriaResponseDTO(
        Long id,
        String nombre,
        String descripcion,
        boolean activo
) {}
