package com.example.hormigas.security.application.mapper;

import com.example.hormigas.security.infrastructure.dtos.CreateUsuarioDTO;
import com.example.hormigas.security.infrastructure.dtos.LoginRequestDTO;
import com.example.hormigas.security.infrastructure.dtos.UsuarioResponseDTO;
import com.example.hormigas.security.domain.Usuario;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

public class AuthMapper {
    private AuthMapper() {
        throw new UnsupportedOperationException("Esta clase nunca debe ser instanciada");
    }

    public static Usuario fromDto(final CreateUsuarioDTO dto) {
        Usuario usuario = new Usuario(); // constructor vacío
        usuario.setNombre(dto.nombre());
        usuario.setCorreo(dto.correo());
        usuario.setPasswordHash(dto.password());
        usuario.setActivo(true); // activo por defecto al crear
        return usuario;
    }

    public static Authentication fromDto(final LoginRequestDTO loginRequestDTO) {
        return new UsernamePasswordAuthenticationToken(loginRequestDTO.email(), loginRequestDTO.password());
    }

    public static UsuarioResponseDTO toDto(final Usuario usuario) {
        return new UsuarioResponseDTO(
                usuario.getId(),
                usuario.getUsername(),
                usuario.getCorreo(),
                usuario.getEmpresa().getId()
        );
    }
}