# Hormigas — API REST de Inventarios

API REST multi-empresa para gestión de inventarios. Soporta múltiples empresas, múltiples sucursales por empresa, registro de movimientos con historial completo, traslados atómicos entre sucursales, alertas de stock y reportes.

---

## Stack

| Capa | Tecnología |
|------|------------|
| Lenguaje | Java 21 |
| Framework | Spring Boot 3.2.5 |
| Seguridad | Spring Security + JWT |
| Persistencia | Spring Data JPA + Hibernate |
| Base de datos | PostgreSQL |
| Build | Maven |

---

## Modelo de dominio

```
Superusuario
  └── Empresa
        ├── Usuario (N por empresa, con roles)
        ├── Producto (N por empresa, con SKU único + precio)
        ├── MotivoMovimiento (catálogo de razones por empresa)
        └── Sucursal (N por empresa)
              └── Inventario (1 por producto/sucursal, con stock min/max)
                    └── Movimiento (historial completo de cambios de stock)
```

Todos los datos están scoped a la empresa del usuario autenticado. El frontend no necesita enviar `empresaId`.

---

## Instalación

**Requisitos:** Java 21, Maven, PostgreSQL

```bash
git clone https://github.com/Urielxdov/hormigas.git
cd hormigas
```

Copiar `.env.example` a `.env` y configurar:

```properties
DB_HOST=localhost
DB_PORT=5432
DB_NAME=inventarios
DB_USER=postgres
DB_PASSWORD=tu_password

JWT_SECRET=tu_clave_secreta_larga
JWT_EXPIRATION=86400000

ADMIN_EMAIL=superadmin@sistema.com
ADMIN_PASSWORD=tu_password
ADMIN_NOMBRE=Super Admin
```

```bash
# Con Docker (recomendado)
docker-compose up

# Sin Docker
./mvnw spring-boot:run
```

Al arrancar, se crea automáticamente el superusuario inicial si no existe.

---

## Autenticación

Todos los endpoints requieren JWT en el header:

```
Authorization: Bearer <token>
```

**Obtener token:**
```
POST /auth/login
{ "correo": "admin@empresa.com", "password": "contraseña" }
```

### Roles

| Rol | Capacidades |
|-----|-------------|
| `SUPER_ADMIN` | Crea empresas y sus usuarios administradores |
| `ADMIN` | Administra su empresa, sucursales, productos, inventario |
| `USUARIO` | Operación diaria: movimientos, consultas |

---

## Endpoints

### Autenticación
| Método | Ruta | Descripción |
|--------|------|-------------|
| POST | `/auth/login` | Obtener JWT |

### Empresas
| Método | Ruta | Descripción |
|--------|------|-------------|
| POST | `/api/empresa/crearConAdmin` | Crear empresa + usuario admin (solo SUPER_ADMIN) |
| GET | `/api/empresa` | Listar empresas |
| PUT | `/api/empresa/{id}` | Actualizar empresa |
| DELETE | `/api/empresa/{id}` | Eliminar empresa |

### Usuarios
| Método | Ruta | Descripción |
|--------|------|-------------|
| POST | `/api/usuario/crear` | Crear usuario en la empresa |
| GET | `/api/usuario` | Listar usuarios de la empresa |
| PUT | `/api/usuario/{id}` | Actualizar usuario |

### Sucursales
| Método | Ruta | Descripción |
|--------|------|-------------|
| POST | `/api/sucursal/crear` | Crear sucursal |
| GET | `/api/sucursal/buscar` | Listar sucursales con filtros |
| PUT | `/api/sucursal/{id}` | Actualizar sucursal |

### Productos
| Método | Ruta | Descripción |
|--------|------|-------------|
| POST | `/api/producto/crear` | Crear producto (SKU único por empresa) |
| GET | `/api/producto/buscar` | Listar productos con filtros |
| PUT | `/api/producto/{id}` | Actualizar producto |

### Inventario
| Método | Ruta | Descripción |
|--------|------|-------------|
| POST | `/api/inventario/crear` | Registrar producto en sucursal con stock inicial |
| GET | `/api/inventario/porSucursal?sucursalId=` | Inventario de una sucursal |

### Movimientos de stock
| Método | Ruta | Descripción |
|--------|------|-------------|
| POST | `/api/movimiento/crear` | Registrar movimiento (ver tipos abajo) |
| GET | `/api/movimiento/buscar` | Historial con filtros (sucursal, producto, tipo, fechas) |

**Tipos de movimiento:**

| Tipo | Efecto | Uso |
|------|--------|-----|
| `COMPRA` | +stock | Entrada de proveedor |
| `VENTA` | -stock | Salida por venta |
| `AJUSTE` | =cantidad | Corrección física (cantidad = nuevo valor absoluto) |
| `MERMA` | -stock | Robo, daño, caducidad |
| `DEVOLUCION_CLIENTE` | +stock | Cliente devuelve producto |
| `DEVOLUCION_PROVEEDOR` | -stock | Devolución al proveedor |
| `TRASLADO_ENTRADA` | +stock | Llegada de traslado entre sucursales |
| `TRASLADO_SALIDA` | -stock | Salida de traslado entre sucursales |

> Para traslados entre sucursales usar `POST /api/traslado/crear` en lugar de los tipos `TRASLADO_*` directamente — garantiza atomicidad.

**Alertas en response:** cada movimiento retorna `alerta` con el estado del stock resultante:

```json
{
  "stockNuevo": 3,
  "alerta": {
    "tipo": "STOCK_BAJO",
    "mensaje": "Stock actual (3) por debajo del mínimo (5)"
  }
}
```

| tipo | condición |
|------|-----------|
| `STOCK_CRITICO` | stock == 0 |
| `STOCK_BAJO` | stock < stockMinimo |
| `STOCK_EXCEDIDO` | stock > stockMaximo |
| `null` | stock en rango normal |

### Traslados atómicos
| Método | Ruta | Descripción |
|--------|------|-------------|
| POST | `/api/traslado/crear` | Mover stock entre sucursales (transaccional) |

Si falla cualquier validación (stock insuficiente en origen, se excedería máximo en destino), ninguna sucursal se modifica.

### Motivos de movimiento
| Método | Ruta | Descripción |
|--------|------|-------------|
| POST | `/api/motivo/crear` | Crear motivo |
| GET | `/api/motivo` | Listar motivos activos |
| PUT | `/api/motivo/{id}` | Actualizar motivo |

### Reportes
| Método | Ruta | Descripción |
|--------|------|-------------|
| GET | `/api/reportes/movimientos` | Movimientos paginados por período |
| GET | `/api/reportes/productos-top` | Ranking de productos por volumen movido |
| GET | `/api/reportes/valor-inventario` | Valor monetario del inventario por sucursal |

Todos filtran por empresa del usuario logueado. Parámetros comunes: `fechaInicio`, `fechaFin`, `sucursalId`.

---

## Reglas de negocio

- `stockMaximo` se valida en entradas (COMPRA, DEVOLUCION_CLIENTE, TRASLADO_ENTRADA) — error si se excede
- `stockMinimo` genera alerta en el response cuando se viola, no bloquea la operación
- `cantidad` siempre debe ser > 0
- En `AJUSTE`, `cantidad` es el nuevo valor absoluto del stock (no una diferencia)
- Cada movimiento registra `stockAnterior` y `stockNuevo` para historial inmutable
- `ultimaActualizacion` del inventario se actualiza automáticamente con cada movimiento
- El par `(sucursal, producto)` es único en inventario — no puede haber dos registros del mismo producto en la misma sucursal

---

## Estructura del proyecto

```
src/main/java/com/example/hormigas/
├── empresa/          — CRUD de empresas
├── sucursal/         — CRUD de sucursales
├── producto/         — CRUD de productos + categorías
├── inventario/       — Stock por sucursal + alertas
├── movimiento/       — Historial de movimientos
├── traslado/         — Traslados atómicos entre sucursales
├── motivo/           — Catálogo de motivos de movimiento
├── reporte/          — Reportes de negocio
└── security/         — JWT, usuarios, roles, filtros
```

---

## Contrato de API para frontend

Ver [`docs/frontend-contract.md`](docs/frontend-contract.md) — documenta todos los endpoints con ejemplos de request/response, tipos de movimiento, manejo de alertas y notas de integración.

---

## Deuda técnica conocida

- Roles sin `@PreAuthorize` granular por endpoint — seguridad a nivel de ruta global
- Sin paginación en la mayoría de listados (solo en reportes)
- Sin Swagger/OpenAPI generado automáticamente
- Tests solo en `AlertaStockService` — falta cobertura de servicios e integración
