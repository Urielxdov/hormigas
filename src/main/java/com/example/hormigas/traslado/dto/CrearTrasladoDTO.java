package com.example.hormigas.traslado.dto;

import jakarta.validation.constraints.Positive;

public record CrearTrasladoDTO(
        Long sucursalOrigenId,
        Long sucursalDestinoId,
        Long productoId,
        @Positive int cantidad,
        String referencia
) {}
