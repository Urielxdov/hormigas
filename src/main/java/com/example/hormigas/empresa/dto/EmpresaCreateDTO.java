package com.example.hormigas.empresa.dto;

import jakarta.validation.constraints.NotBlank;

public record EmpresaCreateDTO(
        @NotBlank String nombre,
        String rfc,
        String direccion,
        String telefono
) {}
