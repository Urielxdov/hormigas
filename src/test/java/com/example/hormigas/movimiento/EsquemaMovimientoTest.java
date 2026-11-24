package com.example.hormigas.movimiento;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** La idempotencia de la venta batch depende de esta unique y del indice por inventario. */
@SpringBootTest
@ActiveProfiles("test")
class EsquemaMovimientoTest {

    @Autowired JdbcTemplate jdbc;

    @Test
    void existeLaUniqueDeClientIdEInventario() {
        Integer n = jdbc.queryForObject("""
                SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS
                WHERE LOWER(CONSTRAINT_NAME) = 'uk_mov_venta_client_inventario' AND CONSTRAINT_TYPE = 'UNIQUE'
                """, Integer.class);
        assertEquals(1, n);
    }

    @Test
    void existenLosIndicesPorInventario() {
        Integer n = jdbc.queryForObject("""
                SELECT COUNT(*) FROM INFORMATION_SCHEMA.INDEXES
                WHERE LOWER(INDEX_NAME) IN ('idx_mov_inventario', 'idx_mov_inventario_fecha')
                """, Integer.class);
        assertEquals(2, n);
    }
}
