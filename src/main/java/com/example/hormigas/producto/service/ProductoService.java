package com.example.hormigas.producto.service;

import com.example.hormigas.inventario.repository.InventarioRepository;
import com.example.hormigas.producto.dto.NuevoProductoDTO;
import com.example.hormigas.producto.dto.ProductoActualizadoDTO;
import com.example.hormigas.producto.dto.ProductoConStockDTO;
import com.example.hormigas.producto.dto.ProductoResponseDTO;
import com.example.hormigas.producto.entity.Producto;
import com.example.hormigas.producto.mapper.ProductoMapper;
import com.example.hormigas.producto.repository.CategoriaRepository;
import com.example.hormigas.producto.repository.ProductoRepository;
import com.example.hormigas.security.domain.Usuario;
import com.example.hormigas.security.domain.services.UsuarioService;
import com.example.hormigas.shared.exception.ConflictException;
import com.example.hormigas.sucursal.repository.SucursalRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
public class ProductoService {
    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;
    private final UsuarioService usuarioService;
    private final InventarioRepository inventarioRepository;
    private final SucursalRepository sucursalRepository;

    public ProductoService(
            ProductoRepository productoRepository,
            CategoriaRepository categoriaRepository,
            UsuarioService usuarioService,
            InventarioRepository inventarioRepository,
            SucursalRepository sucursalRepository
    ) {
        this.productoRepository = productoRepository;
        this.categoriaRepository = categoriaRepository;
        this.usuarioService = usuarioService;
        this.inventarioRepository = inventarioRepository;
        this.sucursalRepository = sucursalRepository;
    }
    // Crear producto
    public ProductoResponseDTO crearProducto(NuevoProductoDTO dto) {
        Usuario user = usuarioService.getUsuarioLogueado();

        productoRepository.findByEmpresaIdAndSku(user.getEmpresa().getId(), dto.sku())
                .ifPresent(p -> {
                    throw new ConflictException("El SKU ya existe en esta empresa");
                });

        Producto producto = new Producto();

        producto.setDescripcion(dto.descripcion());
        producto.setActivo(true);

        if (dto.categoriaId() != null) {
            categoriaRepository.findByIdAndEmpresaId(dto.categoriaId(), user.getEmpresa().getId())
                    .ifPresent(cat -> {
                        producto.setCategoria(cat);
                        producto.setCategoriaTexto(cat.getNombre());
                    });
        } else if (dto.categoria() != null) {
            producto.setCategoriaTexto(dto.categoria());
        }

        // Obligatorio
        producto.setNombre(dto.nombre());
        producto.setSku(dto.sku());
        producto.setPrecio(dto.precio());

        // relacion
        producto.setEmpresa(user.getEmpresa());

        return ProductoMapper.toResponse(productoRepository.save(producto));
    }

    // Actualizar producto
    @Transactional
    public ProductoResponseDTO actualizarProducto(Long id, ProductoActualizadoDTO dto) {
        Usuario user = usuarioService.getUsuarioLogueado();
        Producto producto = productoRepository
                .findByIdAndEmpresaId(id, user.getEmpresa().getId())
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado"));

        if (dto.nombre() != null) {
            producto.setNombre(dto.nombre());
        }

        if (dto.descripcion() != null) {
            producto.setDescripcion(dto.descripcion());
        }

        if (dto.sku() != null && !dto.sku().equals(producto.getSku())) {
            productoRepository.findByEmpresaIdAndSku(user.getEmpresa().getId(), dto.sku())
                    .ifPresent(p -> {
                        throw new ConflictException("El SKU ya existe en esta empresa");
                    });
            producto.setSku(dto.sku());
        }

        if (dto.precio() != null) {
            producto.setPrecio(dto.precio());
        }

        if (dto.categoriaId() != null) {
            var categoria = categoriaRepository.findByIdAndEmpresaId(dto.categoriaId(), user.getEmpresa().getId())
                    .orElseThrow(() -> new EntityNotFoundException("Categoria no encontrada"));
            producto.setCategoria(categoria);
            producto.setCategoriaTexto(categoria.getNombre());
        } else if (dto.categoria() != null) {
            producto.setCategoria(null);
            producto.setCategoriaTexto(dto.categoria());
        }

        if (dto.activo() != null) {
            producto.setActivo(dto.activo());
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
    // Una sola consulta (producto LEFT JOIN inventario de la sucursal). Sin limit devuelve todo:
    // el POS usa q="" para descargar el catalogo completo.
    public List<ProductoConStockDTO> buscarConStock(String q, Long sucursalId, Integer limit) {
        Usuario user = usuarioService.getUsuarioLogueado();
        Long empresaId = user.getEmpresa().getId();

        sucursalRepository.findByIdAndEmpresaId(sucursalId, empresaId)
                .orElseThrow(() -> new EntityNotFoundException("Sucursal no encontrada en esta empresa"));

        Pageable pagina = (limit != null && limit > 0) ? PageRequest.of(0, limit) : Pageable.unpaged();
        return productoRepository.buscarConStock(q == null ? "" : q, empresaId, sucursalId, pagina);
    }

}
