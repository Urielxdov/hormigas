package com.example.hormigas.inventario.repository;

import com.example.hormigas.inventario.entity.Inventario;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface InventarioRepository extends JpaRepository<Inventario, Long>, JpaSpecificationExecutor<Inventario> {
    List<Inventario> findBySucursalId(Long sucursalId);
    List<Inventario> findBySucursalEmpresaId(Long empresaId);
    Optional<Inventario> findBySucursalIdAndProductoId(Long sucursalId, Long productoId);

    @Modifying
    @Transactional
    @Query("""
            UPDATE Inventario i
            SET i.sucursal.id = :sucursalId
            WHERE i.id = :inventarioId
            AND i.producto.empresa.id = :empresaId
            AND i.sucursal.empresa.id = :empresaId
    """)
    int asignarASucursal(Long inventarioId, Long sucursalId, Long empresaId);
}
