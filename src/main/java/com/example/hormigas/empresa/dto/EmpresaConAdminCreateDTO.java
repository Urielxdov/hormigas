package com.example.hormigas.empresa.dto;

public record EmpresaConAdminCreateDTO(
        EmpresaCreateDTO empresa,
        UsuarioEmpresaCreateDTO admin
) {}
