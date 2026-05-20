package com.example.hormigas.producto.repository;

import com.example.hormigas.producto.entity.Producto;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

    Optional<Producto> findByEmpresaIdAndSku(Long empresaId, String sku);

    Optional<Producto> findByIdAndEmpresaId(Long id, Long empresaId);

    List<Producto> findByEmpresaIdAndActivoTrue(Long empresaId);

    Page<Producto> findByEmpresaIdAndActivoTrue(Long empresaId, Pageable pageable);

    @Modifying
    @Transactional
    @Query("""
        UPDATE Producto p
        SET p.activo = false
        WHERE p.id = :id
        AND p.empresa.id = :empresaId
    """)
    int desactivarProducto(
            @Param("id") Long id,
            @Param("empresaId") Long empresaId
    );

    @Query("""
        SELECT p FROM Producto p
        WHERE p.empresa.id = :empresaId
        AND p.activo = true
        AND (LOWER(p.nombre) LIKE LOWER(CONCAT('%', :q, '%'))
          OR LOWER(p.sku) LIKE LOWER(CONCAT('%', :q, '%')))
        ORDER BY p.nombre ASC
    """)
    List<Producto> buscarPorNombreOSku(@Param("q") String q, @Param("empresaId") Long empresaId);

}