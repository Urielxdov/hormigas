package com.example.hormigas.security.service;

import com.example.hormigas.empresa.entity.Empresa;
import com.example.hormigas.empresa.repository.EmpresaRepository;
import com.example.hormigas.security.dto.usuario.LoginResponseDTO;
import com.example.hormigas.security.dto.usuario.NuevoUsuarioDto;
import com.example.hormigas.security.dto.usuario.UsuarioModificadoDTO;
import com.example.hormigas.security.entity.Usuario;
import com.example.hormigas.security.mapper.UsuarioMapper;
import com.example.hormigas.security.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
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
    public LoginResponseDTO nuevoUsuario(NuevoUsuarioDto dto) {
        if (usuarioRepository.findByCorreo(dto.correo()).isPresent()) {
            throw new IllegalArgumentException("Correo ya registrado");
        }

        // Usuario ya logeado
        Usuario creador = authService.getUsuarioLogueado();

        // Extraccion de la empresa de usuario
        Empresa empresa = creador.getEmpresa();

        Usuario usuario = new Usuario();
        usuario.setCorreo(dto.correo());
        usuario.setNombre(dto.nombre());
        usuario.setPasswordHash(passwordEncoder.encode(dto.password()));
        usuario.setEmpresa(empresa);

        usuarioRepository.save(usuario);

        return UsuarioMapper.toResponse(usuario);
    }

    // Actualizar usuario
    public LoginResponseDTO actualizarUsuario (UsuarioModificadoDTO dto) {
        Usuario usuario = authService.getUsuarioLogueado();

        if (dto.nombre() != null && !dto.nombre().isBlank()) {
            usuario.setNombre(dto.nombre());
        }

        if (dto.correo() != null && !dto.correo().isBlank()) {
            // Verificamos que no exista otro usuario con el mismo correo
            usuarioRepository.findByCorreo(dto.getCorreo())
                    .filter(u -> !u.getId().equals(usuario.getId()))
                    .ifPresent(u -> { throw new IllegalArgumentException("Correo ya registrado"); });

        }

        if (dto.passwordHash() != null && !dto.passwordHash().isBlank()) {

        }
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
}
