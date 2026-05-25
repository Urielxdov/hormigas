package com.example.hormigas.movimiento.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record VentaBatchDTO(
        @NotNull Long sucursalId,
        @NotEmpty @Valid List<VentaBatchItemDTO> items,
        String referencia
) {}
