package com.example.hormigas.motivo.mapper;

import com.example.hormigas.motivo.dto.MotivoMovimientoResponse;
import com.example.hormigas.motivo.entity.MotivoMovimiento;

public class MotivoMovimientoMapper {

    public static MotivoMovimientoResponse toResponse(MotivoMovimiento motivo) {
        return new MotivoMovimientoResponse(
                motivo.getEmpresa().getId(),
                motivo.getNombre(),
                motivo.getDescripcion(),
                motivo.getTipoMovimiento()
        );
    }
}
