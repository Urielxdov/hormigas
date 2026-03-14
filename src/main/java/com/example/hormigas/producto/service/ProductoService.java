package com.example.hormigas.producto.service;

import com.example.hormigas.empresa.entity.Empresa;
import com.example.hormigas.empresa.repository.EmpresaRepository;
import com.example.hormigas.producto.dto.NuevoProductoDTO;
import com.example.hormigas.producto.dto.ProductoActualizadoDTO;
import com.example.hormigas.producto.dto.ProductoResponseDTO;
import com.example.hormigas.producto.entity.Producto;
import com.example.hormigas.producto.mapper.ProductoMapper;
import com.example.hormigas.producto.repository.ProductoRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


@Service
public class ProductoService {
    private final ProductoRepository productoRepository;
    private final EmpresaRepository empresaRepository;

    public ProductoService(
            ProductoRepository productoRepository,
            EmpresaRepository empresaRepository
    ) {
        this.productoRepository = productoRepository;
        this.empresaRepository = empresaRepository;
    }
    // Crear producto
    public ProductoResponseDTO crearProducto(NuevoProductoDTO dto) {
        Empresa empresa = empresaRepository.findById(dto.empresaId())
                .orElseThrow(() -> new EntityNotFoundException("No se localizo la empresa"));

        productoRepository.findByEmpresaIdAndSku(dto.empresaId(), dto.sku())
                .ifPresent(p -> {
                    throw new RuntimeException("El SKU ya existe en esta empresa");
                });

        Producto producto = new Producto();

        producto.setDescripcion(dto.descripcion());
        producto.setActivo(true);

        // Obligatorio
        producto.setNombre(dto.nombre());
        producto.setSku(dto.sku());
        producto.setPrecio(dto.precio());

        // relacion
        producto.setEmpresa(empresa);

        return ProductoMapper.toResponse(productoRepository.save(producto));
    }

    // Actualizar producto
    public ProductoResponseDTO actualizarProducto(Long id, Long empresaId, ProductoActualizadoDTO dto) {
        Producto producto = productoRepository
                .findByIdAndEmpresaId(id, empresaId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        if (dto.nombre() != null) {
            producto.setNombre(dto.nombre());
        }

        if (dto.descripcion() != null) {
            producto.setDescripcion(dto.descripcion());
        }

        if (dto.precio() != null) {
            producto.setPrecio(dto.precio());
        }

        return ProductoMapper.toResponse(productoRepository.save(producto));
    }
    // Eliminar producto
    public void eliminarProducto (Long id, Long empresaId) {
        int filas = productoRepository.desactivarProducto(id, empresaId);

        if (filas == 0) {
            throw new EntityNotFoundException("Producto no encontrado");
        }
    }
    // Ver productos
    public Page<ProductoResponseDTO> obtenerProductos(Long empresaId, Pageable pageable) {
        return productoRepository
                .findByEmpresaIdAndActivoTrue(empresaId, pageable)
                .map(ProductoMapper::toResponse);
    }

}
