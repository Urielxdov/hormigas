package com.example.hormigas.inventario.controller;

import com.example.hormigas.inventario.dto.CrearInventarioDTO;
import com.example.hormigas.inventario.dto.InventarioFiltroDTO;
import com.example.hormigas.inventario.dto.InventarioResponseDTO;
import com.example.hormigas.inventario.service.InventarioService;
import com.example.hormigas.security.domain.Usuario;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventario")
public class InventarioController {

    private InventarioService inventarioService;

    public InventarioController (InventarioService inventarioService) {
        this.inventarioService = inventarioService;
    }

    @PostMapping("/crear")
    public InventarioResponseDTO crear(@RequestBody CrearInventarioDTO dto) {
        return inventarioService.crear(dto);
    }

    @GetMapping("/porSucursal")
    public List<InventarioResponseDTO> porSucursal(@AuthenticationPrincipal Usuario usuario) {
        Long sucursalId = usuario.getSucursal() != null ? usuario.getSucursal().getId() : null;
        return inventarioService.obtenerInventario(new InventarioFiltroDTO(sucursalId, null));
    }

    @GetMapping("/stockBajo")
    public List<InventarioResponseDTO> stockBajo() {
        return inventarioService.obtenerStockBajo();
    }
}
