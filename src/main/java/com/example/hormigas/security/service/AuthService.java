package com.example.hormigas.security.service;

import com.example.hormigas.security.dto.usuario.LoginRequestDTO;
import com.example.hormigas.security.dto.usuario.LoginResponseDTO;
import com.example.hormigas.security.entity.Usuario;
import com.example.hormigas.security.mapper.UsuarioMapper;
import com.example.hormigas.security.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public LoginResponseDTO login(LoginRequestDTO dto) {
        Usuario usuario = usuarioRepository
                .findByCorreoAndActivoTrue(dto.correo())
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));

        if (!passwordEncoder.matches(dto.password(), usuario.getPasswordHash())) {
            throw new RuntimeException("Contraseña incorrecta");
        }

        usuario.setUltimoAcceso(LocalDateTime.now());
        usuarioRepository.save(usuario);
        return UsuarioMapper.toResponse(usuario);
    }
}