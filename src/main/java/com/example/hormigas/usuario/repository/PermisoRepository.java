package com.example.hormigas.usuario.repository;

import com.example.hormigas.usuario.entity.Permiso;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PermisoRepository extends JpaRepository<Permiso, Long> {
}
