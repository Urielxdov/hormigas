package com.example.hormigas.security.infrastructure.controller;

import com.example.hormigas.security.infrastructure.dtos.CreateUsuarioDTO;
import com.example.hormigas.security.infrastructure.dtos.UsuarioResponseDTO;
import com.example.hormigas.security.domain.services.UsuarioService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/usuario")
public class UsuarioController {
    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping("/create")
    public UsuarioResponseDTO crearUsuario (@RequestBody CreateUsuarioDTO createUsuarioDTO) {
        return usuarioService.nuevoUsuario(createUsuarioDTO);
    }

}
