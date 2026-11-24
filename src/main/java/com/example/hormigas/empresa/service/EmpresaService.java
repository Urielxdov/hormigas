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

import java.util.List;

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
        String rfc = empresaDto.rfc().trim().toUpperCase();
        String nombre = empresaDto.nombre().trim();

        // Antes de crear nada: si el correo del admin ya existe no se deja una empresa sin admin
        if (usuarioService.existeCorreo(adminDto.correo())) {
            throw new IllegalArgumentException("El correo del administrador ya está registrado");
        }

        if (empresaRepository.findByRfc(rfc).isPresent()) {
            logger.error("[COMPANY]: The company with rfc {} already exist", empresaDto.rfc());
            throw new EmpresaDuplicadaException("Ya existe una empresa con este RFC");
        }

        if (empresaRepository.findByNombre(nombre).isPresent()) {
            logger.error("[COMPANY] : The company with name {} already exist", empresaDto.nombre());
            throw new EmpresaDuplicadaException("Ya existe una empresa con este nombre");
        }

        Empresa nuevaEmpresa = new Empresa();

        nuevaEmpresa.setNombre(nombre);
        nuevaEmpresa.setRfc(rfc);
        nuevaEmpresa.setDireccion(empresaDto.direccion() == null ? null : empresaDto.direccion().trim());
        nuevaEmpresa.setTelefono(empresaDto.telefono() == null || empresaDto.telefono().isBlank() ? null : empresaDto.telefono());
        nuevaEmpresa.setActivo(true);

        Empresa empresa = empresaRepository.save(nuevaEmpresa);

        usuarioService.nuevoAdminParaEmpresa(UsuarioMapper.toCreate(adminDto, empresa.getId()));
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

        empresa = empresaRepository.save(empresa);
        logger.info("[COMPANY] : Company update {}", empresa.getId());

        return EmpresaMapper.toResponseEmpresa(empresa);
    }

    // Eliminar empresa
    // Exclusivo de superadmin
    public void deleteEmpresa (Long id) {
        Usuario actual = usuarioService.getUsuarioLogueado();
        if (actual.getEmpresa() != null && actual.getEmpresa().getId().equals(id)) {
            // Desactivarla bloquearia al propio superusuario (cuenta bloqueada)
            throw new IllegalArgumentException("No puedes desactivar la empresa del sistema");
        }
        if (empresaRepository.desactivarEmpresa(id) == 0) {
            throw new EntityNotFoundException("No se encontró la empresa");
        }
        logger.info("[COMPANY] : Company deactivated {}", id);
    }

    public void deleteEmpresa () {
        Usuario admin = usuarioService.getUsuarioLogueado();
        empresaRepository.desactivarEmpresa(admin.getEmpresa().getId());
    }

    public void activate (Long id) {
        int filas = empresaRepository.activarEmpresa(id);

        if (filas == 0) {
            logger.error("[SYS] : Super admin try activate non-existent company. Id: {}", id);
            throw new EntityNotFoundException("No se encontró la empresa");
        }

        if (filas > 1) {
            logger.fatal("[SYS] : CRITICAL - Multiple companies activated with same ID. Id: {}", id);
            throw new IllegalStateException("Error crítico: múltiples empresas afectadas");
        }

        logger.info("[COMPANY] : Company activated {}", id);
    }

    public void activate (String rfc) {
        int filas = empresaRepository.activarEmpresa(rfc);

        if (filas == 0) {
            logger.error("[SYS] : Super admin try activate non-existent company. rfc: {}", rfc);
            throw new EntityNotFoundException("No se encontró la empresa");
        }

        if (filas > 1) {
            logger.fatal("[SYS] : CRITICAL - Multiple companies activated with same ID. rfc: {}", rfc);
            throw new IllegalStateException("Error crítico: múltiples empresas afectadas");
        }

        logger.info("[COMPANY] : Company activated {}", rfc);
    }

    // Ver detalle de empresa
    public EmpresaResponseDTO getEmpresaDetails() {
        Usuario user = usuarioService.getUsuarioLogueado();

        return EmpresaMapper.toResponseEmpresa(user.getEmpresa());
    }

    // Obtener todas las empresas (activas e inactivas, para poder reactivarlas),
    // sin la empresa del sistema a la que pertenece el superusuario.
    public List<EmpresaResponseDTO> getAllEmpresas() {
        Usuario actual = usuarioService.getUsuarioLogueado();
        Long propia = actual.getEmpresa() != null ? actual.getEmpresa().getId() : null;
        List<Empresa> empresas = empresaRepository.findAllByOrderByNombreAsc().stream()
                .filter(e -> !e.getId().equals(propia))
                .toList();
        logger.info("[COMPANY] : Retrieved companies. Total: {}", empresas.size());
        return empresas.stream()
                .map(EmpresaMapper::toResponseEmpresa)
                .toList();
    }
}
