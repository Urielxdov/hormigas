package com.example.hormigas.security.infrastructure.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateUsuarioDTO(
        @NotBlank @Email(message = "Formato de correo inválido") @Size(max = 255) String correo,
        @NotBlank @Size(min = 8, max = 72, message = "La contraseña debe tener entre 8 y 72 caracteres") String password,
        @NotBlank @Size(max = 100) String nombre,
        Long empresaId,
        Long sucursalId
) {}
