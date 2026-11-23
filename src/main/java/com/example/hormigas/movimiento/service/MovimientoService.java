package com.example.hormigas.movimiento.service;

import com.example.hormigas.inventario.entity.Inventario;
import com.example.hormigas.inventario.repository.InventarioRepository;
import com.example.hormigas.movimiento.dto.CrearMovimientoDTO;
import com.example.hormigas.movimiento.dto.MovimientoFiltroDTO;
import com.example.hormigas.movimiento.dto.MovimientoResponseDTO;
import com.example.hormigas.movimiento.dto.ItemRechazadoDTO;
import com.example.hormigas.movimiento.dto.VentaBatchDTO;
import com.example.hormigas.movimiento.dto.VentaBatchResponseDTO;
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

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class MovimientoService {

    private final InventarioRepository inventarioRepository;
    private final MovimientoRepository movimientoRepository;
    private final SucursalRepository sucursalRepository;
    private final UsuarioService usuarioService;
    private final ProductoRepository productoRepository;
    private final TransactionTemplate transaccionPorItem;

    public MovimientoService(
            InventarioRepository inventarioRepository,
            MovimientoRepository movimientoRepository,
            UsuarioService usuarioService,
            SucursalRepository sucursalRepository,
            ProductoRepository productoRepository,
            PlatformTransactionManager transactionManager
    ) {
        this.transaccionPorItem = new TransactionTemplate(transactionManager);
        this.transaccionPorItem.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
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
                sucursal.getId(), dto.productoId(), dto.tipoMovimiento(), dto.cantidad(), dto.referencia(), user,
                LocalDateTime.now(), null, null
        );
        return MovimientoMapper.toResponse(movimiento);
    }

    /**
     * Registra una venta (posiblemente offline) de forma idempotente y parcial.
     * <ul>
     *   <li>Cada item se procesa en su propia transaccion: uno invalido no revierte los demas.</li>
     *   <li>Si {@code clientId} ya fue procesado, los items ya registrados no se repiten.</li>
     * </ul>
     */
    public VentaBatchResponseDTO registrarVentaBatch(VentaBatchDTO dto) {
        Usuario user = usuarioService.getUsuarioLogueado();
        Long empresaId = user.getEmpresa().getId();
        Sucursal sucursal = sucursalRepository.findByIdAndEmpresaId(dto.sucursalId(), empresaId)
                .orElseThrow(() -> new EntityNotFoundException("No se encontro la sucursal"));
        if (!sucursal.isActiva()) throw new IllegalArgumentException("La sucursal no esta activa");

        String clientId = (dto.clientId() == null || dto.clientId().isBlank()) ? null : dto.clientId();
        String referencia = dto.referencia() != null ? dto.referencia() : clientId;
        LocalDateTime fecha = resolverFecha(dto.fecha());

        List<MovimientoResponseDTO> procesados = new ArrayList<>();
        List<ItemRechazadoDTO> rechazados = new ArrayList<>();

        // Movimientos ya registrados por un intento previo con el mismo clientId
        Set<Long> yaRegistrados = new HashSet<>();
        if (clientId != null) {
            for (Movimiento previo : movimientoRepository
                    .findByVentaClientIdAndInventarioSucursalEmpresaId(clientId, empresaId)) {
                yaRegistrados.add(previo.getInventario().getProducto().getId());
                procesados.add(MovimientoMapper.toResponse(previo));
            }
        }

        boolean hayNuevos = false;
        for (VentaBatchItemDTO item : fusionarItems(dto.items())) {
            if (yaRegistrados.contains(item.productoId())) continue;
            hayNuevos = true;
            try {
                Movimiento m = transaccionPorItem.execute(status -> procesarItemMovimiento(
                        sucursal.getId(), item.productoId(), TipoMovimiento.VENTA, item.cantidad(),
                        referencia, user, fecha, item.precioUnitario(), clientId
                ));
                procesados.add(MovimientoMapper.toResponse(m));
            } catch (EntityNotFoundException | IllegalArgumentException e) {
                rechazados.add(new ItemRechazadoDTO(item.productoId(), item.cantidad(), e.getMessage()));
            } catch (DataIntegrityViolationException e) {
                // Otro request con el mismo clientId gano la carrera: el item ya quedo registrado.
                rechazados.add(new ItemRechazadoDTO(item.productoId(), item.cantidad(), "Item ya registrado"));
            }
        }
        boolean duplicada = clientId != null && !hayNuevos && !yaRegistrados.isEmpty();
        return new VentaBatchResponseDTO(clientId, duplicada, procesados, rechazados);
    }

    /** Suma cantidades del mismo producto para no violar la unicidad (clientId, inventario). */
    private static List<VentaBatchItemDTO> fusionarItems(List<VentaBatchItemDTO> items) {
        Map<Long, VentaBatchItemDTO> porProducto = new LinkedHashMap<>();
        for (VentaBatchItemDTO item : items) {
            porProducto.merge(item.productoId(), item, (a, b) -> new VentaBatchItemDTO(
                    a.productoId(), a.cantidad() + b.cantidad(), a.precioUnitario() != null ? a.precioUnitario() : b.precioUnitario()
            ));
        }
        return new ArrayList<>(porProducto.values());
    }

    /** Usa la fecha del cliente (venta offline) salvo que falte o sea futura. */
    private static LocalDateTime resolverFecha(OffsetDateTime fechaCliente) {
        LocalDateTime ahora = LocalDateTime.now();
        if (fechaCliente == null) return ahora;
        LocalDateTime fecha = fechaCliente.atZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
        return fecha.isAfter(ahora) ? ahora : fecha;
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
            Usuario user,
            LocalDateTime fecha,
            BigDecimal precioUnitario,
            String ventaClientId
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
        movimiento.setFecha(fecha);
        movimiento.setPrecioUnitario(precioUnitario != null ? precioUnitario : inventario.getProducto().getPrecio());
        movimiento.setVentaClientId(ventaClientId);
        movimientoRepository.save(movimiento);

        return movimiento;
    }
}
