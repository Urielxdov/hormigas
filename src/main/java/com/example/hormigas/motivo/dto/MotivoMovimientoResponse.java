package com.example.hormigas.motivo.dto;

import com.example.hormigas.movimiento.entity.TipoMovimiento;

public record MotivoMovimientoResponse
        (Long id,
         String nombre,
         String descripcion,
         TipoMovimiento tipoMovimiento
        )
{
}
