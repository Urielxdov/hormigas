package com.example.hormigas.traslado.service;

import com.example.hormigas.inventario.dto.AlertaStock;
import com.example.hormigas.inventario.entity.Inventario;
import com.example.hormigas.inventario.repository.InventarioRepository;
import com.example.hormigas.inventario.service.AlertaStockService;
import com.example.hormigas.movimiento.entity.Movimiento;
import com.example.hormigas.movimiento.entity.TipoMovimiento;
import com.example.hormigas.movimiento.mapper.MovimientoMapper;
import com.example.hormigas.movimiento.repository.MovimientoRepository;
import com.example.hormigas.security.domain.Usuario;
import com.example.hormigas.security.domain.services.UsuarioService;
import com.example.hormigas.traslado.dto.CrearTrasladoDTO;
import com.example.hormigas.traslado.dto.TrasladoResponseDTO;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class TrasladoService {

    private final InventarioRepository inventarioRepository;
    private final MovimientoRepository movimientoRepository;
    private final UsuarioService usuarioService;
    private final AlertaStockService alertaStockService;

    public TrasladoService(
            InventarioRepository inventarioRepository,
            MovimientoRepository movimientoRepository,
            UsuarioService usuarioService,
            AlertaStockService alertaStockService
    ) {
        this.inventarioRepository = inventarioRepository;
        this.movimientoRepository = movimientoRepository;
        this.usuarioService = usuarioService;
        this.alertaStockService = alertaStockService;
    }

    @Transactional
    public TrasladoResponseDTO crear(CrearTrasladoDTO dto) {
        Usuario user = usuarioService.getUsuarioLogueado();

        Inventario origen = inventarioRepository
                .findBySucursalIdAndProductoId(dto.sucursalOrigenId(), dto.productoId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "No existe inventario del producto en la sucursal origen"));

        Inventario destino = inventarioRepository
                .findBySucursalIdAndProductoId(dto.sucursalDestinoId(), dto.productoId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "No existe inventario del producto en la sucursal destino"));

        if (!origen.getSucursal().getEmpresa().getId().equals(user.getEmpresa().getId()) ||
            !destino.getSucursal().getEmpresa().getId().equals(user.getEmpresa().getId())) {
            throw new IllegalArgumentException("Las sucursales no pertenecen a la empresa del usuario");
        }

        int stockOrigen = origen.getStockActual();
        int nuevoStockOrigen = TipoMovimiento.TRASLADO_SALIDA.aplicar(stockOrigen, dto.cantidad());
        if (nuevoStockOrigen < 0) {
            throw new IllegalArgumentException("Stock insuficiente en sucursal origen");
        }

        int stockDestino = destino.getStockActual();
        int nuevoStockDestino = TipoMovimiento.TRASLADO_ENTRADA.aplicar(stockDestino, dto.cantidad());
        if (nuevoStockDestino > destino.getStockMaximo()) {
            throw new IllegalArgumentException(
                    "Stock resultante en destino (" + nuevoStockDestino + ") excede el máximo (" + destino.getStockMaximo() + ")");
        }

        String referenciaTraslado = dto.referencia() != null ? dto.referencia() : UUID.randomUUID().toString();
        LocalDateTime ahora = LocalDateTime.now();

        origen.setStockActual(nuevoStockOrigen);
        origen.setUltimaActualizacion(ahora);
        inventarioRepository.save(origen);

        destino.setStockActual(nuevoStockDestino);
        destino.setUltimaActualizacion(ahora);
        inventarioRepository.save(destino);

        Movimiento salida = new Movimiento();
        salida.setInventario(origen);
        salida.setTipoMovimiento(TipoMovimiento.TRASLADO_SALIDA);
        salida.setCantidad(dto.cantidad());
        salida.setStockAnterior(stockOrigen);
        salida.setStockNuevo(nuevoStockOrigen);
        salida.setUsuario(user);
        salida.setFecha(ahora);
        salida.setReferencia(referenciaTraslado);
        movimientoRepository.save(salida);

        Movimiento entrada = new Movimiento();
        entrada.setInventario(destino);
        entrada.setTipoMovimiento(TipoMovimiento.TRASLADO_ENTRADA);
        entrada.setCantidad(dto.cantidad());
        entrada.setStockAnterior(stockDestino);
        entrada.setStockNuevo(nuevoStockDestino);
        entrada.setUsuario(user);
        entrada.setFecha(ahora);
        entrada.setReferencia(referenciaTraslado);
        movimientoRepository.save(entrada);

        AlertaStock alertaOrigen = alertaStockService.evaluar(origen);
        AlertaStock alertaDestino = alertaStockService.evaluar(destino);

        return new TrasladoResponseDTO(
                MovimientoMapper.toResponse(salida, alertaOrigen),
                MovimientoMapper.toResponse(entrada, alertaDestino),
                referenciaTraslado
        );
    }
}
