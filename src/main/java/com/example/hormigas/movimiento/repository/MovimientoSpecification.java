package com.example.hormigas.movimiento.repository;

import com.example.hormigas.movimiento.dto.MovimientoFiltroDTO;
import com.example.hormigas.movimiento.entity.Movimiento;
import org.springframework.data.jpa.domain.Specification;

public class MovimientoSpecification {

    public static Specification<Movimiento> conFiltros(Long empresaId, MovimientoFiltroDTO filtro) {
        return (root, query, cb) -> {

            var predicates = cb.conjunction();

            // SIEMPRE filtrar por empresa
            predicates = cb.and(predicates,
                    cb.equal(
                            root.get("inventario")
                                    .get("sucursal")
                                    .get("empresa")
                                    .get("id"),
                            empresaId
                    )
            );

            // sucursal
            if (filtro.sucursalId() != null) {
                predicates = cb.and(predicates,
                        cb.equal(
                                root.get("inventario").get("sucursal").get("id"),
                                filtro.sucursalId()
                        )
                );
            }

            // producto
            if (filtro.productoId() != null) {
                predicates = cb.and(predicates,
                        cb.equal(
                                root.get("inventario").get("producto").get("id"),
                                filtro.productoId()
                        )
                );
            }

            // inventario
            if (filtro.inventarioId() != null) {
                predicates = cb.and(predicates,
                        cb.equal(
                                root.get("inventario").get("id"),
                                filtro.inventarioId()
                        )
                );
            }

            // 🔁 tipo movimiento
            if (filtro.tipo() != null) {
                predicates = cb.and(predicates,
                        cb.equal(root.get("tipoMovimiento"), filtro.tipo())
                );
            }

            return predicates;
        };
    }
}