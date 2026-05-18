# Inventario Completo — Design Spec

**Fecha:** 2026-05-18  
**Enfoque:** Fix-first incremental (Opción A)  
**Constraint:** No romper endpoints existentes  
**Tipo de sistema:** API REST, multi-empresa, multi-sucursal, mixto (retail/almacén/food)

---

## Contexto

Sistema Spring Boot con 7 módulos: `inventario`, `movimiento`, `motivo`, `producto`, `sucursal`, `empresa`, `security`. El modelo de datos es correcto en estructura, pero tiene bugs críticos en la capa de servicio y faltan funcionalidades típicas de un sistema de inventario.

---

## Fase 1 — Bug Fixes

### 1.1 Movimiento sin inventario (CRÍTICO)

**Archivo:** `MovimientoService.java:80`  
**Fix:** Agregar `movimiento.setInventario(inventario)` antes de `movimientoRepository.save(movimiento)`.

Sin este fix, `inventario_id` queda `null` en BD. `MovimientoSpecification` siempre filtra por `inventario.sucursal.empresa.id` — todas las queries de historial fallan o devuelven vacío.

### 1.2 `ultimaActualizacion` nunca se actualiza

**Archivos:** `MovimientoService.java`, `InventarioService.java`  
**Fix:** Agregar `inventario.setUltimaActualizacion(LocalDateTime.now())` en cada operación que modifique `stockActual` antes del `save`.

### 1.3 `@Index` con columnas inexistentes

**Archivo:** `Movimiento.java:16-18`  
**Fix:** Eliminar los `@Index` de `producto_id` y `sucursal_id`. `Movimiento` no tiene esas columnas directamente — se acceden vía `inventario`. Solo conservar el índice de `fecha`.

### 1.4 `MotivoMovimiento` desconectado

**Archivos:** `Movimiento.java`, `MovimientoService.java`, `CrearMovimientoDTO.java`  
**Fix:**
- Descomentar el campo `motivo` en `Movimiento`
- Agregar `motivoId` opcional a `CrearMovimientoDTO`
- En `MovimientoService.registrarMovimiento()`: si viene `motivoId`, buscar y asignar el motivo

### 1.5 Typo en `InventarioFiltroDTO`

**Archivo:** `InventarioFiltroDTO.java`  
**Fix:** Renombrar `prodcutoId()` → `productoId()`. Actualizar `InventarioSpecification` que lo consume.

---

## Fase 2 — Reglas de Negocio

### 2.1 Validación de `cantidad`

Agregar `@Positive` a `CrearMovimientoDTO.cantidad`. Rechaza `0` y negativos con `400 Bad Request`.

### 2.2 Validación de `stockMaximo` en entradas

En `MovimientoService`, después de calcular `nuevoStock`, para movimientos de tipo entrada:

```java
if (nuevoStock > inventario.getStockMaximo()) {
    throw new IllegalArgumentException("Stock excede el máximo permitido (" + inventario.getStockMaximo() + ")");
}
```

Aplica a: `COMPRA`, `DEVOLUCION_CLIENTE`, `TRASLADO_ENTRADA`.

### 2.3 Split de `DEVOLUCION`

Agregar dos nuevos valores al enum `TipoMovimiento`:
- `DEVOLUCION_CLIENTE` — factor `+1` (cliente devuelve producto, sube stock)
- `DEVOLUCION_PROVEEDOR` — factor `-1` (se devuelve al proveedor, baja stock)

`DEVOLUCION` original se conserva con factor `+1` para compatibilidad con clientes existentes. Documentar como deprecado.

### 2.4 Protección de `agregarASucursal`

Antes de ejecutar el `UPDATE`, validar que no exista ya un inventario con `(sucursalDestino, producto)`. Si existe, lanzar error descriptivo en vez de violar el unique constraint silenciosamente.

---

## Fase 3 — Traslados Atómicos

### Nuevo endpoint

```
POST /api/traslado/crear
```

**Request:**
```json
{
  "sucursalOrigenId": 1,
  "sucursalDestinoId": 2,
  "productoId": 5,
  "cantidad": 10,
  "referencia": "opcional"
}
```

**Lógica interna (`@Transactional`):**
1. Buscar inventario en sucursal origen → validar stock suficiente
2. Buscar inventario en sucursal destino → validar que no exceda `stockMaximo`
3. Crear `Movimiento` tipo `TRASLADO_SALIDA` en origen
4. Crear `Movimiento` tipo `TRASLADO_ENTRADA` en destino
5. Actualizar `stockActual` y `ultimaActualizacion` en ambos inventarios
6. Asignar mismo UUID generado como `referencia` en ambos movimientos para vincularlos

Si cualquier paso falla → rollback total.

**Response:**
```json
{
  "movimientoSalida": { ... },
  "movimientoEntrada": { ... },
  "referencia": "uuid-del-traslado"
}
```

**Endpoints existentes** `POST /api/movimiento/crear` con `TRASLADO_SALIDA`/`TRASLADO_ENTRADA` siguen funcionando pero no garantizan atomicidad.

### Nuevos archivos
- `TrasladoController.java`
- `TrasladoService.java`
- `CrearTrasladoDTO.java`
- `TrasladoResponseDTO.java`

---

## Fase 4 — Alertas de Stock

Después de cada movimiento, evaluar el estado del inventario resultante y retornar alerta en el response.

### Cambio en `MovimientoResponseDTO`

Agregar campo opcional `alerta`:

```json
{
  "id": 1,
  "tipoMovimiento": "VENTA",
  "stockNuevo": 3,
  "alerta": {
    "tipo": "STOCK_BAJO",
    "mensaje": "Stock actual (3) por debajo del mínimo (5)"
  }
}
```

`alerta` es `null` cuando el stock está en rango normal.

### Tipos de alerta

| Tipo | Condición |
|------|-----------|
| `STOCK_CRITICO` | `stockNuevo == 0` |
| `STOCK_BAJO` | `stockNuevo < stockMinimo` (y `stockMinimo != null`) |
| `STOCK_EXCEDIDO` | `stockNuevo > stockMaximo` |

Prioridad: `STOCK_CRITICO` > `STOCK_BAJO` > `STOCK_EXCEDIDO`. Solo se retorna una alerta por respuesta.

### Nuevo archivo
- `AlertaStock.java` — record con `tipo` y `mensaje`
- `AlertaStockService.java` — lógica de evaluación (invocado desde `MovimientoService` y `TrasladoService`)

---

## Fase 5 — Reportes

Todos los endpoints bajo `/api/reportes`. Requieren autenticación. Filtran siempre por empresa del usuario logueado.

### 5.1 Movimientos por período

```
GET /api/reportes/movimientos?fechaInicio=&fechaFin=&sucursalId=&productoId=&tipo=&page=0&size=20
```

**Cambio transversal:** agregar `fechaInicio` y `fechaFin` a `MovimientoFiltroDTO` y `MovimientoSpecification`. Los endpoints existentes de movimientos también se benefician.

**Response:** lista paginada de `MovimientoResponseDTO` existente.

### 5.2 Productos más movidos

```
GET /api/reportes/productos-top?fechaInicio=&fechaFin=&sucursalId=&limite=10
```

Agrupa movimientos por producto en el período. Suma cantidad separada por entradas y salidas.

**Response:**
```json
[
  {
    "productoId": 3,
    "nombre": "Refresco Cola",
    "sku": "RC-001",
    "totalEntradas": 120,
    "totalSalidas": 95,
    "netoCambio": 25
  }
]
```

Implementado con query JPQL de agregación en `MovimientoRepository`.

### 5.3 Valor total de inventario

```
GET /api/reportes/valor-inventario?sucursalId=
```

Calcula `SUM(stockActual * precio)` agrupado por sucursal. `Producto.precio` ya existe como `BigDecimal`.

Productos sin precio (`precio == null`) se incluyen con valor `0` y se marcan con flag `sinPrecio: true`.

**Response:**
```json
{
  "sucursalId": 1,
  "nombreSucursal": "Sucursal Norte",
  "valorTotal": 45230.50,
  "productosConPrecio": 48,
  "productosSinPrecio": 3,
  "detalle": [
    { "productoId": 5, "nombre": "Refresco", "stockActual": 100, "precio": 15.50, "valorLinea": 1550.00 }
  ]
}
```

### Nuevos archivos
- `ReporteController.java`
- `ReporteService.java`
- DTOs de respuesta para cada reporte

---

## Resumen de cambios por fase

| Fase | Archivos modificados | Archivos nuevos |
|------|---------------------|-----------------|
| 1 - Bugs | 5 archivos existentes | 0 |
| 2 - Reglas | `TipoMovimiento`, `MovimientoService`, `CrearMovimientoDTO` | 0 |
| 3 - Traslados | 0 | 4 archivos |
| 4 - Alertas | `MovimientoResponseDTO`, `MovimientoService` | 2 archivos |
| 5 - Reportes | `MovimientoFiltroDTO`, `MovimientoSpecification`, `MovimientoRepository` | ~6 archivos |

**Total endpoints existentes afectados:** 0 (solo enriquecidos con campos opcionales o validaciones adicionales).
