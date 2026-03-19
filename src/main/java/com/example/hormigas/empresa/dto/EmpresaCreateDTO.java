package com.example.hormigas.empresa.dto;

public record EmpresaCreateDTO(
    String nombre,
    String rfc,
    String direccion,
    String telefono
) {}
