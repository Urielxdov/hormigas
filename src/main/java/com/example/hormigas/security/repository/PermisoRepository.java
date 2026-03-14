package com.example.hormigas.security.repository;

import com.example.hormigas.security.entity.permiso.Permiso;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PermisoRepository extends JpaRepository<Permiso, Long> {
}
