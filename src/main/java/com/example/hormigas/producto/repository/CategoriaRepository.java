package com.example.hormigas.producto.repository;

import com.example.hormigas.producto.entity.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
    List<Categoria> findByEmpresaIdAndActivoTrue(Long empresaId);
    List<Categoria> findByEmpresaIdAndActivoTrueOrderByNombreAsc(Long empresaId);
    List<Categoria> findByEmpresaIdOrderByNombreAsc(Long empresaId);
    Optional<Categoria> findByIdAndEmpresaId(Long id, Long empresaId);
    Optional<Categoria> findByEmpresaIdAndNombreIgnoreCase(Long empresaId, String nombre);
}
