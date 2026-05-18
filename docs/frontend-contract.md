# Frontend Contract — API de Inventarios

Todos los endpoints requieren JWT en el header:
```
Authorization: Bearer <token>
```

El token se obtiene en `POST /auth/login`. Todos los datos están scoped automáticamente a la empresa del usuario autenticado — el frontend no necesita mandar `empresaId`.

Fechas en formato ISO-8601: `2024-01-15T10:30:00`

---

## Autenticación

### POST /auth/login
```json
// Request
{ "correo": "admin@empresa.com", "password": "contraseña" }

// Response 200
{ "token": "eyJ..." }
```

---

## Inventario

### POST /api/inventario/crear
Registrar un producto en una sucursal con stock inicial.
```json
// Request
{
  "sucursalId": 1,
  "productoId": 5,
  "stockActual": 100,
  "stockMinimo": 10,
  "stockMaximo": 200
}

// Response 200
{
  "id": 42,
  "productoId": 5,
  "productoNombre": "Refresco Cola",
  "precio": 15.50,
  "sucursalId": 1,
  "sucursalNombre": "Sucursal Norte",
  "stockActual": 100,
  "stockMinimo": 10,
  "stockMaximo": 200
}
```

### GET /api/inventario/porSucursal?sucursalId=1
Listar inventario de una sucursal.
```json
// Response 200 — array de InventarioResponseDTO (misma forma que arriba)
[{ "id": 42, "productoId": 5, ... }]
```

---

## Movimientos

### POST /api/movimiento/crear
Registrar un movimiento de stock. Responde con el estado del stock después del movimiento y una alerta si aplica.

```json
// Request
{
  "sucursalId": 1,
  "productoId": 5,
  "tipoMovimiento": "VENTA",
  "cantidad": 3,
  "referencia": "ticket-001",  // opcional
  "motivoId": 2                // opcional — ver endpoint de motivos
}

// Response 200
{
  "id": 101,
  "productoId": 5,
  "productoNombre": "Refresco Cola",
  "sucursalId": 1,
  "sucursalNombre": "Sucursal Norte",
  "tipoMovimiento": "VENTA",
  "cantidad": 3,
  "stockAnterior": 100,
  "stockNuevo": 97,
  "usuarioNombre": "Juan Pérez",
  "referencia": "ticket-001",
  "fecha": "2024-01-15T10:30:00",
  "alerta": null
}

// Response 200 con alerta (cuando stockNuevo < stockMinimo)
{
  "id": 102,
  ...
  "stockNuevo": 8,
  "alerta": {
    "tipo": "STOCK_BAJO",
    "mensaje": "Stock actual (8) por debajo del mínimo (10)"
  }
}
```

**Tipos de alerta posibles:**
| tipo | cuándo mostrar al usuario |
|------|--------------------------|
| `STOCK_CRITICO` | Stock llegó a 0 — mostrar error prominente |
| `STOCK_BAJO` | Stock < mínimo — mostrar advertencia |
| `STOCK_EXCEDIDO` | Stock > máximo — mostrar advertencia |
| `null` | Stock normal — no mostrar nada |

**Valores de `tipoMovimiento`:**
| valor | efecto | usar cuando |
|-------|--------|-------------|
| `COMPRA` | Sube stock | Entrada de proveedor |
| `VENTA` | Baja stock | Salida por venta |
| `AJUSTE` | Establece stock exacto (cantidad = nuevo valor) | Corrección física |
| `MERMA` | Baja stock | Robo, daño, caducidad |
| `DEVOLUCION_CLIENTE` | Sube stock | Cliente regresa producto |
| `DEVOLUCION_PROVEEDOR` | Baja stock | Se regresa al proveedor |
| `DEVOLUCION` | Sube stock | ⚠️ Deprecado, usar `DEVOLUCION_CLIENTE` |

> Los tipos `TRASLADO_SALIDA` y `TRASLADO_ENTRADA` existen pero **no usar directamente** — usar `POST /api/traslado/crear` para traslados entre sucursales.

**Errores posibles:**
- `400` — cantidad <= 0, stock insuficiente, o stock excedería el máximo
- `404` — inventario, sucursal o producto no encontrado

### GET /api/movimiento/buscar
Historial de movimientos con filtros opcionales.

```
GET /api/movimiento/buscar
  ?sucursalId=1          (opcional)
  &productoId=5          (opcional)
  &inventarioId=42       (opcional)
  &tipo=VENTA            (opcional)
  &fechaInicio=2024-01-01T00:00:00  (opcional)
  &fechaFin=2024-01-31T23:59:59     (opcional)
```

```json
// Response 200 — array
[{
  "id": 101,
  "productoId": 5,
  "productoNombre": "Refresco Cola",
  "sucursalId": 1,
  "sucursalNombre": "Sucursal Norte",
  "tipoMovimiento": "VENTA",
  "cantidad": 3,
  "stockAnterior": 100,
  "stockNuevo": 97,
  "usuarioNombre": "Juan Pérez",
  "referencia": "ticket-001",
  "fecha": "2024-01-15T10:30:00",
  "alerta": null
}]
```

---

## Traslados entre sucursales

### POST /api/traslado/crear
Mueve stock de una sucursal a otra de forma atómica. Si falla cualquier validación, ninguna sucursal cambia.

```json
// Request
{
  "sucursalOrigenId": 1,
  "sucursalDestinoId": 2,
  "productoId": 5,
  "cantidad": 20,
  "referencia": "traslado-enero"  // opcional — se genera UUID si se omite
}

// Response 200
{
  "movimientoSalida": {
    "id": 110,
    "sucursalId": 1,
    "sucursalNombre": "Sucursal Norte",
    "tipoMovimiento": "TRASLADO_SALIDA",
    "cantidad": 20,
    "stockAnterior": 100,
    "stockNuevo": 80,
    "alerta": null,
    ...
  },
  "movimientoEntrada": {
    "id": 111,
    "sucursalId": 2,
    "sucursalNombre": "Sucursal Sur",
    "tipoMovimiento": "TRASLADO_ENTRADA",
    "cantidad": 20,
    "stockAnterior": 50,
    "stockNuevo": 70,
    "alerta": null,
    ...
  },
  "referencia": "traslado-enero"
}
```

**Errores posibles:**
- `400` — cantidad <= 0, stock insuficiente en origen, stock excedería máximo en destino, sucursales de distintas empresas
- `404` — inventario del producto no existe en alguna sucursal

---

## Reportes

### GET /api/reportes/movimientos
Movimientos paginados por período. Útil para exportar historial.

```
GET /api/reportes/movimientos
  ?fechaInicio=2024-01-01T00:00:00  (opcional)
  &fechaFin=2024-01-31T23:59:59     (opcional)
  &sucursalId=1    (opcional)
  &productoId=5    (opcional)
  &page=0          (default 0)
  &size=20         (default 20)
```

Response: igual que `GET /api/movimiento/buscar` pero paginado.

### GET /api/reportes/productos-top
Ranking de productos por volumen de movimiento en un período.

```
GET /api/reportes/productos-top
  ?fechaInicio=2024-01-01T00:00:00  (opcional)
  &fechaFin=2024-01-31T23:59:59     (opcional)
  &sucursalId=1   (opcional)
  &limite=10      (default 10)
```

```json
// Response 200
[
  {
    "productoId": 5,
    "nombre": "Refresco Cola",
    "sku": "RC-001",
    "totalEntradas": 120,
    "totalSalidas": 95,
    "netoCambio": 25
  }
]
```

### GET /api/reportes/valor-inventario?sucursalId=1
Valor monetario del inventario de una sucursal.

```json
// Response 200
{
  "sucursalId": 1,
  "nombreSucursal": "Sucursal Norte",
  "valorTotal": 45230.50,
  "productosConPrecio": 48,
  "productosSinPrecio": 3,
  "detalle": [
    {
      "productoId": 5,
      "nombre": "Refresco Cola",
      "sku": "RC-001",
      "stockActual": 100,
      "precio": 15.50,
      "valorLinea": 1550.00,
      "sinPrecio": false
    },
    {
      "productoId": 9,
      "nombre": "Producto sin precio",
      "sku": "SP-002",
      "stockActual": 20,
      "precio": null,
      "valorLinea": 0,
      "sinPrecio": true
    }
  ]
}
```

---

## Motivos de movimiento

Catálogo de razones para un movimiento. Opcional pero mejora la trazabilidad (se manda `motivoId` al crear un movimiento).

### GET /api/motivo
Lista motivos activos de la empresa.

### POST /api/motivo/crear
```json
{ "nombre": "Venta en mostrador", "tipoMovimiento": "VENTA", "descripcion": "..." }
```

---

## Sucursales

### POST /api/sucursal/crear
```json
{ "nombre": "Sucursal Norte", "direccion": "...", "empresaId": 1 }
```

### GET /api/sucursal/buscar
Listar sucursales de la empresa con filtros opcionales.

---

## Productos

### POST /api/producto/crear
```json
{
  "nombre": "Refresco Cola",
  "sku": "RC-001",
  "descripcion": "...",
  "precio": 15.50,
  "categoriaId": 3
}
```

### GET /api/producto/buscar
Listar productos de la empresa.

---

## Notas de integración

1. **Formato de fechas:** siempre ISO-8601 en query params: `2024-01-15T10:30:00`. Sin timezone — el servidor usa UTC.

2. **Alertas de stock:** el campo `alerta` puede ser `null`. Siempre verificar antes de acceder a `alerta.tipo`.

3. **AJUSTE:** el campo `cantidad` en este tipo de movimiento es el **nuevo valor absoluto del stock**, no una diferencia. Ej: si stock es 10 y se ajusta a 12, mandar `cantidad: 12`.

4. **Traslados:** el producto debe tener inventario registrado en **ambas** sucursales antes de poder trasladar. Si no existe en la sucursal destino, crear inventario primero con `POST /api/inventario/crear` con `stockActual: 0`.

5. **Paginación en reportes:** `page` empieza en 0. Para saber si hay más páginas, verificar si el array de respuesta tiene `size` elementos.
