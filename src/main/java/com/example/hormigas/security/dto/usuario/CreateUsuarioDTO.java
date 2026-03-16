package com.example.hormigas.security.dto.usuario;

public record CreateUsuarioDTO(
        String correo,
        String password,
        String nombre
) {}
