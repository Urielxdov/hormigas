package com.example.hormigas.security.mapper;

import com.example.hormigas.security.entity.Usuario;

public class UsuarioMapper {

    public static LoginResponseDTO toResponse(Usuario usuario) {
        return new LoginResponseDTO(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getCorreo(),
                usuario.getEmpresa().getId()
        );
    }
}
