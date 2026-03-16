package com.example.hormigas.security.mapper;

import com.example.hormigas.security.dto.usuario.UsuarioResponseDTO;
import com.example.hormigas.security.entity.Usuario;

public class UsuarioMapper {

    public static UsuarioResponseDTO toResponse(Usuario usuario) {
        return new UsuarioResponseDTO(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getCorreo(),
                usuario.getEmpresa().getId()
        );
    }
}
