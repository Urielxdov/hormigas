package com.example.hormigas.security.application.mapper;

import com.example.hormigas.empresa.dto.UsuarioEmpresaCreateDTO;
import com.example.hormigas.security.infrastructure.dtos.CreateUsuarioDTO;
import com.example.hormigas.security.infrastructure.dtos.UsuarioResponseDTO;
import com.example.hormigas.security.domain.Usuario;

public class UsuarioMapper {

    public static UsuarioResponseDTO toResponse(Usuario usuario) {
        return new UsuarioResponseDTO(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getCorreo(),
                usuario.getEmpresa().getId()
        );
    }

    public static CreateUsuarioDTO toCreate(UsuarioEmpresaCreateDTO usuario, Long id) {
        return new CreateUsuarioDTO(
                usuario.correo(),
                usuario.password(),
                usuario.nombre(),
                id
        );
    }
}
