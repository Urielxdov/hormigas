package com.example.hormigas.producto.dto;

import jakarta.validation.constraints.NotBlank;

public record NuevaCategoriaDTO(
        @NotBlank String nombre,
        String descripcion
) {}
