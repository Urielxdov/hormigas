package com.example.hormigas.sucursal.dto;

import jakarta.validation.constraints.NotBlank;

public record SucursalCreateDTO(
        @NotBlank String nombre,
        String direccion,
        Long encargadoId
) {}
