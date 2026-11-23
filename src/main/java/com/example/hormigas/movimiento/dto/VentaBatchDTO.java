package com.example.hormigas.movimiento.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Venta enviada por el POS (posiblemente generada offline).
 *
 * @param clientId   identificador generado en el cliente; hace idempotente el reintento de la misma venta
 * @param fecha      momento real de la venta en el cliente; si falta o es futura se usa la hora del servidor
 */
public record VentaBatchDTO(
        @NotNull Long sucursalId,
        @NotEmpty @Valid List<VentaBatchItemDTO> items,
        String referencia,
        @Size(max = 64) String clientId,
        OffsetDateTime fecha
) {}
