package com.example.hormigas.sucursal.repository;

import com.example.hormigas.sucursal.entity.Sucursal;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SucursalRepository extends JpaRepository<Sucursal, Long> {
}
