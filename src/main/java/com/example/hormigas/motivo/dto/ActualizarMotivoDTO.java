package com.example.hormigas.motivo.dto;

import com.example.hormigas.movimiento.entity.TipoMovimiento;

public record ActualizarMotivoDTO (
        String nombre,
        String descripcion,
        TipoMovimiento tipoMovimiento
) {
}
