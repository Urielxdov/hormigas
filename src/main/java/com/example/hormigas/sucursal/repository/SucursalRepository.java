package com.example.hormigas.sucursal.repository;

import com.example.hormigas.sucursal.entity.Sucursal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SucursalRepository extends JpaRepository<Sucursal, Long> {
    List<Sucursal> findByEmpresaId(Long empresaId);
    Optional<Sucursal> findByIdAndEmpresaId(Long id, Long empresaId);
}
