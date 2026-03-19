package com.example.hormigas.security.application.services;

import com.example.hormigas.security.infrastructure.dtos.CreateUsuarioDTO;
import com.example.hormigas.security.infrastructure.dtos.LoginRequestDTO;
import com.example.hormigas.security.domain.Usuario;
import com.example.hormigas.security.application.mapper.AuthMapper;
import com.example.hormigas.security.domain.repository.UsuarioRepository;
import com.example.hormigas.security.domain.services.UsuarioService;
import com.example.hormigas.security.domain.services.AuthService;
import com.example.hormigas.security.domain.services.TokenService;
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

    public AuthServiceImpl(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationConfiguration authenticationConfiguration,
            TokenService tokenService,
            UsuarioService usuarioService
    ){
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationConfiguration = authenticationConfiguration;
        this.tokenService = tokenService;
        this.usuarioService = usuarioService;
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
        final Usuario createUsuario = AuthMapper.fromDto(createUserDto);
        createUsuario.setPasswordHash(passwordEncoder.encode(createUserDto.password()));
        final Usuario usuario = usuarioRepository.save(createUsuario);
        logger.info("[USER] : User succesfully created with id {}", usuario.getId());
    }

}
