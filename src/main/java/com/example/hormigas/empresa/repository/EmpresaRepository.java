package com.example.hormigas.empresa.repository;

import com.example.hormigas.empresa.entity.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmpresaRepository extends JpaRepository<Empresa, Long> {
    Optional<Empresa> findByRfc(String rfc);
    Optional<Empresa> findByNombre(String nombre);
}
