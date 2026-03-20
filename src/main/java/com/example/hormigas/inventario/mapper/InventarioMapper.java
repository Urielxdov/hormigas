package com.example.hormigas.inventario.mapper;

import com.example.hormigas.inventario.dto.InventarioResponseDTO;
import com.example.hormigas.inventario.entity.Inventario;

public class InventarioMapper {
    public static InventarioResponseDTO toResponseInventario(Inventario inventario) {
        return new InventarioResponseDTO(
                inventario.getId(),
                inventario.getProducto().getId(),
                inventario.getProducto().getNombre(),
                inventario.getSucursal().getId(),
                inventario.getSucursal().getNombre(),
                inventario.getStockActual(),
                inventario.getStockMinimo(),
                inventario.getStockMaximo()
        );
    }
}
