package com.example.hormigas.empresa.repository;

import com.example.hormigas.empresa.entity.Empresa;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface EmpresaRepository extends JpaRepository<Empresa, Long> {
    Optional<Empresa> findByRfc(String rfc);
    Optional<Empresa> findByNombre(String nombre);


    boolean existsByNombre(String nombre);
    boolean existsByRfc(String rfc);

    @Modifying
    @Transactional
    @Query("""
            UPDATE Empresa e
            SET e.activo = false
            WHERE e.id = :empresaId
            """)
    int desactivarEmpresa(@Param("empresaId") Long empresaId);

    @Modifying
    @Transactional
    @Query("""
        UPDATE Empresa e
        SET e.activo = true
        WHERE e.id = :empresaId
    """)
    int activarEmpresa(@Param("empresaId") Long empresaId);

    @Modifying
    @Transactional
    @Query("""
        UPDATE Empresa e
        SET e.activo = true
        WHERE e.rfc = :rfc
    """)
    int activarEmpresa(@Param("rfc") String rfc);
}
