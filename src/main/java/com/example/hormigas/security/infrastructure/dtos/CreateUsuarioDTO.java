package com.example.hormigas.security.infrastructure.dtos;

import jakarta.validation.constraints.NotBlank;

public record CreateUsuarioDTO(
        @NotBlank String correo,
        @NotBlank String password,
        @NotBlank String nombre,
        Long empresaId,
        Long sucursalId
) {}
