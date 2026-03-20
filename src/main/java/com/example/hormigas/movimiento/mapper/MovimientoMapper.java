package com.example.hormigas.movimiento.mapper;

import com.example.hormigas.movimiento.dto.MovimientoResponseDTO;
import com.example.hormigas.movimiento.entity.Movimiento;

import java.time.format.DateTimeFormatter;

public class MovimientoMapper {


    public static MovimientoResponseDTO toResponse(Movimiento movimiento) {
        return new MovimientoResponseDTO(
                movimiento.getId(),

                movimiento.getInventario().getProducto().getId(),
                movimiento.getInventario().getProducto().getNombre(),

                movimiento.getInventario().getSucursal().getId(),
                movimiento.getInventario().getSucursal().getNombre(),

                movimiento.getTipoMovimiento(),
                movimiento.getCantidad(),

                movimiento.getUsuario().getNombre(),
                movimiento.getReferencia(),

                movimiento.getFecha()
        );
    }
}
