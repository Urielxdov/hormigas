package com.example.hormigas.empresa.mapper;

import com.example.hormigas.empresa.dto.EmpresaResponseDTO;
import com.example.hormigas.empresa.entity.Empresa;

public class EmpresaMapper {
    public static EmpresaResponseDTO toResponseEmpresa(Empresa empresa) {
        return new EmpresaResponseDTO(
                empresa.getId(),
                empresa.getNombre(),
                empresa.getRfc(),
                empresa.getDireccion(),
                empresa.getTelefono(),
                empresa.isActivo()
        );
    }
}
