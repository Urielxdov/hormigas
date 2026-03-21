package com.example.hormigas.sucursal.controller;

import com.example.hormigas.sucursal.dto.SucursalCreateDTO;
import com.example.hormigas.sucursal.dto.SucursalResponseDTO;
import com.example.hormigas.sucursal.service.SucursalService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.AccessDeniedException;

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
}
