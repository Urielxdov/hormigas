package com.example.hormigas.sucursal.mapper;

import com.example.hormigas.sucursal.dto.SucursalResponseDTO;
import com.example.hormigas.sucursal.entity.Sucursal;

public class MapperSucursal {
    public static SucursalResponseDTO toResponse(Sucursal sucursal) {
        return new SucursalResponseDTO(
                sucursal.getId(),
                sucursal.getNombre(),
                sucursal.getDireccion(),
                sucursal.isActiva()
        );
    }
}