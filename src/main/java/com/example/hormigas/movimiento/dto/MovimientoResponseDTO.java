package com.example.hormigas.movimiento.dto;

import com.example.hormigas.movimiento.entity.TipoMovimiento;

import java.time.LocalDateTime;

public record MovimientoResponseDTO(
        Long id,
        Long productoId,
        String productoNombre,
        Long sucursalId,
        String sucursalNombre,
        TipoMovimiento tipoMovimiento,
        int cantidad,
        String usuarioNombre,
        String referencia,
        LocalDateTime fecha
) {}
