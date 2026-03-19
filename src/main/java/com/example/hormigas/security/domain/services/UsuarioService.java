package com.example.hormigas.security.domain.services;

import com.example.hormigas.empresa.repository.EmpresaRepository;
import com.example.hormigas.security.infrastructure.dtos.CreateUsuarioDTO;
import com.example.hormigas.security.infrastructure.dtos.UsuarioResponseDTO;
import com.example.hormigas.security.infrastructure.dtos.UsuarioUpdateDTO;
import com.example.hormigas.security.domain.Usuario;
import com.example.hormigas.security.application.services.AuthServiceImpl;
import com.example.hormigas.security.application.mapper.UsuarioMapper;
import com.example.hormigas.security.domain.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UsuarioService implements UserDetailsService {
    private static final Logger logger = LogManager.getLogger(AuthServiceImpl.class);

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmpresaRepository empresaRepository;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, EmpresaRepository empresaRepository) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.empresaRepository = empresaRepository;
    }
    @Override
    public UserDetails loadUserByUsername(String correo) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        return User.builder()
                .username(usuario.getCorreo())
                .password(usuario.getPasswordHash())
                .disabled(!usuario.isActivo())
                .roles("USER") // despues lo debemos cambiar a los que se usan aqui
                .build();
    }

    // Crear usuario
    public UsuarioResponseDTO nuevoUsuario(CreateUsuarioDTO dto) {
        if (usuarioRepository.findByCorreo(dto.correo()).isPresent()) {
            throw new IllegalArgumentException("Correo ya registrado");
        }

        // Usuario ya logeado
        Usuario usuarioActual = getUsuarioLogueado();

        Usuario usuario = new Usuario();
        usuario.setCorreo(dto.correo());
        usuario.setNombre(dto.nombre());
        usuario.setPasswordHash(passwordEncoder.encode(dto.password()));
        usuario.setEmpresa(usuarioActual.getEmpresa());

        usuarioRepository.save(usuario);

        return UsuarioMapper.toResponse(usuario);
    }

    // Actualizar usuario
    public UsuarioResponseDTO actualizarUsuario (UsuarioUpdateDTO dto) {
        //Usuario usuario = authService.getUsuarioLogueado();
        Usuario usuario = usuarioRepository.findById(1L)
                .orElseThrow(() -> new RuntimeException("Pruebas"));
        if (dto.nombre() != null && !dto.nombre().isBlank()) {
            usuario.setNombre(dto.nombre());
        }

        if (dto.correo() != null && !dto.correo().isBlank()) {
            // Verificamos que no exista otro usuario con el mismo correo
            usuarioRepository.findByCorreo(dto.correo())
                    .filter(u -> !u.getId().equals(usuario.getId()))
                    .ifPresent(u -> { throw new IllegalArgumentException("Correo ya registrado"); });

        }

        if (dto.password() != null && !dto.password().isBlank()) {

        }
        return new UsuarioResponseDTO(1L, "Prueba", "Pruebas", 1L);
    }

    // Eliminar usuario

    // Iniciar Sesion

    // asginar roles/permisos

    // Ver usuarios

    // Buscar usuario
    public Usuario obtenerUsuario (String correo) {
        return usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new EntityNotFoundException("Correo no registrado"));
    }

    public Usuario obtenerUsuario(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("[USER] : User not found with id {}", id);
                    return new EntityNotFoundException("No se encontro el usuario");
                });
    }

    public void updateLastLogin(Usuario usuario) {
        usuario.setUltimoAcceso(LocalDateTime.now());
        usuarioRepository.save(usuario);
    }

    public void updateLastLogin(String correo) {
        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> {
                    logger.error("[USER] : User not found with correo {}", correo);
                    return new EntityNotFoundException("No se encontro el usuario");
                });
        updateLastLogin(usuario);
    }

    public Usuario getUsuarioLogueado () {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new RuntimeException("No hay usuario autenticado");
        }
        // auth.getPrincipal() normalmente es un UserDetails
        String correo = auth.getName();
        return usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new RuntimeException("Usuario logueado no encotrado"));
    }
}
