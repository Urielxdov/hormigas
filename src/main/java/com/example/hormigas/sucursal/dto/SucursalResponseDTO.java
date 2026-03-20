package com.example.hormigas.sucursal.dto;

import lombok.Builder;

@Builder
public record SucursalResponseDTO(
        Long id,
        String nombre,
        String direccion,
        boolean activa
) {
}
