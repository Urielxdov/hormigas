package com.example.hormigas.movimiento.mapper;

import com.example.hormigas.movimiento.dto.MovimientoResponseDTO;
import com.example.hormigas.movimiento.entity.Movimiento;

public class MovimientoMapper {
    public static MovimientoResponseDTO toResponse(Movimiento movimiento) {
        return new MovimientoResponseDTO(
                movimiento.getId(),
                movimiento.getProducto().getId(),
                movimiento.getProducto().getNombre(),
                movimiento.getSucursal().getId(),
                movimiento.getSucursal().getNombre(),
                movimiento.getTipoMovimiento(),
                movimiento.getCantidad(),
                movimiento.getUsuario().getNombre(),
                movimiento.getReferencia(),
                movimiento.getFecha()
        );
    }
}
