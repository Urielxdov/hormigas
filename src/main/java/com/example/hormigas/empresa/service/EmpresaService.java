package com.example.hormigas.empresa.service;

import com.example.hormigas.empresa.dto.EmpresaCreateDTO;
import com.example.hormigas.empresa.dto.EmpresaResponseDTO;
import com.example.hormigas.empresa.dto.EmpresaUpdateDTO;
import com.example.hormigas.empresa.entity.Empresa;
import com.example.hormigas.empresa.exception.EmpresaDuplicadaException;
import com.example.hormigas.empresa.mapper.EmpresaMapper;
import com.example.hormigas.empresa.repository.EmpresaRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

@Service
public class EmpresaService {
    private final static Logger logger = LogManager.getLogger(EmpresaService.class);
    private final EmpresaRepository empresaRepository;

    public EmpresaService(EmpresaRepository empresaRepository) {
        this.empresaRepository = empresaRepository;
    }
    // Crear empresa
    public EmpresaResponseDTO createEmpresa(EmpresaCreateDTO dto) {
        // Validamos el rfc
        if (empresaRepository.findByRfc(dto.rfc()).isPresent()) {
            logger.error("[COMPANY] : The company with rfc {} already exist", dto.rfc());
            throw new EmpresaDuplicadaException("Ya existe una empresa con este RFC");
        }

        if (empresaRepository.findByNombre(dto.nombre()).isPresent()) {
            logger.error("[COMPANY] : The company with name {} already exist", dto.nombre());
            throw new EmpresaDuplicadaException("Nombre duplicado");
        }

        Empresa empresa = new Empresa(
                dto.nombre(),
                dto.rfc(),
                dto.direccion(),
                dto.telefono()
        );

        empresa = empresaRepository.save(empresa);
        
        return EmpresaMapper.toResponseEmpresa(empresa);
    }

    // Actualizar empresa
    public EmpresaResponseDTO updateEmpresa(Long id, EmpresaUpdateDTO dto) {

        Empresa empresa = empresaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Empresa no encontrada"));

        if (dto.nombre() != null &&
                !dto.nombre().equals(empresa.getNombre())) {

            if (empresaRepository.existsByNombre(dto.nombre())) {
                throw new EmpresaDuplicadaException("El nombre de la empresa ya existe");
            }

            empresa.setNombre(dto.nombre());
        }

        if (dto.rfc() != null &&
                !dto.rfc().equals(empresa.getRfc())) {

            if (empresaRepository.existsByRfc(dto.rfc())) {
                throw new EmpresaDuplicadaException("RFC ya existe");
            }

            empresa.setRfc(dto.rfc());
        }

        if (dto.direccion() != null) {
            empresa.setDireccion(dto.direccion());
        }

        if (dto.telefono() != null) {
            empresa.setTelefono(dto.telefono());
        }

        empresa.setActivo(true);

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
