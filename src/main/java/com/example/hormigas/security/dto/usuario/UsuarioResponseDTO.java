package com.example.hormigas.security.dto.usuario;

public record UsuarioResponseDTO (
        Long id,
        String name,
        String correo,
        Long empresaId
) {}
