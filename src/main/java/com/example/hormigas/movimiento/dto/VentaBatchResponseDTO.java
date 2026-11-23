package com.example.hormigas.movimiento.dto;

import java.util.List;

/**
 * Resultado de una venta batch. La venta es parcial: los items validos se registran
 * y los invalidos se reportan en {@code rechazados} sin revertir los demas.
 *
 * @param duplicada true si el {@code clientId} ya habia sido procesado; en ese caso
 *                  {@code procesados} son los movimientos registrados la primera vez
 */
public record VentaBatchResponseDTO(
        String clientId,
        boolean duplicada,
        List<MovimientoResponseDTO> procesados,
        List<ItemRechazadoDTO> rechazados
) {}
