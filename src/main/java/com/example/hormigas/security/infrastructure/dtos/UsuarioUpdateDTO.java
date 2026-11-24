package com.example.hormigas.security.infrastructure.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Actualizacion parcial: un campo nulo significa "no cambiar". */
public record UsuarioUpdateDTO (
        @Pattern(regexp = ".*\\S.*", message = "no puede estar en blanco") @Size(max = 100) String nombre,
        @Email(message = "Formato de correo inválido") @Size(max = 255) String correo,
        @Size(min = 8, max = 72, message = "La contraseña debe tener entre 8 y 72 caracteres") String password,
        Long sucursalId
)
{}
