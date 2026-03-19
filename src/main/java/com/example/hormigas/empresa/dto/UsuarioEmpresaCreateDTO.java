package com.example.hormigas.empresa.dto;

public record UsuarioEmpresaCreateDTO(
        String nombre,
        String correo,
        String password
) {}
