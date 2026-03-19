package com.example.hormigas.security.infrastructure.dtos;

public record CreateUsuarioDTO(
        String correo,
        String password,
        String nombre
) {}
