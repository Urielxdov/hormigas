package com.example.hormigas.empresa.service;

import com.example.hormigas.empresa.dto.*;
import com.example.hormigas.empresa.entity.Empresa;
import com.example.hormigas.empresa.exception.EmpresaDuplicadaException;
import com.example.hormigas.empresa.mapper.EmpresaMapper;
import com.example.hormigas.empresa.repository.EmpresaRepository;
import com.example.hormigas.security.application.mapper.UsuarioMapper;
import com.example.hormigas.security.domain.Usuario;
import com.example.hormigas.security.domain.services.UsuarioService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

@Service
public class EmpresaService {
    private final static Logger logger = LogManager.getLogger(EmpresaService.class);
    private final EmpresaRepository empresaRepository;
    private final UsuarioService usuarioService;

    public EmpresaService(EmpresaRepository empresaRepository, UsuarioService usuarioService) {
        this.empresaRepository = empresaRepository;
        this.usuarioService = usuarioService;
    }
    // Crear empresa
    @Transactional
    public EmpresaResponseDTO createEmpresa(EmpresaConAdminCreateDTO dto) {
        EmpresaCreateDTO empresaDto = dto.empresa();
        UsuarioEmpresaCreateDTO adminDto = dto.admin();

        if (empresaRepository.findByRfc(empresaDto.rfc()).isPresent()) {
            logger.error("[COMPANY]: The company with rfc {} already exist", empresaDto.rfc());
            throw new EmpresaDuplicadaException("Ya existe una empresa con este RFC");
        }

        if (empresaRepository.findByNombre(empresaDto.nombre()).isPresent()) {
            logger.error("[COMPANY] : The company with name {} already exist", empresaDto.nombre());
            throw new EmpresaDuplicadaException("Ya existe una empresa con este nombre");
        }

        Empresa nuevaEmpresa = new Empresa();

        nuevaEmpresa.setNombre(empresaDto.nombre());
        nuevaEmpresa.setRfc(empresaDto.rfc());
        nuevaEmpresa.setDireccion(empresaDto.direccion());
        nuevaEmpresa.setTelefono(empresaDto.telefono());
        nuevaEmpresa.setActivo(true);

        Empresa empresa = empresaRepository.save(nuevaEmpresa);

        usuarioService.nuevoUsuario(UsuarioMapper.toCreate(adminDto, empresa.getId()));
        return EmpresaMapper.toResponseEmpresa(empresa);
    }

    // Actualizar empresa
    // Debemos de proteger el endpoint para esto
    public EmpresaResponseDTO updateEmpresa(EmpresaUpdateDTO dto) {
        Usuario admin = usuarioService.getUsuarioLogueado();
        Empresa empresa = admin.getEmpresa();

        if (dto.nombre() != null &&
                !dto.nombre().equals(empresa.getNombre())) {

            if (empresaRepository.existsByNombre(dto.nombre())) {
                logger.error("[COMPANY] : The desired name already registered");
                throw new EmpresaDuplicadaException("El nombre de la empresa ya esta registrado");
            }

            empresa.setNombre(dto.nombre());
        }

        if (dto.rfc() != null &&
                !dto.rfc().equals(empresa.getRfc())) {

            if (empresaRepository.existsByRfc(dto.rfc())) {
                logger.error("[COMPANY] : The desired RFC already registered");
                throw new EmpresaDuplicadaException("RFC ya esta registrado");
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
        logger.info("[COMPANY] : Company update {}", empresa.getId());

        return EmpresaMapper.toResponseEmpresa(empresa);
    }

    // Eliminar empresa
    // Exclusivo de superadmin
    public void deleteEmpresa (Long id) {
        Empresa empresa = empresaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Empresa no encontrada"));

        empresa.setActivo(false);

        empresaRepository.save(empresa);
    }

    public void deleteEmpresa () {
        Usuario admin = usuarioService.getUsuarioLogueado();
        Empresa empresa = admin.getEmpresa();

        empresa.setActivo(false);
        empresaRepository.save(empresa);
    }

    public EmpresaResponseDTO activate (Long id) {
        Empresa empresa = empresaRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("[SYS] : Super admin try activate to \n" +
                            "non-existent company. Id : {}", id);
                    return new EntityNotFoundException("No se encontro la empresa");
                });

        if (empresa.isActivo()) {
            logger.error("[SYS] : Super admin try activate to active company. Id : {}", id);
            throw new IllegalArgumentException("La empresa ya se encuentra activa");
        }
        empresa.setActivo(true);
        return EmpresaMapper.toResponseEmpresa(empresa);
    }

    public EmpresaResponseDTO activate (String rfc) {
        Empresa empresa = empresaRepository.findByRfc(rfc)
                .orElseThrow(() -> {
                    logger.error("[SYS] : Super admin try activate to \n" +
                            "non-existent company. RFC : {}", rfc);
                    return new EntityNotFoundException("No se encontro la empresa");
                });

        if (empresa.isActivo()) {
            logger.error("[SYS] : Super admin try activate to active company. RFC : {}", rfc);
            throw new IllegalArgumentException("La empresa ya se encuentra activa");
        }
        empresa.setActivo(true);
        return EmpresaMapper.toResponseEmpresa(empresa);
    }

    // Ver detalle de empresa
    public EmpresaResponseDTO getEmpresaDetails() {
        Usuario user = usuarioService.getUsuarioLogueado();

        return EmpresaMapper.toResponseEmpresa(user.getEmpresa());
    }
}
