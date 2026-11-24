package com.example.hormigas.empresa.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record EmpresaConAdminCreateDTO(
        @NotNull @Valid EmpresaCreateDTO empresa,
        @NotNull @Valid UsuarioEmpresaCreateDTO admin
) {}
