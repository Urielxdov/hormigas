package com.example.hormigas.security.infrastructure.dtos;

public record UsuarioResponseDTO (
        Long id,
        String name,
        String correo,
        Long empresaId
) {}
