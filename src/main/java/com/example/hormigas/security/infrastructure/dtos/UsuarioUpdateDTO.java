package com.example.hormigas.security.infrastructure.dtos;

public record UsuarioUpdateDTO (
        String nombre,
        String correo,
        String password
)
{}
