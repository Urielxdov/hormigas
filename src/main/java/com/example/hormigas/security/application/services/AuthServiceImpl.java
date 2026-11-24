package com.example.hormigas.security.application.services;

import com.example.hormigas.empresa.entity.Empresa;
import com.example.hormigas.empresa.repository.EmpresaRepository;
import com.example.hormigas.security.domain.Role;
import com.example.hormigas.security.infrastructure.dtos.CreateUsuarioDTO;
import com.example.hormigas.sucursal.entity.Sucursal;
import jakarta.persistence.EntityNotFoundException;
import com.example.hormigas.security.infrastructure.dtos.LoginRequestDTO;
import com.example.hormigas.security.domain.Usuario;
import com.example.hormigas.security.application.mapper.AuthMapper;
import com.example.hormigas.security.domain.repository.UsuarioRepository;
import com.example.hormigas.security.domain.services.UsuarioService;
import com.example.hormigas.security.domain.services.AuthService;
import com.example.hormigas.security.domain.services.TokenService;
import com.example.hormigas.sucursal.repository.SucursalRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderNotFoundException;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Primary
public class AuthServiceImpl implements AuthService {
    private static final Logger logger = LogManager.getLogger(AuthServiceImpl.class);

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationConfiguration authenticationConfiguration;
    private final TokenService tokenService;
    private final UsuarioService usuarioService;
    private final SucursalRepository sucursalRepository;
    private final EmpresaRepository empresaRepository;

    public AuthServiceImpl(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationConfiguration authenticationConfiguration,
            TokenService tokenService,
            UsuarioService usuarioService,
            SucursalRepository sucursalRepository,
            EmpresaRepository empresaRepository
    ){
        this.empresaRepository = empresaRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationConfiguration = authenticationConfiguration;
        this.tokenService = tokenService;
        this.usuarioService = usuarioService;
        this.sucursalRepository = sucursalRepository;
    }


    @Override
    public String login(LoginRequestDTO loginRequestDTO) {
        try {
            final AuthenticationManager authenticationManager = authenticationConfiguration.getAuthenticationManager();
            final Authentication authRequest = AuthMapper.fromDto(loginRequestDTO);
            final Authentication authentication = authenticationManager.authenticate(authRequest);
            usuarioService.updateLastLogin(authentication.getName());
            logger.info("[USER] : Usuario loggeado {}", authentication.getName());
            return tokenService.generateToken(authentication);
        } catch (Exception e) {
            logger.error("[USER] : Error mientras se intentaba logear", e);
            throw new ProviderNotFoundException("Error mientras se trataba de logear");
        }
    }

    @Override
    public boolean validateToken(String token) {
        return tokenService.validateToken(token);
    }

    @Override
    public String getUserFromToken(String token) {
        return tokenService.getUserFromToken(token);
    }

    @Override
    public void createUser(CreateUsuarioDTO createUserDto) {
        if (usuarioRepository.findByCorreo(createUserDto.correo()).isPresent()) {
            throw new IllegalArgumentException("Correo ya registrado");
        }
        final Usuario actual = usuarioService.getUsuarioLogueado();
        final boolean esSuperAdmin = actual.getRoles() != null && actual.getRoles().contains(Role.SUPER_ADMIN);

        final Usuario createUsuario = AuthMapper.fromDto(createUserDto);
        createUsuario.setPasswordHash(passwordEncoder.encode(createUserDto.password()));
        createUsuario.addRol(Role.USER);

        Sucursal sucursal = null;
        Empresa empresa;
        if (esSuperAdmin) {
            // SUPER_ADMIN puede crear usuarios en cualquier empresa/sucursal
            if (createUserDto.sucursalId() != null) {
                sucursal = sucursalRepository.findById(createUserDto.sucursalId())
                        .orElseThrow(() -> new EntityNotFoundException("Sucursal no encontrada"));
            }
            if (createUserDto.empresaId() != null) {
                empresa = empresaRepository.findById(createUserDto.empresaId())
                        .orElseThrow(() -> new EntityNotFoundException("Empresa no encontrada"));
            } else if (sucursal != null) {
                empresa = sucursal.getEmpresa();
            } else {
                throw new IllegalArgumentException("La empresa es obligatoria para el superusuario");
            }
            if (sucursal != null && !sucursal.getEmpresa().getId().equals(empresa.getId())) {
                throw new IllegalArgumentException("La sucursal no pertenece a la empresa indicada");
            }
        } else {
            // ADMIN: solo dentro de su propia empresa
            empresa = actual.getEmpresa();
            if (createUserDto.sucursalId() != null) {
                sucursal = sucursalRepository.findByIdAndEmpresaId(createUserDto.sucursalId(), empresa.getId())
                        .orElseThrow(() -> new EntityNotFoundException("Sucursal no encontrada en esta empresa"));
            }
        }
        createUsuario.setEmpresa(empresa);
        createUsuario.setSucursal(sucursal);

        final Usuario usuario = usuarioRepository.save(createUsuario);
        logger.info("[USER] : User succesfully created with id {}", usuario.getId());
    }

}
