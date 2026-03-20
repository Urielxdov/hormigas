package com.example.hormigas.security.domain.repository;

import com.example.hormigas.security.domain.permiso.Modulo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ModuloRepository extends JpaRepository<Modulo, Long> {
    Optional<Modulo> findByNombre(String nombre);

    default Modulo saveIfNotExists(String nombre) {
        return findByNombre(nombre).orElseGet(() -> {
            Modulo modulo = new Modulo();
            modulo.setNombre(nombre); // setter explícito
            return save(modulo);
        });
    }
}
