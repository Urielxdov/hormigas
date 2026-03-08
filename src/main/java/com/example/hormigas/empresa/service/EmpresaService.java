package com.example.hormigas.empresa.service;

import com.example.hormigas.empresa.dto.EmpresaResponseDTO;
import com.example.hormigas.empresa.dto.EmpresaUpdateDTO;
import com.example.hormigas.empresa.entity.Empresa;
import com.example.hormigas.empresa.exception.EmpresaDuplicadaException;
import com.example.hormigas.empresa.mapper.EmpresaMapper;
import com.example.hormigas.empresa.repository.EmpresaRepository;
import org.springframework.stereotype.Service;

@Service
public class EmpresaService {
    private final EmpresaRepository empresaRepository;

    public EmpresaService(EmpresaRepository empresaRepository) {
        this.empresaRepository = empresaRepository;
    }
    // Crear empresa
    public EmpresaResponseDTO createEmpresa(EmpresaResponseDTO dto) {
        // Validamos el rfc
        if (empresaRepository.findByRfc(dto.getRfc()).isPresent()) {
            throw new EmpresaDuplicadaException("Ya existe una empresa con este RFC");
        }

        if (empresaRepository.findByNombre(dto.getNombre()).isPresent()) {
            throw new EmpresaDuplicadaException("Nombre duplicado");
        }

        Empresa empresa = new Empresa(
                dto.getNombre(),
                dto.getRfc(),
                dto.getDireccion(),
                dto.getTelefono()
        );

        empresa = empresaRepository.save(empresa);
        
        return EmpresaMapper.toResponseEmpresa(empresa);
    }

    // Actualizar empresa
    public EmpresaResponseDTO updateEmpresa(Long id, EmpresaUpdateDTO dto) {

        Empresa empresa = empresaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Empresa no encontrada"));

        if (dto.getNombre() != null &&
                !dto.getNombre().equals(empresa.getNombre())) {

            if (empresaRepository.existsByNombre(dto.getNombre())) {
                throw new EmpresaDuplicadaException("El nombre de la empresa ya existe");
            }

            empresa.setNombre(dto.getNombre());
        }

        if (dto.getRfc() != null &&
                !dto.getRfc().equals(empresa.getRfc())) {

            if (empresaRepository.existsByRfc(dto.getRfc())) {
                throw new EmpresaDuplicadaException("RFC ya existe");
            }

            empresa.setRfc(dto.getRfc());
        }

        if (dto.getDireccion() != null) {
            empresa.setDireccion(dto.getDireccion());
        }

        if (dto.getTelefono() != null) {
            empresa.setTelefono(dto.getTelefono());
        }

        empresa.setActivo(dto.isActivo());

        empresa = empresaRepository.save(empresa);

        return EmpresaMapper.toResponseEmpresa(empresa);
    }

    // Eliminar empresa
    public void deleteEmpresa (Long id) {
        Empresa empresa = empresaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Empresa no encontrada"));

        empresa.setActivo(false);

        empresaRepository.save(empresa);
    }

    // Ver detalle de empresa
    public EmpresaResponseDTO getEmpresaDetails(Long id) {
        Empresa empresa = empresaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Empresa no encontrada"));

        return EmpresaMapper.toResponseEmpresa(empresa);
    }
}
