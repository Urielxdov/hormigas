package com.example.hormigas.reporte.service;

import com.example.hormigas.inventario.entity.Inventario;
import com.example.hormigas.inventario.repository.InventarioRepository;
import com.example.hormigas.movimiento.dto.MovimientoFiltroDTO;
import com.example.hormigas.movimiento.dto.MovimientoResponseDTO;
import com.example.hormigas.movimiento.mapper.MovimientoMapper;
import com.example.hormigas.movimiento.repository.MovimientoRepository;
import com.example.hormigas.movimiento.repository.MovimientoSpecification;
import com.example.hormigas.reporte.dto.DetalleValorDTO;
import com.example.hormigas.reporte.dto.ProductoTopDTO;
import com.example.hormigas.reporte.dto.ValorInventarioDTO;
import com.example.hormigas.security.domain.Usuario;
import com.example.hormigas.security.domain.services.UsuarioService;
import com.example.hormigas.sucursal.entity.Sucursal;
import com.example.hormigas.sucursal.repository.SucursalRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReporteService {

    private final MovimientoRepository movimientoRepository;
    private final InventarioRepository inventarioRepository;
    private final SucursalRepository sucursalRepository;
    private final UsuarioService usuarioService;

    public ReporteService(
            MovimientoRepository movimientoRepository,
            InventarioRepository inventarioRepository,
            SucursalRepository sucursalRepository,
            UsuarioService usuarioService
    ) {
        this.movimientoRepository = movimientoRepository;
        this.inventarioRepository = inventarioRepository;
        this.sucursalRepository = sucursalRepository;
        this.usuarioService = usuarioService;
    }

    public List<MovimientoResponseDTO> movimientosPorPeriodo(
            LocalDateTime fechaInicio, LocalDateTime fechaFin,
            Long sucursalId, Long productoId, int page, int size
    ) {
        Usuario user = usuarioService.getUsuarioLogueado();
        MovimientoFiltroDTO filtro = new MovimientoFiltroDTO(
                sucursalId, productoId, null, null, fechaInicio, fechaFin);

        return movimientoRepository
                .findAll(MovimientoSpecification.conFiltros(user.getEmpresa().getId(), filtro),
                        PageRequest.of(page, size))
                .stream()
                .map(m -> MovimientoMapper.toResponse(m, null))
                .toList();
    }

    public List<ProductoTopDTO> productosTop(
            LocalDateTime fechaInicio, LocalDateTime fechaFin,
            Long sucursalId, int limite
    ) {
        Usuario user = usuarioService.getUsuarioLogueado();
        return movimientoRepository.findProductosTop(
                user.getEmpresa().getId(),
                sucursalId,
                fechaInicio,
                fechaFin,
                PageRequest.of(0, limite)
        );
    }

    public ValorInventarioDTO valorInventario(Long sucursalId) {
        Usuario user = usuarioService.getUsuarioLogueado();

        Sucursal sucursal = sucursalRepository.findById(sucursalId)
                .orElseThrow(() -> new EntityNotFoundException("Sucursal no encontrada"));

        if (!sucursal.getEmpresa().getId().equals(user.getEmpresa().getId())) {
            throw new IllegalArgumentException("Sucursal no pertenece a la empresa");
        }

        List<Inventario> inventarios = inventarioRepository.findBySucursalId(sucursalId);

        List<DetalleValorDTO> detalle = inventarios.stream().map(inv -> {
            BigDecimal precio = inv.getProducto().getPrecio();
            boolean sinPrecio = precio == null;
            BigDecimal precioEfectivo = sinPrecio ? BigDecimal.ZERO : precio;
            BigDecimal valorLinea = precioEfectivo.multiply(BigDecimal.valueOf(inv.getStockActual()));
            return new DetalleValorDTO(
                    inv.getProducto().getId(),
                    inv.getProducto().getNombre(),
                    inv.getProducto().getSku(),
                    inv.getStockActual(),
                    precio,
                    valorLinea,
                    sinPrecio
            );
        }).toList();

        BigDecimal valorTotal = detalle.stream()
                .map(DetalleValorDTO::valorLinea)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long conPrecio = detalle.stream().filter(d -> !d.sinPrecio()).count();
        long sinPrecio = detalle.stream().filter(DetalleValorDTO::sinPrecio).count();

        return new ValorInventarioDTO(
                sucursalId,
                sucursal.getNombre(),
                valorTotal,
                (int) conPrecio,
                (int) sinPrecio,
                detalle
        );
    }
}
