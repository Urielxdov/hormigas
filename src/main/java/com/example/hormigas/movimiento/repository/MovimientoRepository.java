package com.example.hormigas.movimiento.repository;

import com.example.hormigas.movimiento.entity.Movimiento;
import com.example.hormigas.reporte.dto.ProductoTopDTO;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface MovimientoRepository extends JpaRepository<Movimiento, Long>, JpaSpecificationExecutor<Movimiento> {

    @Query("""
        SELECT new com.example.hormigas.reporte.dto.ProductoTopDTO(
            i.producto.id,
            i.producto.nombre,
            i.producto.sku,
            SUM(CASE WHEN m.tipoMovimiento IN (
                com.example.hormigas.movimiento.entity.TipoMovimiento.COMPRA,
                com.example.hormigas.movimiento.entity.TipoMovimiento.DEVOLUCION,
                com.example.hormigas.movimiento.entity.TipoMovimiento.DEVOLUCION_CLIENTE,
                com.example.hormigas.movimiento.entity.TipoMovimiento.TRASLADO_ENTRADA
            ) THEN m.cantidad ELSE 0 END),
            SUM(CASE WHEN m.tipoMovimiento IN (
                com.example.hormigas.movimiento.entity.TipoMovimiento.VENTA,
                com.example.hormigas.movimiento.entity.TipoMovimiento.MERMA,
                com.example.hormigas.movimiento.entity.TipoMovimiento.DEVOLUCION_PROVEEDOR,
                com.example.hormigas.movimiento.entity.TipoMovimiento.TRASLADO_SALIDA
            ) THEN m.cantidad ELSE 0 END)
        )
        FROM Movimiento m JOIN m.inventario i
        WHERE i.sucursal.empresa.id = :empresaId
        AND (:sucursalId IS NULL OR i.sucursal.id = :sucursalId)
        AND (:fechaInicio IS NULL OR m.fecha >= :fechaInicio)
        AND (:fechaFin IS NULL OR m.fecha <= :fechaFin)
        GROUP BY i.producto.id, i.producto.nombre, i.producto.sku
        ORDER BY (SUM(m.cantidad)) DESC
    """)
    List<ProductoTopDTO> findProductosTop(
            @Param("empresaId") Long empresaId,
            @Param("sucursalId") Long sucursalId,
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin,
            Pageable pageable
    );
}
