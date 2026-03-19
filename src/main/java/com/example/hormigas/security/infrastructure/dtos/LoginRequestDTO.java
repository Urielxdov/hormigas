package com.example.hormigas.security.infrastructure.dtos;

public record LoginRequestDTO (
        String email,
        String password
) {}
