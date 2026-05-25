package com.example.hormigas.producto.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record NuevaCategoriaDTO(
        @NotBlank
        @Size(min = 2, max = 50, message = "El nombre debe tener entre 2 y 50 caracteres")
        @Pattern(regexp = "^[\\p{L}\\p{N}\\s\\-_&/.]+$", message = "El nombre solo puede contener letras, números, espacios y los caracteres -_&/.")
        String nombre,

        @Size(max = 200, message = "La descripción no puede exceder 200 caracteres")
        String descripcion
) {}
