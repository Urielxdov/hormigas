package com.example.hormigas.security.dto.usuario;

public record UsuarioModificadoDTO (
    String nombre,
    String correo,
    String passwordHash
) {}
