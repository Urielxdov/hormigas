package com.example.hormigas.movimiento.entity;

public enum TipoMovimiento {

    COMPRA(1),
    VENTA(-1),
    AJUSTE(0),
    MERMA(-1),

    /** @deprecated Usar DEVOLUCION_CLIENTE o DEVOLUCION_PROVEEDOR */
    @Deprecated
    DEVOLUCION(1),

    DEVOLUCION_CLIENTE(1),
    DEVOLUCION_PROVEEDOR(-1),

    TRASLADO_ENTRADA(1),
    TRASLADO_SALIDA(-1);

    private final int factor;

    TipoMovimiento(int factor) {
        this.factor = factor;
    }

    public int aplicar(int stockActual, int cantidad) {
        if (this == AJUSTE) {
            return cantidad;
        }
        return stockActual + (cantidad * factor);
    }

    public boolean esEntrada() {
        return factor > 0;
    }
}
