package com.example.hormigas.security.controller;

import com.example.hormigas.security.dto.usuario.LoginRequestDTO;
import com.example.hormigas.security.entity.Usuario;
import com.example.hormigas.security.repository.UsuarioRepository;
import com.example.hormigas.security.service.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {
    private final AuthenticationManager authManager;
    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;

    public AuthController(AuthenticationManager authManager, JwtService jwtService, UsuarioRepository usuarioRepository) {
        this.authManager = authManager;
        this.jwtService = jwtService;
        this.usuarioRepository = usuarioRepository;
    }

    @PostMapping("/login")
    public String login(@RequestBody LoginRequestDTO request) {
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.correo(), request.password())
        );

        Usuario usuario = usuarioRepository.findByCorreo(request.correo())
                .orElseThrow();


        String token = jwtService.generateToken(usuario);
        // Genera JWT
        return token;
    }
}
