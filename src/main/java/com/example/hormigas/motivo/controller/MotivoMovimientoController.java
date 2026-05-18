package com.example.hormigas.motivo.controller;

import com.example.hormigas.motivo.dto.ActualizarMotivoDTO;
import com.example.hormigas.motivo.dto.CrearMotivoDTO;
import com.example.hormigas.motivo.dto.MotivoMovimientoResponse;
import com.example.hormigas.motivo.service.MotivoMovimientoService;
import com.example.hormigas.security.domain.Usuario;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/motivos-movimiento")
@Validated
public class MotivoMovimientoController {

    private final MotivoMovimientoService motivoMovimientoService;

    public MotivoMovimientoController(MotivoMovimientoService motivoMovimientoService) {
        this.motivoMovimientoService = motivoMovimientoService;
    }

    @PostMapping
    public MotivoMovimientoResponse crear(
            @Valid @RequestBody CrearMotivoDTO dto,
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
            @Valid @RequestBody ActualizarMotivoDTO dto,
            @AuthenticationPrincipal Usuario usuario) {
        return motivoMovimientoService.actualizar(id, dto, usuario);
    }

    @DeleteMapping("/{id}")
    public void desactivar(
            @PathVariable Long id,
            @AuthenticationPrincipal Usuario usuario) {
        motivoMovimientoService.desactivar(id, usuario);
    }
}
