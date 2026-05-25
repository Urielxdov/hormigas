package com.example.hormigas.producto.service;

import com.example.hormigas.producto.dto.CategoriaResponseDTO;
import com.example.hormigas.producto.dto.NuevaCategoriaDTO;
import com.example.hormigas.producto.entity.Categoria;
import com.example.hormigas.producto.repository.CategoriaRepository;
import com.example.hormigas.security.domain.Usuario;
import com.example.hormigas.security.domain.services.UsuarioService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final UsuarioService usuarioService;

    public CategoriaService(CategoriaRepository categoriaRepository, UsuarioService usuarioService) {
        this.categoriaRepository = categoriaRepository;
        this.usuarioService = usuarioService;
    }

    public List<CategoriaResponseDTO> listarActivas() {
        Usuario user = usuarioService.getUsuarioLogueado();
        return categoriaRepository
                .findByEmpresaIdAndActivoTrue(user.getEmpresa().getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public CategoriaResponseDTO crear(NuevaCategoriaDTO dto) {
        Usuario user = usuarioService.getUsuarioLogueado();
        Categoria cat = new Categoria();
        cat.setEmpresa(user.getEmpresa());
        cat.setNombre(dto.nombre());
        cat.setDescripcion(dto.descripcion());
        return toResponse(categoriaRepository.save(cat));
    }

    public CategoriaResponseDTO toggleActivo(Long id) {
        Usuario user = usuarioService.getUsuarioLogueado();
        Categoria cat = categoriaRepository
                .findByIdAndEmpresaId(id, user.getEmpresa().getId())
                .orElseThrow(() -> new EntityNotFoundException("Categoría no encontrada"));
        cat.setActivo(!cat.isActivo());
        return toResponse(categoriaRepository.save(cat));
    }

    private CategoriaResponseDTO toResponse(Categoria cat) {
        return new CategoriaResponseDTO(cat.getId(), cat.getNombre(), cat.getDescripcion(), cat.isActivo());
    }
}
