package com.example.hormigas.empresa.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UsuarioEmpresaCreateDTO(
        @NotBlank @Size(max = 100) String nombre,
        @NotBlank @Email @Size(max = 255) String correo,
        @NotBlank @Size(min = 8, max = 72) String password
) {}
