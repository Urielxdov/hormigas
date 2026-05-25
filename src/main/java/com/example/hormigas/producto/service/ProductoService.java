package com.example.hormigas.producto.service;

import com.example.hormigas.inventario.repository.InventarioRepository;
import com.example.hormigas.producto.dto.NuevoProductoDTO;
import com.example.hormigas.producto.dto.ProductoActualizadoDTO;
import com.example.hormigas.producto.dto.ProductoConStockDTO;
import com.example.hormigas.producto.dto.ProductoResponseDTO;
import com.example.hormigas.producto.entity.Producto;
import com.example.hormigas.producto.mapper.ProductoMapper;
import com.example.hormigas.producto.repository.ProductoRepository;
import com.example.hormigas.security.domain.Usuario;
import com.example.hormigas.security.domain.services.UsuarioService;
import com.example.hormigas.sucursal.repository.SucursalRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class ProductoService {
    private final ProductoRepository productoRepository;
    private final UsuarioService usuarioService;
    private final InventarioRepository inventarioRepository;
    private final SucursalRepository sucursalRepository;

    public ProductoService(
            ProductoRepository productoRepository,
            UsuarioService usuarioService,
            InventarioRepository inventarioRepository,
            SucursalRepository sucursalRepository
    ) {
        this.productoRepository = productoRepository;
        this.usuarioService = usuarioService;
        this.inventarioRepository = inventarioRepository;
        this.sucursalRepository = sucursalRepository;
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
        producto.setCategoriaTexto(dto.categoria());

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

    // Buscar productos con stock en sucursal
    public List<ProductoConStockDTO> buscarConStock(String q, Long sucursalId) {
        Usuario user = usuarioService.getUsuarioLogueado();
        Long empresaId = user.getEmpresa().getId();

        sucursalRepository.findByIdAndEmpresaId(sucursalId, empresaId)
                .orElseThrow(() -> new EntityNotFoundException("Sucursal no encontrada en esta empresa"));

        List<Producto> productos = productoRepository.buscarPorNombreOSku(
                q == null ? "" : q, empresaId
        );
        return productos.stream().map(p ->
                inventarioRepository
                        .findBySucursalIdAndProductoId(sucursalId, p.getId())
                        .map(inv -> new ProductoConStockDTO(
                                p.getId(), inv.getId(), p.getNombre(), p.getSku(), p.getPrecio(), inv.getStockActual()
                        ))
                        .orElse(new ProductoConStockDTO(p.getId(), null, p.getNombre(), p.getSku(), p.getPrecio(), 0))
        ).toList();
    }

}
