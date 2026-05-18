package com.example.hormigas.traslado.dto;

import com.example.hormigas.movimiento.dto.MovimientoResponseDTO;

public record TrasladoResponseDTO(
        MovimientoResponseDTO movimientoSalida,
        MovimientoResponseDTO movimientoEntrada,
        String referencia
) {}
