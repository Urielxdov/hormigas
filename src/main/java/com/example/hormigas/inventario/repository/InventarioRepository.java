package com.example.hormigas.inventario.repository;

import com.example.hormigas.inventario.entity.Inventario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InventarioRepository extends JpaRepository<Inventario, Long> {
    List<Inventario> findBySucursalId(Long sucursalId);
    List<Inventario> findBySucursalEmpresaId(Long empresaId);
    Optional<Inventario> findBySucursalIdAndProductoId(Long sucursalId, Long productoId);
}
