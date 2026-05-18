package com.example.hormigas.motivo.dto;

import com.example.hormigas.movimiento.entity.TipoMovimiento;
import jakarta.validation.constraints.NotBlank;

public record CrearMotivoDTO(
        @NotBlank String nombre,
        String descripcion,
        TipoMovimiento tipoMovimiento
) {}
