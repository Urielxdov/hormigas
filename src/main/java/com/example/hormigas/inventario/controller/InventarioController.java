package com.example.hormigas.inventario.controller;

import com.example.hormigas.inventario.dto.CrearInventarioDTO;
import com.example.hormigas.inventario.dto.InventarioResponseDTO;
import com.example.hormigas.inventario.service.InventarioService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
