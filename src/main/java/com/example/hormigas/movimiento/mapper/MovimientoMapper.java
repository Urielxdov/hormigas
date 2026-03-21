package com.example.hormigas.movimiento.mapper;

import com.example.hormigas.movimiento.dto.MovimientoResponseDTO;
import com.example.hormigas.movimiento.entity.Movimiento;

import java.time.format.DateTimeFormatter;

public class MovimientoMapper {

    public static MovimientoResponseDTO toResponse(Movimiento movimiento) {
        Long productoId = movimiento.getInventario() != null && movimiento.getInventario().getProducto() != null
                ? movimiento.getInventario().getProducto().getId()
                : null;
        String productoNombre = movimiento.getInventario() != null && movimiento.getInventario().getProducto() != null
                ? movimiento.getInventario().getProducto().getNombre()
                : null;

        Long sucursalId = movimiento.getInventario() != null && movimiento.getInventario().getSucursal() != null
                ? movimiento.getInventario().getSucursal().getId()
                : null;
        String sucursalNombre = movimiento.getInventario() != null && movimiento.getInventario().getSucursal() != null
                ? movimiento.getInventario().getSucursal().getNombre()
                : null;

        String usuarioNombre = movimiento.getUsuario() != null
                ? movimiento.getUsuario().getNombre()
                : null;

        return new MovimientoResponseDTO(
                movimiento.getId(),
                productoId,
                productoNombre,
                sucursalId,
                sucursalNombre,
                movimiento.getTipoMovimiento(),
                movimiento.getCantidad(),
                usuarioNombre,
                movimiento.getReferencia(),
                movimiento.getFecha()
        );
    }

}
