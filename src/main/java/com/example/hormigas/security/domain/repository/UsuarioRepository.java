package com.example.hormigas.security.domain.repository;

import com.example.hormigas.security.domain.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByCorreoAndActivoTrue(String correo);
    Optional<Usuario> findByCorreo(String correo);
}
