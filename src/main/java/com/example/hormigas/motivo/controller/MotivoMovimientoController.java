package com.example.hormigas.motivo.controller;

import com.example.hormigas.motivo.dto.ActualizarMotivoDTO;
import com.example.hormigas.motivo.dto.CrearMotivoDTO;
import com.example.hormigas.motivo.dto.MotivoMovimientoResponse;
import com.example.hormigas.motivo.service.MotivoMovimientoService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/motivos-movimiento")
public class MotivoMovimientoController {

    private final MotivoMovimientoService motivoMovimientoService;

    public MotivoMovimientoController(MotivoMovimientoService movimientoService) {
        this.motivoMovimientoService = movimientoService;
    }

    @PostMapping
    public MotivoMovimientoResponse crear (@RequestBody CrearMotivoDTO dto) {
        return motivoMovimientoService.crear(dto);
    }

    @GetMapping("/{empresaId}")
    public List<MotivoMovimientoResponse> listar(@PathVariable Long idEmpresa) {
        return motivoMovimientoService.listar(idEmpresa);
    }

    @PutMapping("/{id}")
    public MotivoMovimientoResponse actualizar(
            @PathVariable Long id,
            @RequestBody ActualizarMotivoDTO dto
            )
    {
        return motivoMovimientoService.actualizar(id, dto);
    }

    @DeleteMapping("/{id}")
    public void desactivar(@PathVariable Long id) {
        motivoMovimientoService.desactivar(id);
    }
}
