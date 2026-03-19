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
import com.example.hormigas.security.domain.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class MovimientoService {

    private final InventarioRepository inventarioRepository;
    private final MovimientoRepository movimientoRepository;
    private final MotivoMovimientoRepository motivoMovimientoRepository;
    private final UsuarioRepository usuarioRepository;

    public MovimientoService (
            InventarioRepository inventarioRepository,
            MovimientoRepository movimientoRepository,
            UsuarioRepository usuarioRepository,
            MotivoMovimientoRepository motivoMovimientoRepository
    ) {
        this.inventarioRepository = inventarioRepository;
        this.movimientoRepository = movimientoRepository;
        this.usuarioRepository = usuarioRepository;
        this.motivoMovimientoRepository = motivoMovimientoRepository;
    }

    // Registrar movimiento de entrada - agregar stock
    @Transactional
    public MovimientoResponseDTO registrarMovimiento(CrearMovimientoDTO dto, Long usuarioId) {

        Inventario inventario = inventarioRepository
                .findBySucursalIdAndProductoId(dto.sucursalId(), dto.productoId())
                .orElseThrow();

        MotivoMovimiento motivo = motivoMovimientoRepository
                .findById(dto.motivoId())
                .orElseThrow();

        TipoMovimiento tipo = motivo.getTipoMovimiento();

        int stockActual = inventario.getStockActual();
        int nuevoStock = tipo.aplicar(stockActual, dto.cantidad());

        if (nuevoStock < 0) {
            throw new IllegalArgumentException("Stock insuficiente");
        }

        inventario.setStockActual(nuevoStock);
        inventarioRepository.save(inventario);

        Movimiento movimiento = new Movimiento();
        movimiento.setProducto(inventario.getProducto());
        movimiento.setSucursal(inventario.getSucursal());
        movimiento.setMotivo(motivo);
        movimiento.setTipoMovimiento(tipo);
        movimiento.setCantidad(dto.cantidad());
        movimiento.setStockAnterior(stockActual);
        movimiento.setStockNuevo(nuevoStock);
        movimiento.setUsuario(usuarioRepository.findById(usuarioId).orElseThrow());

        movimientoRepository.save(movimiento);

        return MovimientoMapper.toResponse(movimiento);
    }



    // Registrar movimiento de salida - cuando se consume o vende stock

    // Registrar movimiento de ajuste - correcion de inventario por conteo fisico o errores

    // Ver historial de mocimientos - consultar mocimientos por producto, sucursal, usuario o ranfos de fechas

    // Configuracion motivos de movimeinto - crear motivos personalizables de entrada/salida/ajuste
}
