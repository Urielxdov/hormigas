package com.example.hormigas.movimiento.dto;

import java.util.List;

public record VentaBatchDTO(
        List<VentaBatchItemDTO> items,
        String referencia
) {}
