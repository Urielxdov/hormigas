package com.example.hormigas.producto.service;

import com.example.hormigas.empresa.entity.Empresa;
import com.example.hormigas.empresa.repository.EmpresaRepository;
import com.example.hormigas.producto.dto.NuevoProductoDTO;
import com.example.hormigas.producto.dto.ProductoActualizadoDTO;
import com.example.hormigas.producto.dto.ProductoResponseDTO;
import com.example.hormigas.producto.entity.Producto;
import com.example.hormigas.producto.mapper.ProductoMapper;
import com.example.hormigas.producto.repository.ProductoRepository;
import com.example.hormigas.security.domain.Usuario;
import com.example.hormigas.security.domain.services.UsuarioService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


@Service
public class ProductoService {
    private final ProductoRepository productoRepository;
    private final UsuarioService usuarioService;

    public ProductoService(
            ProductoRepository productoRepository,
            UsuarioService usuarioService
    ) {
        this.productoRepository = productoRepository;
        this.usuarioService = usuarioService;
    }
    // Crear producto
    public ProductoResponseDTO crearProducto(NuevoProductoDTO dto) {
        Usuario user = usuarioService.getUsuarioLogueado();

        productoRepository.findByEmpresaIdAndSku(user.getEmpresa().getId(), dto.sku())
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
        producto.setEmpresa(user.getEmpresa());

        return ProductoMapper.toResponse(productoRepository.save(producto));
    }

    // Actualizar producto
    public ProductoResponseDTO actualizarProducto(Long id, ProductoActualizadoDTO dto) {
        Usuario user = usuarioService.getUsuarioLogueado();
        Producto producto = productoRepository
                .findByIdAndEmpresaId(id, user.getEmpresa().getId())
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
    public void eliminarProducto (Long id) {
        Usuario user = usuarioService.getUsuarioLogueado();
        int filas = productoRepository.desactivarProducto(id, user.getEmpresa().getId());

        if (filas == 0) {
            throw new EntityNotFoundException("Producto no encontrado");
        }
    }
    // Ver productos
    public Page<ProductoResponseDTO> obtenerProductos(Pageable pageable) {
        Usuario user = usuarioService.getUsuarioLogueado();
        return productoRepository
                .findByEmpresaIdAndActivoTrue(user.getEmpresa().getId(), pageable)
                .map(ProductoMapper::toResponse);
    }

}
