package com.example.hormigas.security.dto.usuario;

public record UsuarioUpdateDTO (
        String nombre,
        String correo,
        String password
)
{}
