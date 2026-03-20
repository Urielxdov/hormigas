package com.example.hormigas.movimiento.service;

import com.example.hormigas.inventario.entity.Inventario;
import com.example.hormigas.inventario.repository.InventarioRepository;
import com.example.hormigas.movimiento.dto.CrearMovimientoDTO;
import com.example.hormigas.movimiento.dto.MovimientoResponseDTO;
import com.example.hormigas.motivo.entity.MotivoMovimiento;
import com.example.hormigas.movimiento.entity.Movimiento;
import com.example.hormigas.movimiento.entity.TipoMovimiento;
import com.example.hormigas.movimiento.mapper.MovimientoMapper;
import com.example.hormigas.motivo.repository.MotivoMovimientoRepository;
import com.example.hormigas.movimiento.repository.MovimientoRepository;
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

        Inventario inventario = inventarioRepository
                .findBySucursalIdAndProductoId(dto.sucursalId(), dto.productoId())
                .orElseThrow(() -> new EntityNotFoundException("Inventario no encontrado"));

        TipoMovimiento tipo = dto.tipoMovimiento();
        int stockActual = inventario.getStockActual();
        int nuevoStock = tipo.aplicar(stockActual, dto.cantidad());

        if (nuevoStock < 0) throw new IllegalArgumentException("Stock insuficiente");

        inventario.setStockActual(nuevoStock);
        inventarioRepository.save(inventario);

        Movimiento movimiento = new Movimiento();
        movimiento.setProducto(inventario.getProducto());
        movimiento.setSucursal(inventario.getSucursal());
        movimiento.setTipoMovimiento(tipo);
        movimiento.setCantidad(dto.cantidad());
        movimiento.setStockAnterior(stockActual);
        movimiento.setStockNuevo(nuevoStock);
        movimiento.setUsuario(user);

        movimientoRepository.save(movimiento);

        return MovimientoMapper.toResponse(movimiento);
    }

    public List<MovimientoResponseDTO> obtenerMovimientos() {
        Usuario user = usuarioService.getUsuarioLogueado();
        List<Movimiento> movimientos = movimientoRepository.findBySucursal_Empresa_Id(user.getEmpresa().getId());
        return movimientos.stream().map(MovimientoMapper::toResponse).toList();
    }

    public List<MovimientoResponseDTO> obtenerMovimientos(Long sucursalId) {
        if (!sucursalRepository.existsById(sucursalId)) {
            throw new EntityNotFoundException("Sucursal no existente");
        }
        Usuario user = usuarioService.getUsuarioLogueado();
        List<Movimiento> movimientos = movimientoRepository.findBySucursal_Empresa_IdAndSucursal_Id(
                user.getEmpresa().getId(), sucursalId
        );
        return movimientos.stream().map(MovimientoMapper::toResponse).toList();
    }

    public List<MovimientoResponseDTO> obtenerMovimientosProducto(Long productoId) {
        if (!productoRepository.existsById(productoId)) {
            throw new EntityNotFoundException("Producto no registrado");
        }
        Usuario user = usuarioService.getUsuarioLogueado();
        List<Movimiento> movimientos = movimientoRepository.findBySucursal_Empresa_IdAndProducto_Id(
                user.getEmpresa().getId(), productoId
        );
        return movimientos.stream().map(MovimientoMapper::toResponse).toList();
    }

    public MovimientoResponseDTO obtenerMovimiento(Long id) {
        Usuario user = usuarioService.getUsuarioLogueado();
        Movimiento movimiento = movimientoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Movimiento no encontrado"));

        // Validación de empresa a través de sucursal
        if (!user.getEmpresa().getId().equals(movimiento.getSucursal().getEmpresa().getId())) {
            throw new IllegalArgumentException("No autorizado");
        }

        return MovimientoMapper.toResponse(movimiento);
    }
}