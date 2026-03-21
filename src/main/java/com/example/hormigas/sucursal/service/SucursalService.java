package com.example.hormigas.sucursal.service;

import com.example.hormigas.inventario.service.InventarioService;
import com.example.hormigas.security.domain.Role;
import com.example.hormigas.security.domain.Usuario;
import com.example.hormigas.security.domain.services.UsuarioService;
import com.example.hormigas.sucursal.dto.SucursalCreateDTO;
import com.example.hormigas.sucursal.dto.SucursalResponseDTO;
import com.example.hormigas.sucursal.entity.Sucursal;
import com.example.hormigas.sucursal.mapper.MapperSucursal;
import com.example.hormigas.sucursal.repository.SucursalRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.Objects;

@Service
public class SucursalService {
    private final UsuarioService usuarioService;
    private final SucursalRepository sucursalRepository;
    private final InventarioService inventarioService;

    public SucursalService(UsuarioService usuarioService, SucursalRepository sucursalRepository, InventarioService inventarioService) {
        this.usuarioService = usuarioService;
        this.sucursalRepository = sucursalRepository;
        this.inventarioService = inventarioService;
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

        sucursal = sucursalRepository.save(sucursal);

        return MapperSucursal.toResponse(sucursal);
    }


    // actualizar sucursal

    // Eliminar sucursal
    public SucursalResponseDTO desactivar(Long id) {
        Usuario admin = usuarioService.getUsuarioLogueado();

        Sucursal sucursal = sucursalRepository.findById(id)
                .orElseThrow(() -> {
                    return new EntityNotFoundException("No se encontro la sucursal");
                });

        if (!Objects.equals(admin.getEmpresa().getId(), sucursal.getEmpresa().getId())) {
            throw new IllegalArgumentException("El usuario que intenta eliminar la sucursal no pertenece a la empresa");
        }

        sucursal.setActiva(false);

        return MapperSucursal.toResponse(sucursalRepository.save(sucursal));
    }

    // Ver sucursales
    public List<SucursalResponseDTO> obtenerSucursales() {
        Usuario admin = usuarioService.getUsuarioLogueado();

        return sucursalRepository.findByEmpresaId(admin.getEmpresa().getId())
                .stream()
                .map(MapperSucursal::toResponse)
                .toList();
    }


    // Agregar inventario a sucursal
    public void agregarInventario(Long sucursalId, Long inventarioId) {
        inventarioService.agregarASucursal(sucursalId, inventarioId);
    }
}
