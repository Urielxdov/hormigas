package com.example.hormigas.movimiento.service;

import com.example.hormigas.inventario.entity.Inventario;
import com.example.hormigas.inventario.repository.InventarioRepository;
import com.example.hormigas.movimiento.dto.CrearMovimientoDTO;
import com.example.hormigas.movimiento.dto.MovimientoFiltroDTO;
import com.example.hormigas.movimiento.dto.MovimientoResponseDTO;
import com.example.hormigas.movimiento.dto.VentaBatchDTO;
import com.example.hormigas.movimiento.dto.VentaBatchItemDTO;
import com.example.hormigas.movimiento.entity.Movimiento;
import com.example.hormigas.movimiento.entity.TipoMovimiento;
import com.example.hormigas.movimiento.mapper.MovimientoMapper;
import com.example.hormigas.movimiento.repository.MovimientoRepository;
import com.example.hormigas.movimiento.repository.MovimientoSpecification;
import com.example.hormigas.producto.repository.ProductoRepository;
import com.example.hormigas.security.domain.Usuario;
import com.example.hormigas.security.domain.services.UsuarioService;
import com.example.hormigas.sucursal.entity.Sucursal;
import com.example.hormigas.sucursal.repository.SucursalRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
        Sucursal sucursal = sucursalRepository.findByIdAndEmpresaId(dto.sucursalId(), user.getEmpresa().getId())
                .orElseThrow(() -> new EntityNotFoundException("No se encontro la sucursal"));
        if (!sucursal.isActiva()) throw new IllegalArgumentException("La sucursal no esta activa");

        Movimiento movimiento = procesarItemMovimiento(
                sucursal.getId(), dto.productoId(), dto.tipoMovimiento(), dto.cantidad(), dto.referencia(), user
        );
        return MovimientoMapper.toResponse(movimiento);
    }

    @Transactional
    public List<MovimientoResponseDTO> registrarVentaBatch(VentaBatchDTO dto) {
        Usuario user = usuarioService.getUsuarioLogueado();
        Sucursal sucursal = sucursalRepository.findByIdAndEmpresaId(dto.sucursalId(), user.getEmpresa().getId())
                .orElseThrow(() -> new EntityNotFoundException("No se encontro la sucursal"));
        if (!sucursal.isActiva()) throw new IllegalArgumentException("La sucursal no esta activa");

        List<MovimientoResponseDTO> resultados = new ArrayList<>();
        for (VentaBatchItemDTO item : dto.items()) {
            Movimiento movimiento = procesarItemMovimiento(
                    sucursal.getId(), item.productoId(), TipoMovimiento.VENTA, item.cantidad(), dto.referencia(), user
            );
            resultados.add(MovimientoMapper.toResponse(movimiento));
        }
        return resultados;
    }

    public Page<MovimientoResponseDTO> obtenerMovimientos(MovimientoFiltroDTO filtro, Pageable pageable) {
        Usuario user = usuarioService.getUsuarioLogueado();
        return movimientoRepository
                .findAll(MovimientoSpecification.conFiltros(user.getEmpresa().getId(), filtro), pageable)
                .map(MovimientoMapper::toResponse);
    }

    private Movimiento procesarItemMovimiento(
            Long sucursalId,
            Long productoId,
            TipoMovimiento tipo,
            int cantidad,
            String referencia,
            Usuario user
    ) {
        Inventario inventario = inventarioRepository
                .findBySucursalIdAndProductoId(sucursalId, productoId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Inventario no encontrado para productoId=" + productoId
                ));

        int stockActual = inventario.getStockActual();
        int nuevoStock = tipo.aplicar(stockActual, cantidad);

        if (nuevoStock < 0) throw new IllegalArgumentException(
                "Stock insuficiente para productoId=" + productoId
        );

        inventario.setStockActual(nuevoStock);
        inventarioRepository.save(inventario);

        Movimiento movimiento = new Movimiento();
        movimiento.setTipoMovimiento(tipo);
        movimiento.setCantidad(cantidad);
        movimiento.setStockAnterior(stockActual);
        movimiento.setStockNuevo(nuevoStock);
        movimiento.setUsuario(user);
        movimiento.setInventario(inventario);
        movimiento.setReferencia(referencia);
        movimiento.setFecha(LocalDateTime.now());
        movimientoRepository.save(movimiento);

        return movimiento;
    }
}
