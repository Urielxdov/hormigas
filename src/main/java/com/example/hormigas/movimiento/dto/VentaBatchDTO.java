package com.example.hormigas.movimiento.dto;

import java.util.List;

public record VentaBatchDTO(
        Long sucursalId,
        List<VentaBatchItemDTO> items,
        String referencia
) {}
