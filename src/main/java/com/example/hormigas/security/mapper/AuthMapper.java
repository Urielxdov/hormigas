package com.example.hormigas.security.mapper;

import com.example.hormigas.security.dto.usuario.CreateUsuarioDTO;
import com.example.hormigas.security.dto.usuario.LoginRequestDTO;
import com.example.hormigas.security.dto.usuario.UsuarioResponseDTO;
import com.example.hormigas.security.entity.Usuario;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

public class AuthMapper {
    private AuthMapper () {
        throw new UnsupportedOperationException("Esta clase nunca debe ser instanciada");
    }

    public static Usuario fromDto(final CreateUsuarioDTO dto) {
        return Usuario.builder()
                .nombre(dto.nombre())
                .correo(dto.correo())
                .passwordHash(dto.password())
                .activo(true) // Por ejemplo, activo por defecto al crear
                .build();
    }

    public static Authentication fromDto(final LoginRequestDTO loginRequestDTO) {
        return new UsernamePasswordAuthenticationToken(loginRequestDTO.email(), loginRequestDTO.password());
    }

    public static UsuarioResponseDTO toDto(final Usuario usuario) {
        return new UsuarioResponseDTO(usuario.getId(), usuario.getUsername(), usuario.getCorreo(), usuario.getEmpresa().getId());
    }


}
