package com.example.hormigas.security.dto.usuario;

public record LoginRequestDTO(
        String correo,
        String password
) {}