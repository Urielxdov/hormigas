package com.example.hormigas.movimiento.repository;

import com.example.hormigas.movimiento.entity.Movimiento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MovimientoRepository extends JpaRepository<Movimiento, Long> {

    // Movimientos por empresa (a través de sucursal)
    List<Movimiento> findBySucursal_Empresa_Id(Long empresaId);

    // Movimientos por empresa y sucursal
    List<Movimiento> findBySucursal_Empresa_IdAndSucursal_Id(Long empresaId, Long sucursalId);

    // Movimientos por empresa y producto
    List<Movimiento> findBySucursal_Empresa_IdAndProducto_Id(Long empresaId, Long productoId);

    // Si quieres, también por sucursal y producto:
    List<Movimiento> findBySucursal_IdAndProducto_Id(Long sucursalId, Long productoId);
}
