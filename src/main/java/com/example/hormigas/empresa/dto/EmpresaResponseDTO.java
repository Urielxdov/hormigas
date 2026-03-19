package com.example.hormigas.empresa.dto;

public record EmpresaResponseDTO(
        Long id,
        String nombre,
        String rfc,
        String direccion,
        String telefono
) {}
