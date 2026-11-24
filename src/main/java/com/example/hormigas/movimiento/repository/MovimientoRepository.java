package com.example.hormigas.movimiento.repository;

import com.example.hormigas.movimiento.entity.Movimiento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

/**
 * inventario y usuario son LAZY: las consultas cuyo resultado se mapea a DTO
 * cargan las relaciones con un EntityGraph (una sola consulta, sin N+1).
 */
public interface MovimientoRepository extends JpaRepository<Movimiento, Long>, JpaSpecificationExecutor<Movimiento> {

    @Override
    @EntityGraph(attributePaths = {"inventario.producto", "inventario.sucursal", "usuario"})
    Page<Movimiento> findAll(Specification<Movimiento> spec, Pageable pageable);

    @EntityGraph(attributePaths = {"inventario.producto", "inventario.sucursal", "usuario"})
    List<Movimiento> findByVentaClientIdAndInventarioSucursalEmpresaId(String ventaClientId, Long empresaId);

    @EntityGraph(attributePaths = {"inventario.producto", "inventario.sucursal", "usuario"})
    List<Movimiento> findByVentaClientIdAndInventarioSucursalId(String ventaClientId, Long sucursalId);

    @EntityGraph(attributePaths = {"inventario.producto", "inventario.sucursal", "usuario"})
    Optional<Movimiento> findByVentaClientIdAndInventarioSucursalIdAndInventarioProductoId(
            String ventaClientId, Long sucursalId, Long productoId);
}
