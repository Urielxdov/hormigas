package com.example.hormigas.motivo.repository;

import com.example.hormigas.empresa.entity.Empresa;
import com.example.hormigas.motivo.entity.MotivoMovimiento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MotivoMovimientoRepository extends JpaRepository<MotivoMovimiento, Long> {
    List<MotivoMovimiento> findByEmpresa(Empresa empresa);
}
