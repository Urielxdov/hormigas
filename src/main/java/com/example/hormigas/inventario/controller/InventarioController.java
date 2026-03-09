package com.example.hormigas.inventario.controller;

import com.example.hormigas.inventario.dto.InventarioResponseDTO;
import com.example.hormigas.inventario.entity.Inventario;
import com.example.hormigas.inventario.service.InventarioService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/inventario")
public class InventarioController {

    private final InventarioService inventarioService;

    public InventarioController (InventarioService inventarioService) {
        this.inventarioService = inventarioService;
    }

    @GetMapping("/sucursal/{sucursalId}")
    public List<InventarioResponseDTO> obtenerInventarioSucursal(@PathVariable Long sucursalId) {
        return inventarioService.obtenerInventarioPorSucursal(sucursalId);
    }
}
