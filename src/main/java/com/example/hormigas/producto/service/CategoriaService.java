package com.example.hormigas.producto.service;

import com.example.hormigas.producto.dto.CategoriaResponseDTO;
import com.example.hormigas.producto.dto.NuevaCategoriaDTO;
import com.example.hormigas.producto.entity.Categoria;
import com.example.hormigas.producto.repository.CategoriaRepository;
import com.example.hormigas.security.domain.Usuario;
import com.example.hormigas.security.domain.services.UsuarioService;
import com.example.hormigas.shared.exception.ConflictException;
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

    /** Activas (para asignar a productos) o todas (pantalla de gestion, para poder reactivarlas). */
    public List<CategoriaResponseDTO> listar(boolean incluirInactivas) {
        Long empresaId = usuarioService.getUsuarioLogueado().getEmpresa().getId();
        var categorias = incluirInactivas
                ? categoriaRepository.findByEmpresaIdOrderByNombreAsc(empresaId)
                : categoriaRepository.findByEmpresaIdAndActivoTrueOrderByNombreAsc(empresaId);
        return categorias.stream().map(this::toResponse).toList();
    }

    public CategoriaResponseDTO crear(NuevaCategoriaDTO dto) {
        Usuario user = usuarioService.getUsuarioLogueado();
        String nombre = normalizar(dto.nombre());
        validarNombreLibre(user.getEmpresa().getId(), nombre, null);
        Categoria cat = new Categoria();
        cat.setEmpresa(user.getEmpresa());
        cat.setNombre(nombre);
        cat.setDescripcion(descripcion(dto.descripcion()));
        return toResponse(categoriaRepository.save(cat));
    }

    public CategoriaResponseDTO actualizar(Long id, NuevaCategoriaDTO dto) {
        Usuario user = usuarioService.getUsuarioLogueado();
        Categoria cat = buscar(id, user.getEmpresa().getId());
        String nombre = normalizar(dto.nombre());
        validarNombreLibre(user.getEmpresa().getId(), nombre, id);
        cat.setNombre(nombre);
        cat.setDescripcion(descripcion(dto.descripcion()));
        return toResponse(categoriaRepository.save(cat));
    }

    public CategoriaResponseDTO toggleActivo(Long id) {
        Usuario user = usuarioService.getUsuarioLogueado();
        Categoria cat = buscar(id, user.getEmpresa().getId());
        cat.setActivo(!cat.isActivo());
        return toResponse(categoriaRepository.save(cat));
    }

    private Categoria buscar(Long id, Long empresaId) {
        return categoriaRepository.findByIdAndEmpresaId(id, empresaId)
                .orElseThrow(() -> new EntityNotFoundException("Categoría no encontrada"));
    }

    private void validarNombreLibre(Long empresaId, String nombre, Long idActual) {
        categoriaRepository.findByEmpresaIdAndNombreIgnoreCase(empresaId, nombre)
                .filter(c -> !c.getId().equals(idActual))
                .ifPresent(c -> {
                    throw new ConflictException(c.isActivo()
                            ? "Ya existe una categoría con ese nombre"
                            : "Ya existe una categoría inactiva con ese nombre; reactívala");
                });
    }

    /** Recorta y colapsa espacios repetidos. */
    private static String normalizar(String nombre) {
        return nombre.trim().replaceAll("\\s+", " ");
    }

    private static String descripcion(String d) {
        return d == null || d.isBlank() ? null : d.trim();
    }

    private CategoriaResponseDTO toResponse(Categoria cat) {
        return new CategoriaResponseDTO(cat.getId(), cat.getNombre(), cat.getDescripcion(), cat.isActivo());
    }
}
