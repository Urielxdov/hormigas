package com.example.hormigas.security.repository;

import com.example.hormigas.security.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByCorreoAndActivoTrue(String correo);
    Optional<Usuario> findByCorreo(String correo);
}
