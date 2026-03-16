package com.example.hormigas.security.service;

import com.example.hormigas.security.dto.usuario.CreateUsuarioDTO;
import com.example.hormigas.security.dto.auth.LoginRequestDTO;
import com.example.hormigas.security.entity.Usuario;
import com.example.hormigas.security.mapper.AuthMapper;
import com.example.hormigas.security.repository.UsuarioRepository;
import com.example.hormigas.security.service.interfaces.AuthService;
import com.example.hormigas.security.service.interfaces.TokenService;
import jakarta.persistence.EntityNotFoundException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderNotFoundException;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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

    public AuthServiceImpl(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationConfiguration authenticationConfiguration,
            TokenService tokenService
    ){
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationConfiguration = authenticationConfiguration;
        this.tokenService = tokenService;
    }


    @Override
    public String login(LoginRequestDTO loginRequestDTO) {
        try {
            final AuthenticationManager authenticationManager = authenticationConfiguration.getAuthenticationManager();
            final Authentication authRequest = AuthMapper.fromDto(loginRequestDTO);
            final Authentication authentication = authenticationManager.authenticate(authRequest);
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
        final Usuario createUsuario = AuthMapper.fromDto(createUserDto);
        createUsuario.setPasswordHash(passwordEncoder.encode(createUserDto.password()));
        final Usuario usuario = usuarioRepository.save(createUsuario);
        logger.info("[USER] : User succesfully created with id {}", usuario.getId());
    }

    @Override
    public Usuario getUser(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("[USER] : User not found with id {}", id);
                    return new EntityNotFoundException("No se encontro el usuario");
                });
    }
}
