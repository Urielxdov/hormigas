package com.example.hormigas.sucursal.mapper;

import com.example.hormigas.sucursal.dto.SucursalResponseDTO;
import com.example.hormigas.sucursal.entity.Sucursal;

public class MapperSucursal {
    public static SucursalResponseDTO toResponse(Sucursal sucursal) {
        return SucursalResponseDTO.builder()
                .id(sucursal.getId())
                .activa(sucursal.isActiva())
                .nombre(sucursal.getNombre())
                .direccion(sucursal.getDireccion())
                .build();
    }
}
