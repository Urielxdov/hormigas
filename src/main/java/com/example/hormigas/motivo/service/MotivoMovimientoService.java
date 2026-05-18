package com.example.hormigas.motivo.service;

import com.example.hormigas.empresa.entity.Empresa;
import com.example.hormigas.motivo.dto.ActualizarMotivoDTO;
import com.example.hormigas.motivo.dto.CrearMotivoDTO;
import com.example.hormigas.motivo.dto.MotivoMovimientoResponse;
import com.example.hormigas.motivo.entity.MotivoMovimiento;
import com.example.hormigas.motivo.mapper.MotivoMovimientoMapper;
import com.example.hormigas.motivo.repository.MotivoMovimientoRepository;
import com.example.hormigas.security.domain.Usuario;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MotivoMovimientoService {

    private final MotivoMovimientoRepository motivoMovimientoRepository;

    public MotivoMovimientoService(MotivoMovimientoRepository motivoMovimientoRepository) {
        this.motivoMovimientoRepository = motivoMovimientoRepository;
    }

    @Transactional
    public MotivoMovimientoResponse crear(CrearMotivoDTO dto, Usuario usuario) {
        MotivoMovimiento motivo = new MotivoMovimiento();
        motivo.setNombre(dto.nombre());
        motivo.setDescripcion(dto.descripcion());
        motivo.setTipoMovimiento(dto.tipoMovimiento());
        motivo.setEmpresa(usuario.getEmpresa());
        motivoMovimientoRepository.save(motivo);
        return MotivoMovimientoMapper.toResponse(motivo);
    }

    public List<MotivoMovimientoResponse> listar(Usuario usuario) {
        Empresa empresa = usuario.getEmpresa();
        return motivoMovimientoRepository.findByEmpresaAndActivoTrue(empresa)
                .stream()
                .map(MotivoMovimientoMapper::toResponse)
                .toList();
    }

    @Transactional
    public void desactivar(Long id, Usuario usuario) {
        MotivoMovimiento motivo = motivoMovimientoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No existe el motivo solicitado"));
        if (!motivo.getEmpresa().getId().equals(usuario.getEmpresa().getId())) {
            throw new AccessDeniedException("No tiene permiso para modificar este motivo");
        }
        motivo.setActivo(false);
        motivoMovimientoRepository.save(motivo);
    }

    public MotivoMovimientoResponse actualizar(Long id, ActualizarMotivoDTO dto, Usuario usuario) {
        MotivoMovimiento motivo = motivoMovimientoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No existe el movimiento a actualizar"));
        if (!motivo.getEmpresa().getId().equals(usuario.getEmpresa().getId())) {
            throw new AccessDeniedException("No tiene permiso para modificar este motivo");
        }
        motivo.setNombre(dto.nombre());
        motivo.setDescripcion(dto.descripcion());
        motivo.setTipoMovimiento(dto.tipoMovimiento());
        return MotivoMovimientoMapper.toResponse(motivoMovimientoRepository.save(motivo));
    }
}
