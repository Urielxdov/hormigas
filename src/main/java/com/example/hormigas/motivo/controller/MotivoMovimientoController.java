package com.example.hormigas.motivo.controller;

import com.example.hormigas.motivo.dto.ActualizarMotivoDTO;
import com.example.hormigas.motivo.dto.CrearMotivoDTO;
import com.example.hormigas.motivo.dto.MotivoMovimientoResponse;
import com.example.hormigas.motivo.service.MotivoMovimientoService;
import com.example.hormigas.security.domain.Usuario;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/motivos-movimiento")
public class MotivoMovimientoController {

    private final MotivoMovimientoService motivoMovimientoService;

    public MotivoMovimientoController(MotivoMovimientoService motivoMovimientoService) {
        this.motivoMovimientoService = motivoMovimientoService;
    }

    @PostMapping
    public MotivoMovimientoResponse crear(
            @RequestBody CrearMotivoDTO dto,
            @AuthenticationPrincipal Usuario usuario) {
        return motivoMovimientoService.crear(dto, usuario);
    }

    @GetMapping
    public List<MotivoMovimientoResponse> listar(@AuthenticationPrincipal Usuario usuario) {
        return motivoMovimientoService.listar(usuario);
    }

    @PutMapping("/{id}")
    public MotivoMovimientoResponse actualizar(
            @PathVariable Long id,
            @RequestBody ActualizarMotivoDTO dto) {
        return motivoMovimientoService.actualizar(id, dto);
    }

    @DeleteMapping("/{id}")
    public void desactivar(@PathVariable Long id) {
        motivoMovimientoService.desactivar(id);
    }
}
