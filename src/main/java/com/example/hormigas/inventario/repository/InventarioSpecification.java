package com.example.hormigas.inventario.repository;

import com.example.hormigas.inventario.dto.InventarioFiltroDTO;
import com.example.hormigas.inventario.entity.Inventario;
import org.springframework.data.jpa.domain.Specification;

public class InventarioSpecification {

    public static Specification<Inventario> conFiltros(Long empresaId, InventarioFiltroDTO filter) {
        return ((root, query, criteriaBuilder) -> {
            var predicates = criteriaBuilder.conjunction();

            // Siempre se hace el filtro por empresa
            predicates = criteriaBuilder.and(predicates,
                        criteriaBuilder.equal(
                                root.get("producto")
                                        .get("empresa")
                                        .get("id"),
                                empresaId
                        )
                    );

            //  Sucursal
            if (filter.sucursalId() != null) {
                predicates = criteriaBuilder.and(predicates,
                            criteriaBuilder.equal(
                                    root.get("sucursal")
                                            .get("id"),
                                    filter.sucursalId()
                            )
                );
            }

            // Producto
            if (filter.prodcutoId() != null) {
                predicates = criteriaBuilder.and(predicates,
                        criteriaBuilder.equal(
                                root.get("producto")
                                        .get("id"),
                                filter.prodcutoId()
                        )
                );
            }

            return predicates;
        });
    }

    public static Specification<Inventario> stockBajoPorEmpresa(Long empresaId) {
        return (root, query, cb) -> {
            var empresa = cb.equal(
                root.get("producto").get("empresa").get("id"), empresaId
            );
            var stockBajo = cb.lessThanOrEqualTo(
                root.get("stockActual"), root.get("stockMinimo")
            );
            var tieneMinimo = cb.isNotNull(root.get("stockMinimo"));
            return cb.and(empresa, tieneMinimo, stockBajo);
        };
    }
}
