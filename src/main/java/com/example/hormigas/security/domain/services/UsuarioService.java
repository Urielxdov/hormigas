package com.example.hormigas.security.domain.services;

import com.example.hormigas.empresa.entity.Empresa;
import com.example.hormigas.empresa.repository.EmpresaRepository;
import com.example.hormigas.security.domain.Role;
import com.example.hormigas.security.infrastructure.dtos.CreateUsuarioDTO;
import com.example.hormigas.security.infrastructure.dtos.UsuarioResponseDTO;
import com.example.hormigas.security.infrastructure.dtos.UsuarioUpdateDTO;
import com.example.hormigas.security.domain.Usuario;
import com.example.hormigas.security.application.services.AuthServiceImpl;
import com.example.hormigas.security.application.mapper.UsuarioMapper;
import com.example.hormigas.security.domain.repository.UsuarioRepository;
import com.example.hormigas.sucursal.entity.Sucursal;
import com.example.hormigas.sucursal.repository.SucursalRepository;
import jakarta.persistence.EntityNotFoundException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UsuarioService implements UserDetailsService {
    private static final Logger logger = LogManager.getLogger(AuthServiceImpl.class);

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmpresaRepository empresaRepository;
    private final SucursalRepository sucursalRepository;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder,
                          EmpresaRepository empresaRepository, SucursalRepository sucursalRepository) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.empresaRepository = empresaRepository;
        this.sucursalRepository = sucursalRepository;
    }
    @Override
    public UserDetails loadUserByUsername(String correo) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        return User.builder()
                .username(usuario.getCorreo())
                .password(usuario.getPasswordHash())
                .disabled(!usuario.isActivo())
                // Empresa desactivada: sus usuarios no pueden entrar ni usar tokens ya emitidos
                .accountLocked(usuario.getEmpresa() == null || !usuario.getEmpresa().isActivo())
                .authorities(usuario.getAuthorities()) // despues lo debemos cambiar a los que se usan aqui
                .build();
    }

    // Crear usuario
    // SUPER_ADMIN crea el ADMIN de una empresa; ADMIN crea cajeros (USER) de su empresa,
    // que necesitan sucursal para poder vender en el POS.
    public UsuarioResponseDTO nuevoUsuario(CreateUsuarioDTO dto) {
        String correo = dto.correo().trim().toLowerCase();
        if (usuarioRepository.findByCorreo(correo).isPresent()) {
            logger.error("[USER] : The email {} already exist", correo);
            throw new IllegalArgumentException("Correo ya registrado");
        }

        Usuario usuarioActual = getUsuarioLogueado();
        Empresa empresaDestino;
        Sucursal sucursal = null;
        Role role;

        if (tieneRol(usuarioActual, Role.SUPER_ADMIN)) {
            if (dto.empresaId() == null) {
                logger.error("[SYS] : The company is required for super_admin");
                throw new IllegalArgumentException("La empresa es obligatoria para el superusuario");
            }
            empresaDestino = empresaRepository.findById(dto.empresaId())
                    .orElseThrow(() -> new EntityNotFoundException("Empresa no encontrada"));
            role = Role.ADMIN;
        } else {
            empresaDestino = usuarioActual.getEmpresa();
            if (dto.sucursalId() == null) {
                throw new IllegalArgumentException("Selecciona la sucursal donde trabajará el cajero");
            }
            sucursal = buscarSucursalDeEmpresa(dto.sucursalId(), empresaDestino.getId());
            role = Role.USER;
        }

        Usuario usuario = new Usuario();
        usuario.setCorreo(correo);
        usuario.setNombre(dto.nombre().trim());
        usuario.setPasswordHash(passwordEncoder.encode(dto.password()));
        usuario.setEmpresa(empresaDestino);
        usuario.setSucursal(sucursal);
        usuario.setActivo(true);
        usuario.addRol(role);

        usuarioRepository.save(usuario);
        logger.info("[USER] : User created with role {}", role);
        return UsuarioMapper.toResponse(usuario);
    }

    private Sucursal buscarSucursalDeEmpresa(Long sucursalId, Long empresaId) {
        return sucursalRepository.findByIdAndEmpresaId(sucursalId, empresaId)
                .orElseThrow(() -> new EntityNotFoundException("Sucursal no encontrada en esta empresa"));
    }

    /** Usuario de la misma empresa que el logueado (SUPER_ADMIN: cualquiera). */
    private Usuario usuarioGestionable(Usuario actual, Long id) {
        Usuario usuario = obtenerUsuario(id);
        if (!tieneRol(actual, Role.SUPER_ADMIN)
                && (usuario.getEmpresa() == null
                    || !usuario.getEmpresa().getId().equals(actual.getEmpresa().getId())
                    || tieneRol(usuario, Role.SUPER_ADMIN))) {
            throw new EntityNotFoundException("No se encontro el usuario");
        }
        return usuario;
    }

    // Activar / desactivar usuario (uno desactivado no puede iniciar sesion ni usar su token)
    public UsuarioResponseDTO toggleActivo(Long id) {
        Usuario actual = getUsuarioLogueado();
        Usuario usuario = usuarioGestionable(actual, id);
        if (usuario.getId().equals(actual.getId())) {
            throw new IllegalArgumentException("No puedes desactivar tu propia cuenta");
        }
        usuario.setActivo(!usuario.isActivo());
        logger.info("[USER] : User {} {}", id, usuario.isActivo() ? "activated" : "deactivated");
        return UsuarioMapper.toResponse(usuarioRepository.save(usuario));
    }

    public boolean existeCorreo(String correo) {
        return correo != null && usuarioRepository.findByCorreo(correo.trim().toLowerCase()).isPresent();
    }

    public UsuarioResponseDTO nuevoAdminParaEmpresa(CreateUsuarioDTO dto) {
        if (existeCorreo(dto.correo())) {
            logger.error("[USER] : The email {} already exist", dto.correo());
            throw new IllegalArgumentException("Correo ya registrado");
        }

        Empresa empresaDestino = empresaRepository.findById(dto.empresaId())
                .orElseThrow(() -> {
                    logger.error("[SYS] : The company for the new admin does not exist");
                    return new EntityNotFoundException("Empresa no encontrada");
                });

        Usuario usuario = new Usuario();
        usuario.setCorreo(dto.correo().trim().toLowerCase());
        usuario.setNombre(dto.nombre().trim());
        usuario.setPasswordHash(passwordEncoder.encode(dto.password()));
        usuario.setEmpresa(empresaDestino);
        usuario.setActivo(true);
        usuario.addRol(Role.ADMIN);

        usuarioRepository.save(usuario);

        return UsuarioMapper.toResponse(usuario);
    }

    private boolean tieneRol(Usuario usuario, Role role) {
        return usuario.getRoles() != null && usuario.getRoles().contains(role);
    }

    // Actualizar usuario
    public UsuarioResponseDTO actualizarUsuario (Long id, UsuarioUpdateDTO dto) {
        Usuario actual = getUsuarioLogueado();
        Usuario usuario = usuarioGestionable(actual, id);

        if (dto.nombre() != null && !dto.nombre().isBlank()) {
            usuario.setNombre(dto.nombre().trim());
        }

        if (dto.sucursalId() != null) {
            usuario.setSucursal(buscarSucursalDeEmpresa(dto.sucursalId(), usuario.getEmpresa().getId()));
        }

        if (dto.correo() != null && !dto.correo().isBlank()) {
            String correo = dto.correo().trim().toLowerCase();
            // Verificamos que no exista otro usuario con el mismo correo
            usuarioRepository.findByCorreo(correo)
                    .filter(u -> !u.getId().equals(usuario.getId()))
                    .ifPresent(u -> { throw new IllegalArgumentException("Correo ya registrado"); });
            usuario.setCorreo(correo);
        }

        if (dto.password() != null && !dto.password().isBlank()) {
            usuario.setPasswordHash(passwordEncoder.encode(dto.password()));
        }
        return UsuarioMapper.toResponse(usuarioRepository.save(usuario));
    }

    // Obtener usuarios (activos e inactivos, para poder reactivarlos)
    public List<UsuarioResponseDTO> getAllUsuarios() {
        Usuario usuarioActual = getUsuarioLogueado();
        List<Usuario> usuarios;
        if (tieneRol(usuarioActual, Role.SUPER_ADMIN)) {
            usuarios = usuarioRepository.findAllByOrderByNombreAsc();
        } else {
            usuarios = usuarioRepository.findAllByEmpresaIdOrderByNombreAsc(usuarioActual.getEmpresa().getId());
        }
        logger.info("[USER] : Retrieved users. Total: {}", usuarios.size());
        return usuarios.stream()
                .map(UsuarioMapper::toResponse)
                .toList();
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
            throw new AuthenticationCredentialsNotFoundException("No hay usuario autenticado");
        }
        // auth.getPrincipal() normalmente es un UserDetails
        String correo = auth.getName();
        return usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new AuthenticationCredentialsNotFoundException("Usuario logueado no encontrado"));
    }
}
