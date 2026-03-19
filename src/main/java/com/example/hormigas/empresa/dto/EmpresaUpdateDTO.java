package com.example.hormigas.empresa.dto;

public record EmpresaUpdateDTO(
        String nombre,
        String rfc,
        String direccion,
        String telefono,
        boolean activo
) {}
