package com.example.hormigas.sucursal.dto;

public record SucursalResponseDTO(
        Long id,
        String nombre,
        String direccion,
        boolean activa
) {
}
