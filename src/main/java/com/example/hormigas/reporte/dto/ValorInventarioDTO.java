package com.example.hormigas.reporte.dto;

import java.math.BigDecimal;
import java.util.List;

public record ValorInventarioDTO(
        Long sucursalId,
        String nombreSucursal,
        BigDecimal valorTotal,
        int productosConPrecio,
        int productosSinPrecio,
        List<DetalleValorDTO> detalle
) {}
