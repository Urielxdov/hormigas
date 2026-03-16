package com.example.hormigas.security.service;

import com.example.hormigas.empresa.entity.Empresa;
import com.example.hormigas.empresa.repository.EmpresaRepository;
import com.example.hormigas.security.dto.usuario.CreateUsuarioDTO;
import com.example.hormigas.security.dto.usuario.UsuarioResponseDTO;
import com.example.hormigas.security.dto.usuario.UsuarioUpdateDTO;
import com.example.hormigas.security.entity.Usuario;
import com.example.hormigas.security.mapper.UsuarioMapper;
import com.example.hormigas.security.repository.UsuarioRepository;
import com.example.hormigas.security.service.interfaces.AuthService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UsuarioService implements UserDetailsService {
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmpresaRepository empresaRepository;
    private final AuthService authService;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, EmpresaRepository empresaRepository, AuthService authService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.empresaRepository = empresaRepository;
        this.authService = authService;
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
    public Usuario obtenerUsurio (String correo) {
        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new EntityNotFoundException("Correo no registrado"));

        return usuario;
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
