package com.example.hormigas.security.domain.services;

import com.example.hormigas.security.infrastructure.dtos.CreateUsuarioDTO;
import com.example.hormigas.security.infrastructure.dtos.LoginRequestDTO;

public interface AuthService {
    String login(LoginRequestDTO loginRequestDTO);
    boolean validateToken(String token);
    String getUserFromToken(String token);
    void createUser(CreateUsuarioDTO createUserDto);
}
