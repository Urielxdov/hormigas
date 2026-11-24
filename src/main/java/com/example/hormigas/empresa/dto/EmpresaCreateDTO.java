package com.example.hormigas.empresa.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record EmpresaCreateDTO(
        @NotBlank @Size(min = 3, max = 120, message = "El nombre debe tener entre 3 y 120 caracteres") String nombre,
        @NotBlank
        @Pattern(regexp = "^[A-Za-zÑñ&]{3,4}\\d{6}[A-Za-z0-9]{3}$", message = "RFC inválido (12 o 13 caracteres, ej. ABC010101AB1)")
        String rfc,
        @Size(max = 255) String direccion,
        @Pattern(regexp = "^$|^\\d{10}$", message = "El teléfono debe tener 10 dígitos") String telefono
) {}
