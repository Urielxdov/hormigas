package com.example.hormigas.inventario.repository;

import com.example.hormigas.inventario.entity.Inventario;
import jakarta.persistence.LockModeType;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface InventarioRepository extends JpaRepository<Inventario, Long>, JpaSpecificationExecutor<Inventario> {
    /** producto y sucursal son LAZY: los listados los cargan en la misma consulta. */
    @Override
    @EntityGraph(attributePaths = {"producto", "sucursal"})
    List<Inventario> findAll(Specification<Inventario> spec);

    List<Inventario> findBySucursalId(Long sucursalId);
    List<Inventario> findBySucursalEmpresaId(Long empresaId);
    Optional<Inventario> findBySucursalIdAndProductoId(Long sucursalId, Long productoId);

    /** Lee el inventario bloqueando la fila (SELECT ... FOR UPDATE) para modificar el stock sin carreras. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM Inventario i WHERE i.sucursal.id = :sucursalId AND i.producto.id = :productoId")
    Optional<Inventario> findForUpdateBySucursalIdAndProductoId(Long sucursalId, Long productoId);

    @Modifying
    @Transactional
    @Query("""
            UPDATE Inventario i
            SET i.sucursal.id = :sucursalId
            WHERE i.id = :inventarioId
            AND i.producto.empresa.id = :empresaId
            AND i.sucursal.empresa.id = :empresaId
            AND EXISTS (
                SELECT 1 FROM Sucursal s
                WHERE s.id = :sucursalId AND s.empresa.id = :empresaId
            )
    """)
    int asignarASucursal(Long inventarioId, Long sucursalId, Long empresaId);
}
