package com.example.hormigas.security.dto.usuario;

public record LoginResponseDTO(
        Long id,
        String nombre,
        String correo,
        Long empresaId
) {
}
