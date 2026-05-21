package com.example.hormigas.motivo.repository;

import com.example.hormigas.empresa.entity.Empresa;
import com.example.hormigas.motivo.entity.MotivoMovimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface MotivoMovimientoRepository extends JpaRepository<MotivoMovimiento, Long> {
    List<MotivoMovimiento> findByEmpresa(Empresa empresa);
    List<MotivoMovimiento> findByEmpresaAndActivoTrue(Empresa empresa);

    @Query("SELECT m FROM MotivoMovimiento m WHERE m.id = :id AND m.empresa.id = :empresaId")
    Optional<MotivoMovimiento> findByIdAndEmpresaId(Long id, Long empresaId);
}
