package com.example.hormigas.movimiento.entity;

/**
 * Tipos de movimientos que pueden afectar el inventario.
 *
 * Todo cambio en la cantidad de un producto debe registrarse como un movimiento.
 * Esto permite auditar el inventario y saber por qué subió o bajó el stock.
 */
public enum TipoMovimiento {

    /**
     * Entrada de inventario por compra a proveedor.
     * Aumenta el stock.
     * Ejemplo: se compran 20 refrescos a Coca-Cola.
     */
    COMPRA,

    /**
     * Salida de inventario por venta a un cliente.
     * Disminuye el stock.
     * Ejemplo: se venden 3 refrescos en la tienda.
     */
    VENTA,

    /**
     * Corrección manual del inventario cuando el sistema
     * no coincide con el conteo físico.
     *
     * Puede aumentar o disminuir el stock dependiendo del ajuste.
     * Ejemplo: el sistema dice 10 pero físicamente hay 12.
     */
    AJUSTE,

    /**
     * Pérdida de inventario por daño, caducidad, robo o desperdicio.
     * Siempre disminuye el stock.
     * Ejemplo: productos caducados o rotos.
     */
    MERMA,

    /**
     * Movimiento causado por una devolución.
     *
     * Dependiendo del contexto puede aumentar o disminuir el stock:
     * - Cliente devuelve un producto -> aumenta inventario.
     * - Se devuelve al proveedor -> disminuye inventario.
     */
    DEVOLUCION,

    /**
     * Entrada de inventario proveniente de otra sucursal o almacén.
     * Aumenta el stock en la sucursal destino.
     */
    TRASLADO_ENTRADA,

    /**
     * Salida de inventario hacia otra sucursal o almacén.
     * Disminuye el stock en la sucursal origen.
     */
    TRASLADO_SALIDA
}