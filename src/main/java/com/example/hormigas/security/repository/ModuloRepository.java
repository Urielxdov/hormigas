package com.example.hormigas.security.repository;

import com.example.hormigas.security.entity.permiso.Modulo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ModuloRepository extends JpaRepository<Modulo, Long> {
    Optional<Modulo> findByNombre(String nombre);

    default Modulo saveIfNotExists(String nombre) {
        return findByNombre(nombre).orElseGet(() -> save(Modulo.builder().nombre(nombre).build()));
    }
}
