package com.example.hormigas.movimiento.repository;

import com.example.hormigas.movimiento.entity.Movimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface MovimientoRepository extends JpaRepository<Movimiento, Long>, JpaSpecificationExecutor<Movimiento> {
    List<Movimiento> findByVentaClientIdAndInventarioSucursalEmpresaId(String ventaClientId, Long empresaId);

    List<Movimiento> findByVentaClientIdAndInventarioSucursalId(String ventaClientId, Long sucursalId);

    Optional<Movimiento> findByVentaClientIdAndInventarioSucursalIdAndInventarioProductoId(
            String ventaClientId, Long sucursalId, Long productoId);
}
