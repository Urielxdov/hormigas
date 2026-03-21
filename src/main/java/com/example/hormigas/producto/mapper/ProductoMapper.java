package com.example.hormigas.producto.mapper;

import com.example.hormigas.producto.dto.ProductoResponseDTO;
import com.example.hormigas.producto.entity.Producto;

public class ProductoMapper {

    public static ProductoResponseDTO toResponse(Producto producto) {
        return new ProductoResponseDTO(
                producto.getId(),
                producto.getCategoria() != null ? producto.getCategoria().getNombre() : null,
                producto.getNombre(),
                producto.getDescripcion(),
                producto.getSku(),
                producto.getPrecio(),
                producto.isActivo()
        );
    }
}
