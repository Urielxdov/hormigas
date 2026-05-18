package com.example.hormigas.inventario.service;

import com.example.hormigas.inventario.dto.AlertaStock;
import com.example.hormigas.inventario.entity.Inventario;
import org.springframework.stereotype.Service;

@Service
public class AlertaStockService {

    public AlertaStock evaluar(Inventario inventario) {
        int stock = inventario.getStockActual();

        if (stock == 0) {
            return new AlertaStock("STOCK_CRITICO", "Stock en cero");
        }
        if (inventario.getStockMinimo() != null && stock < inventario.getStockMinimo()) {
            return new AlertaStock("STOCK_BAJO",
                    "Stock actual (" + stock + ") por debajo del mínimo (" + inventario.getStockMinimo() + ")");
        }
        if (stock > inventario.getStockMaximo()) {
            return new AlertaStock("STOCK_EXCEDIDO",
                    "Stock actual (" + stock + ") excede el máximo (" + inventario.getStockMaximo() + ")");
        }
        return null;
    }
}
