package com.example.hormigas.security.dto.usuario;

public record NuevoUsuarioDto(
        Long empresaId,
        String nombre,
        String correo,
        String password
) {}
