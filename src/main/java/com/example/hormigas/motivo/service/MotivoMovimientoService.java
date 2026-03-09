package com.example.hormigas.motivo.service;

import com.example.hormigas.empresa.entity.Empresa;
import com.example.hormigas.empresa.repository.EmpresaRepository;
import com.example.hormigas.motivo.dto.ActualizarMotivoDTO;
import com.example.hormigas.motivo.dto.CrearMotivoDTO;
import com.example.hormigas.motivo.dto.MotivoMovimientoResponse;
import com.example.hormigas.motivo.entity.MotivoMovimiento;
import com.example.hormigas.motivo.mapper.MotivoMovimientoMapper;
import com.example.hormigas.motivo.repository.MotivoMovimientoRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MotivoMovimientoService {

    private final MotivoMovimientoRepository motivoMovimientoRepository;
    private final EmpresaRepository empresaRepository;

    public MotivoMovimientoService (MotivoMovimientoRepository motivoMovimientoRepository, EmpresaRepository empresaRepository) {
        this.motivoMovimientoRepository = motivoMovimientoRepository;
        this.empresaRepository = empresaRepository;
    }

    public MotivoMovimientoResponse crear (CrearMotivoDTO dto) {
        if (!this.empresaRepository.existsById(dto.empresaId())) {
            throw new EntityNotFoundException("Los datos sobre la empresa no coinciden");
        }
        MotivoMovimiento motivo = new MotivoMovimiento();
        motivo.setNombre(dto.nombre());
        motivo.setDescripcion(dto.descripcion());
        motivo.setTipoMovimiento(dto.tipoMovimiento());

        motivoMovimientoRepository.save(motivo);

        return MotivoMovimientoMapper.toResponse(motivo);
    }

    public List<MotivoMovimientoResponse> listar(Long idEmpresa) {
        Empresa empresa = empresaRepository.findById(idEmpresa)
                .orElseThrow(() -> new EntityNotFoundException("La empresa a la que se intenta asignar no existe"));
        return motivoMovimientoRepository.findByEmpresa(empresa)
                .stream()
                .map(MotivoMovimientoMapper::toResponse)
                .toList();
    }

    public void desactivar (Long id) {
        MotivoMovimiento motivo = motivoMovimientoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No existe el motivo solciitado"));

        motivo.setActivo(false);
        motivoMovimientoRepository.save(motivo);
    }

    public MotivoMovimientoResponse actualizar(Long id, ActualizarMotivoDTO dto) {
        MotivoMovimiento motivo = motivoMovimientoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No existe el movimiento a actualizar"));
        motivo.setNombre(dto.nombre());
        motivo.setDescripcion(dto.descripcion());
        motivo.setTipoMovimiento(dto.tipoMovimiento());
        MotivoMovimiento movimientoActualizado = motivoMovimientoRepository.save(motivo);
        return MotivoMovimientoMapper.toResponse(movimientoActualizado);
    }
}
