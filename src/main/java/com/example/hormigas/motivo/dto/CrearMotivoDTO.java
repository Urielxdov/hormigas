package com.example.hormigas.motivo.dto;

import com.example.hormigas.movimiento.entity.TipoMovimiento;

public record CrearMotivoDTO(
        String nombre,
        String descripcion,
        TipoMovimiento tipoMovimiento
) {}
