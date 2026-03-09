package com.example.hormigas.movimiento.dto;

import java.time.LocalDateTime;

public record MovimientoListDTO(
        Long id,
        String producto,
        String tipoMovimiento,
        int cantidad,
        LocalDateTime fecha
) {}