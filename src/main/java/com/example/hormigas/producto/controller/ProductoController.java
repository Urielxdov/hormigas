package com.example.hormigas.producto.controller;

import com.example.hormigas.producto.dto.NuevoProductoDTO;
import com.example.hormigas.producto.dto.ProductoConStockDTO;
import com.example.hormigas.producto.dto.ProductoResponseDTO;
import com.example.hormigas.producto.service.ProductoService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/producto")
public class ProductoController {
    private ProductoService productoService;

    public ProductoController (ProductoService productoService) {
        this.productoService = productoService;
    }

    @GetMapping("/")
    public Page<ProductoResponseDTO> listarProductos(Pageable pageable) {
        return productoService.obtenerProductos(pageable);
    }

    @PostMapping("/nuevo")
    public ProductoResponseDTO crear(@RequestBody NuevoProductoDTO dto) {
        return productoService.crearProducto(dto);
    }

    @GetMapping("/buscar")
    public List<ProductoConStockDTO> buscarConStock(
            @RequestParam(required = false, defaultValue = "") String q,
            @RequestParam Long sucursalId) {
        return productoService.buscarConStock(q, sucursalId);
    }
}
