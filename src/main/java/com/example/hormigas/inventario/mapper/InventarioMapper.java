package com.example.hormigas.inventario.mapper;

import com.example.hormigas.inventario.dto.InventarioResponseDTO;
import com.example.hormigas.inventario.entity.Inventario;

public class InventarioMapper {
    public static InventarioResponseDTO toResponseInventario(Inventario inventario) {
        InventarioResponseDTO response = new InventarioResponseDTO();
        response.setProductoId(inventario.getProducto().getId());
        response.setProductoNombre(inventario.getProducto().getNombre());
        response.setStockActual(inventario.getStockActual());
        response.setStockMinimo(inventario.getStockMinimo());
        return response;
    }
}
