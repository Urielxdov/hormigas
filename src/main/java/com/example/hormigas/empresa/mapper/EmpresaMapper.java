package com.example.hormigas.empresa.mapper;

import com.example.hormigas.empresa.dto.EmpresaResponseDTO;
import com.example.hormigas.empresa.entity.Empresa;

public class EmpresaMapper {
    public static EmpresaResponseDTO toResponseEmpresa(Empresa empresa) {
        EmpresaResponseDTO nueva = new EmpresaResponseDTO();
        nueva.setId(empresa.getId());
        nueva.setNombre(empresa.getNombre());
        nueva.setDireccion(empresa.getDireccion());
        nueva.setRfc(empresa.getRfc());
        nueva.setTelefono(empresa.getTelefono());
        return nueva;
    }
}
