package com.example.hormigas.reporte.dto;

public record ProductoTopDTO(
        Long productoId,
        String nombre,
        String sku,
        long totalEntradas,
        long totalSalidas
) {
    public long netoCambio() {
        return totalEntradas - totalSalidas;
    }
}
