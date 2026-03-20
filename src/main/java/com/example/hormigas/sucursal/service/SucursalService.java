package com.example.hormigas.sucursal.service;

import com.example.hormigas.security.domain.Role;
import com.example.hormigas.security.domain.Usuario;
import com.example.hormigas.security.domain.services.UsuarioService;
import com.example.hormigas.sucursal.dto.SucursalCreateDTO;
import com.example.hormigas.sucursal.dto.SucursalResponseDTO;
import com.example.hormigas.sucursal.entity.Sucursal;
import com.example.hormigas.sucursal.mapper.MapperSucursal;
import com.example.hormigas.sucursal.repository.SucursalRepository;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;
import java.util.List;

@Service
public class SucursalService {
    private final UsuarioService usuarioService;
    private final SucursalRepository sucursalRepository;

    public SucursalService(UsuarioService usuarioService, SucursalRepository sucursalRepository) {
        this.usuarioService = usuarioService;
        this.sucursalRepository = sucursalRepository;
    }


    // Agregar sucursal
    public SucursalResponseDTO crear (SucursalCreateDTO dto) throws AccessDeniedException {
        Usuario admin = usuarioService.getUsuarioLogueado();

        if (!admin.getRoles().contains(Role.ADMIN)) {
            throw new AccessDeniedException("Usuario no autorizado para creacion de sucursales");
        }

        Sucursal sucursal = new Sucursal();
        sucursal.setNombre(dto.nombre());
        sucursal.setDireccion(dto.direccion());
        sucursal.setEmpresa(admin.getEmpresa());

        return MapperSucursal.toResponse(sucursal);
    }


    // actualizar sucursal

    // Eliminar sucursal

    // Ver sucursales
    public List<SucursalResponseDTO> obtenerSucursales() {
        Usuario admin = usuarioService.getUsuarioLogueado();

        return sucursalRepository.findByEmpresaId(admin.getEmpresa().getId())
                .stream()
                .map(MapperSucursal::toResponse)
                .toList();
    }
}
