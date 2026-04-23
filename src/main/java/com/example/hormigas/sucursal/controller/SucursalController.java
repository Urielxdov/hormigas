package com.example.hormigas.sucursal.controller;

import com.example.hormigas.sucursal.dto.SucursalCreateDTO;
import com.example.hormigas.sucursal.dto.SucursalResponseDTO;
import com.example.hormigas.sucursal.service.SucursalService;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.List;

@RestController
@RequestMapping("/api/sucursal")
public class SucursalController {

    private final SucursalService sucursalService;

    public SucursalController(SucursalService sucursalService) {
        this.sucursalService = sucursalService;
    }

    @PostMapping("/crear")
    public SucursalResponseDTO crear (@RequestBody SucursalCreateDTO dto) throws AccessDeniedException {
        return sucursalService.crear(dto);
    }

    @GetMapping("/listar")
    public List<SucursalResponseDTO> listar() {
        return sucursalService.obtenerSucursales();
    }
}
