package com.example.hormigas.security.controller;

import com.example.hormigas.security.dto.usuario.LoginResponseDTO;
import com.example.hormigas.security.dto.usuario.NuevoUsuarioDto;
import com.example.hormigas.security.service.UsuarioService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UsuarioController {
    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping("/usuario/crear")
    public LoginResponseDTO crearUsuario(NuevoUsuarioDto dto,) {

    }

    @PostMapping("/login")
}
