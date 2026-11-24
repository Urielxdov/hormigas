package com.example.hormigas.movimiento.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Venta enviada por el POS (posiblemente generada offline).
 *
 * @param clientId   identificador generado en el cliente (obligatorio); hace idempotente el reintento de la misma venta
 * @param fecha      momento real de la venta en el cliente; si falta, es futura o tiene mas de 30 dias se usa la hora del servidor
 */
public record VentaBatchDTO(
        @NotNull Long sucursalId,
        @NotEmpty @Valid List<@NotNull VentaBatchItemDTO> items,
        @Size(max = 255) String referencia,
        @NotBlank @Size(max = 64) String clientId,
        OffsetDateTime fecha
) {}
