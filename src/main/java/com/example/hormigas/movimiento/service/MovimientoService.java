package com.example.hormigas.movimiento.service;

import com.example.hormigas.inventario.entity.Inventario;
import com.example.hormigas.inventario.repository.InventarioRepository;
import com.example.hormigas.movimiento.dto.CrearMovimientoDTO;
import com.example.hormigas.movimiento.dto.MovimientoFiltroDTO;
import com.example.hormigas.movimiento.dto.MovimientoResponseDTO;
import com.example.hormigas.motivo.entity.MotivoMovimiento;
import com.example.hormigas.movimiento.entity.Movimiento;
import com.example.hormigas.movimiento.entity.TipoMovimiento;
import com.example.hormigas.movimiento.mapper.MovimientoMapper;
import com.example.hormigas.motivo.repository.MotivoMovimientoRepository;
import com.example.hormigas.movimiento.repository.MovimientoRepository;
import com.example.hormigas.movimiento.repository.MovimientoSpecification;
import com.example.hormigas.producto.repository.ProductoRepository;
import com.example.hormigas.security.domain.Usuario;
import com.example.hormigas.security.domain.repository.UsuarioRepository;
import com.example.hormigas.security.domain.services.UsuarioService;
import com.example.hormigas.sucursal.repository.SucursalRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MovimientoService {

    private final InventarioRepository inventarioRepository;
    private final MovimientoRepository movimientoRepository;
    private final SucursalRepository sucursalRepository;
    private final UsuarioService usuarioService;
    private final ProductoRepository productoRepository;

    public MovimientoService(
            InventarioRepository inventarioRepository,
            MovimientoRepository movimientoRepository,
            UsuarioService usuarioService,
            SucursalRepository sucursalRepository,
            ProductoRepository productoRepository
    ) {
        this.inventarioRepository = inventarioRepository;
        this.movimientoRepository = movimientoRepository;
        this.usuarioService = usuarioService;
        this.sucursalRepository = sucursalRepository;
        this.productoRepository = productoRepository;
    }

    @Transactional
    public MovimientoResponseDTO registrarMovimiento(CrearMovimientoDTO dto) {
        Usuario user = usuarioService.getUsuarioLogueado();

        // Se valida la existencia del inventario
        Inventario inventario = inventarioRepository
                .findBySucursalIdAndProductoId(dto.sucursalId(), dto.productoId())
                .orElseThrow(() -> new EntityNotFoundException("Inventario no encontrado"));

        // El tipo de movimiento es un enum
        TipoMovimiento tipo = dto.tipoMovimiento();
        // Almacenamos para el historial
        int stockActual = inventario.getStockActual();
        // tipo posee operaciones para aplicar el cambio
        int nuevoStock = tipo.aplicar(stockActual, dto.cantidad());

        if (nuevoStock < 0) throw new IllegalArgumentException("Stock insuficiente");

        // Actualizacion del inventraio
        inventario.setStockActual(nuevoStock);
        inventarioRepository.save(inventario);

        // Creamos el movimiento
        Movimiento movimiento = new Movimiento();
        movimiento.setTipoMovimiento(tipo);
        movimiento.setCantidad(dto.cantidad());
        movimiento.setStockAnterior(stockActual);
        movimiento.setStockNuevo(nuevoStock);
        movimiento.setUsuario(user);

        movimientoRepository.save(movimiento);

        return MovimientoMapper.toResponse(movimiento);
    }

    public List<MovimientoResponseDTO> obtenerMovimientos(MovimientoFiltroDTO filtro) {
        Usuario user = usuarioService.getUsuarioLogueado();
        List<Movimiento> movimientos = movimientoRepository.findAll(
                MovimientoSpecification.conFiltros(user.getEmpresa().getId(), filtro)
        );
        return movimientos.stream().map(MovimientoMapper::toResponse).toList();
    }


}