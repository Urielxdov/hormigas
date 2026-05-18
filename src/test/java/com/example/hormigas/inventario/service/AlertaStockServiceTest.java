package com.example.hormigas.inventario.service;

import com.example.hormigas.inventario.dto.AlertaStock;
import com.example.hormigas.inventario.entity.Inventario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AlertaStockServiceTest {

    private AlertaStockService service;

    @BeforeEach
    void setUp() {
        service = new AlertaStockService();
    }

    private Inventario inventario(int actual, Integer minimo, int maximo) {
        Inventario inv = new Inventario();
        inv.setStockActual(actual);
        inv.setStockMinimo(minimo);
        inv.setStockMaximo(maximo);
        return inv;
    }

    @Test
    void noAlerta_cuandoStockEnRangoNormal() {
        assertThat(service.evaluar(inventario(10, 5, 20))).isNull();
    }

    @Test
    void alertaCritico_cuandoStockEsCero() {
        AlertaStock alerta = service.evaluar(inventario(0, 5, 20));
        assertThat(alerta).isNotNull();
        assertThat(alerta.tipo()).isEqualTo("STOCK_CRITICO");
    }

    @Test
    void alertaBajo_cuandoStockMenorQueMinimo() {
        AlertaStock alerta = service.evaluar(inventario(3, 5, 20));
        assertThat(alerta).isNotNull();
        assertThat(alerta.tipo()).isEqualTo("STOCK_BAJO");
    }

    @Test
    void alertaExcedido_cuandoStockMayorQueMaximo() {
        AlertaStock alerta = service.evaluar(inventario(25, 5, 20));
        assertThat(alerta).isNotNull();
        assertThat(alerta.tipo()).isEqualTo("STOCK_EXCEDIDO");
    }

    @Test
    void alertaCritico_tienePrioridadSobreBajo() {
        AlertaStock alerta = service.evaluar(inventario(0, 5, 20));
        assertThat(alerta.tipo()).isEqualTo("STOCK_CRITICO");
    }

    @Test
    void noAlerta_cuandoStockMinimoEsNull() {
        assertThat(service.evaluar(inventario(3, null, 20))).isNull();
    }
}
