package com.example.hormigas.security.dto.auth;

public record LoginRequestDTO (
        String email,
        String password
) {}
