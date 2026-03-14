package com.example.hormigas.producto.controller;

import com.example.hormigas.producto.dto.ProductoResponseDTO;
import com.example.hormigas.producto.service.ProductoService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/producto")
public class ProductoController {
    private ProductoService productoService;

    public ProductoController (ProductoService productoService) {
        this.productoService = productoService;
    }

    @GetMapping("/{empresaid}")
    public Page<ProductoResponseDTO> listarProductos(Pageable pageable, @PathVariable Long empresaId) {
        return productoService.obtenerProductos(empresaId, pageable);
    }
}
