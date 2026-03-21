package com.example.hormigas.inventario.service;

import com.example.hormigas.inventario.dto.CrearInventarioDTO;
import com.example.hormigas.inventario.dto.InventarioFiltroDTO;
import com.example.hormigas.inventario.dto.InventarioResponseDTO;
import com.example.hormigas.inventario.entity.Inventario;
import com.example.hormigas.inventario.mapper.InventarioMapper;
import com.example.hormigas.inventario.repository.InventarioRepository;
import com.example.hormigas.inventario.repository.InventarioSpecification;
import com.example.hormigas.movimiento.service.MovimientoService;
import com.example.hormigas.producto.entity.Producto;
import com.example.hormigas.producto.repository.ProductoRepository;
import com.example.hormigas.security.domain.Usuario;
import com.example.hormigas.security.domain.services.UsuarioService;
import com.example.hormigas.sucursal.entity.Sucursal;
import com.example.hormigas.sucursal.repository.SucursalRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class InventarioService {

    private final InventarioRepository inventarioRepository;
    private final SucursalRepository sucursalRepository;
    private final UsuarioService usuarioService;
    private final ProductoRepository productoRepository;

    public InventarioService (
            InventarioRepository inventarioRepository,
            SucursalRepository sucursalRepository,
            UsuarioService usuarioService,
            ProductoRepository productoRepository
    ) {
        this.inventarioRepository = inventarioRepository;
        this.sucursalRepository = sucursalRepository;
        this.usuarioService = usuarioService;
        this.productoRepository = productoRepository;
    }

    public InventarioResponseDTO crear(CrearInventarioDTO dto) {
        Usuario user = usuarioService.getUsuarioLogueado();

        Sucursal sucursal = sucursalRepository.findById(dto.sucursalId())
                .orElseThrow(() -> new EntityNotFoundException("No existe la sucursal"));

        Producto producto = productoRepository.findById(dto.productoId())
                .orElseThrow(() -> new EntityNotFoundException("No se encontro el producto"));

        if(!user.getEmpresa().equals(sucursal.getEmpresa()) || !user.getEmpresa().equals(producto.getEmpresa())) {
            throw new IllegalArgumentException("Parametros invalidos");
        }

        Inventario inventario = new Inventario();
        inventario.setSucursal(sucursal);
        inventario.setProducto(producto);
        inventario.setStockActual(dto.stockActual());
        inventario.setStockMinimo(dto.stockMinimo());
        inventario.setStockMaximo(dto.stockMaximo());

        return InventarioMapper.toResponseInventario(inventarioRepository.save(inventario));
    }

    //Busqueda por sucursal, producto y empresa
    public List<InventarioResponseDTO> obtenerInventario(InventarioFiltroDTO filter) {
        Usuario user = usuarioService.getUsuarioLogueado();

        List<Inventario> inventarios = inventarioRepository.findAll(
                InventarioSpecification.conFiltros(user.getEmpresa().getId(), filter)
        );

        return inventarios.stream()
                .map(InventarioMapper::toResponseInventario)
                .toList();
    }

    @Transactional
    public void agregarASucursal(Long sucursalId, Long inventarioId) {
        Usuario user = usuarioService.getUsuarioLogueado();

        int filas = inventarioRepository.asignarASucursal(
                inventarioId,
                sucursalId,
                user.getEmpresa().getId()
        );

        if (filas == 0) {
            throw new IllegalArgumentException("Inventario o sucursal invalidos o no pertenecen a la empresa");
        }
        if (filas > 1) {
            throw new IllegalStateException("Error critico: multiples inventarios afectado");
        }
    }


    // Ver historial de inventario (Historial de movmientos)

}
