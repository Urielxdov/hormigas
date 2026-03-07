package com.example.hormigas.inventario.repository;

import com.example.hormigas.inventario.entity.Inventario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventarioRepository extends JpaRepository<Inventario, Long> {
}
