package com.example.hormigas.producto.mapper;

import com.example.hormigas.producto.dto.ProductoResponseDTO;
import com.example.hormigas.producto.entity.Producto;

public class ProductoMapper {

    public static ProductoResponseDTO toResponse(Producto producto) {
        return toResponse(producto, null);
    }

    public static ProductoResponseDTO toResponse(Producto producto, Long stockTotal) {
        var categoria = producto.getCategoria();
        return new ProductoResponseDTO(
                producto.getId(),
                categoria != null ? categoria.getNombre() : producto.getCategoriaTexto(),
                producto.getNombre(),
                producto.getDescripcion(),
                producto.getSku(),
                producto.getPrecio(),
                producto.isActivo(),
                categoria != null ? categoria.getId() : null,
                stockTotal
        );
    }
}
