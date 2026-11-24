package com.example.hormigas.security.infrastructure.dtos;

import java.time.LocalDateTime;
import java.util.List;

public record UsuarioResponseDTO (
        Long id,
        String name,
        String correo,
        Long empresaId,
        Long sucursalId,
        String sucursalNombre,
        boolean activo,
        List<String> roles,
        LocalDateTime ultimoAcceso
) {}
