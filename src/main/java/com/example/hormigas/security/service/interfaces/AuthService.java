package com.example.hormigas.security.service.interfaces;

import com.example.hormigas.security.dto.usuario.CreateUsuarioDTO;
import com.example.hormigas.security.dto.usuario.LoginRequestDTO;
import com.example.hormigas.security.entity.Usuario;

public interface AuthService {
    String login(LoginRequestDTO loginRequestDTO);
    boolean validateToken(String token);
    String getUserFromToken(String token);
    void createUser(CreateUsuarioDTO createUserDto);
    Usuario getUser(Long id);
}
