# API Endpoints

Base URL: `/api`

Auth: JWT via cookie (`auth_token`) o header `Authorization: Bearer <token>`.

---

## Auth — `/api/auth`

| Método | Ruta | Acceso | Descripción |
|--------|------|--------|-------------|
| `POST` | `/api/auth` | Público | Crear usuario |
| `POST` | `/api/auth/login` | Público | Login → retorna token JWT |
| `POST` | `/api/auth/logout` | Público | Logout (limpia cookie) |

### POST `/api/auth/login`
**Body:**
```json
{
  "email": "string",
  "password": "string"
}
```
**Response `200`:**
```json
{ "token": "string" }
```

### POST `/api/auth`
**Body:**
```json
{
  "correo": "string",
  "password": "string",
  "nombre": "string",
  "empresaId": 1
}
```

---

## Usuario — `/api/usuario`

| Método | Ruta | Roles | Descripción |
|--------|------|-------|-------------|
| `POST` | `/api/usuario/create` | `SUPER_ADMIN` | Crear usuario |
| `GET` | `/api/usuario/list` | `ADMIN`, `SUPER_ADMIN` | Listar usuarios |

### POST `/api/usuario/create`
**Body:**
```json
{
  "correo": "string",
  "password": "string",
  "nombre": "string",
  "empresaId": 1
}
```
**Response:**
```json
{
  "id": 1,
  "name": "string",
  "correo": "string",
  "empresaId": 1
}
```

### GET `/api/usuario/list`
**Response:**
```json
[
  { "id": 1, "name": "string", "correo": "string", "empresaId": 1 }
]
```

---

## Empresa — `/api/empresa`

| Método | Ruta | Roles | Descripción |
|--------|------|-------|-------------|
| `POST` | `/api/empresa/create` | `SUPER_ADMIN` | Crear empresa + admin |
| `GET` | `/api/empresa/` | `ADMIN`, `SUPER_ADMIN` | Obtener empresa propia |
| `GET` | `/api/empresa/all` | `ADMIN`, `SUPER_ADMIN` | Listar todas las empresas |
| `PATCH` | `/api/empresa/update` | `ADMIN`, `SUPER_ADMIN` | Actualizar empresa |
| `PATCH` | `/api/empresa/{id}/activate` | `SUPER_ADMIN` | Activar empresa por ID |
| `PATCH` | `/api/empresa/{rfc}/activate` | `SUPER_ADMIN` | Activar empresa por RFC |
| `DELETE` | `/api/empresa/delete/{id}` | `SUPER_ADMIN` | Eliminar empresa por ID |
| `DELETE` | `/api/empresa/delete` | `SUPER_ADMIN` | Eliminar empresa propia |

### POST `/api/empresa/create`
**Body:**
```json
{
  "empresa": {
    "nombre": "string",
    "rfc": "string",
    "direccion": "string",
    "telefono": "string"
  },
  "admin": {
    "nombre": "string",
    "correo": "string",
    "password": "string"
  }
}
```
**Response `201`:**
```json
{
  "id": 1,
  "nombre": "string",
  "rfc": "string",
  "direccion": "string",
  "telefono": "string"
}
```

### PATCH `/api/empresa/update`
**Body:**
```json
{
  "nombre": "string",
  "rfc": "string",
  "direccion": "string",
  "telefono": "string"
}
```

---

## Sucursal — `/api/sucursal`

| Método | Ruta | Roles | Descripción |
|--------|------|-------|-------------|
| `POST` | `/api/sucursal/crear` | Autenticado | Crear sucursal |
| `GET` | `/api/sucursal/listar` | Autenticado | Listar sucursales de la empresa |

### POST `/api/sucursal/crear`
**Body:**
```json
{
  "nombre": "string",
  "direccion": "string"
}
```
**Response:**
```json
{
  "id": 1,
  "nombre": "string",
  "direccion": "string",
  "activa": true
}
```

### GET `/api/sucursal/listar`
**Response:**
```json
[
  { "id": 1, "nombre": "string", "direccion": "string", "activa": true }
]
```

---

## Producto — `/api/producto`

| Método | Ruta | Roles | Descripción |
|--------|------|-------|-------------|
| `GET` | `/api/producto/` | Autenticado | Listar productos (paginado) |
| `POST` | `/api/producto/nuevo` | Autenticado | Crear producto |

### GET `/api/producto/`
**Query params:** `page`, `size`, `sort` (Spring `Pageable`)

**Response:**
```json
{
  "content": [
    {
      "id": 1,
      "categoria": "string",
      "nombre": "string",
      "descripcion": "string",
      "sku": "string",
      "precio": 0.00,
      "activo": true
    }
  ],
  "totalElements": 1,
  "totalPages": 1
}
```

### POST `/api/producto/nuevo`
**Body:**
```json
{
  "nombre": "string",
  "descripcion": "string",
  "sku": "string",
  "precio": 0.00
}
```

---

## Inventario — `/api/inventario`

| Método | Ruta | Roles | Descripción |
|--------|------|-------|-------------|
| `POST` | `/api/inventario/crear` | Autenticado | Crear registro de inventario |
| `GET` | `/api/inventario/porSucursal` | Autenticado | Inventario por sucursal |

### POST `/api/inventario/crear`
**Body:**
```json
{
  "sucursalId": 1,
  "productoId": 1,
  "stockActual": 100,
  "stockMinimo": 10,
  "stockMaximo": 500
}
```
**Response:**
```json
{
  "id": 1,
  "productoId": 1,
  "productoNombre": "string",
  "precio": 0.00,
  "sucursalId": 1,
  "sucursalNombre": "string",
  "stockActual": 100,
  "stockMinimo": 10,
  "stockMaximo": 500
}
```

### GET `/api/inventario/porSucursal`
**Query params:** `sucursalId` (Long)

---

## Movimiento — `/api/movimiento`

| Método | Ruta | Roles | Descripción |
|--------|------|-------|-------------|
| `POST` | `/api/movimiento/crear` | Autenticado | Registrar movimiento de inventario |
| `GET` | `/api/movimiento/buscar` | Autenticado | Buscar movimientos con filtros |

### POST `/api/movimiento/crear`
**Body:**
```json
{
  "sucursalId": 1,
  "productoId": 1,
  "tipoMovimiento": "ENTRADA | SALIDA",
  "cantidad": 10,
  "referencia": "string"
}
```
**Response:**
```json
{
  "id": 1,
  "productoId": 1,
  "productoNombre": "string",
  "sucursalId": 1,
  "sucursalNombre": "string",
  "tipoMovimiento": "ENTRADA",
  "cantidad": 10,
  "usuarioNombre": "string",
  "referencia": "string",
  "fecha": "2026-05-11T00:00:00"
}
```

### GET `/api/movimiento/buscar`
**Query params** (todos opcionales):
| Param | Tipo | Descripción |
|-------|------|-------------|
| `sucursalId` | Long | Filtrar por sucursal |
| `productoId` | Long | Filtrar por producto |
| `inventarioId` | Long | Filtrar por inventario |
| `tipo` | `ENTRADA\|SALIDA` | Filtrar por tipo |

---

## Motivo de Movimiento — `/api/motivos-movimiento`

| Método | Ruta | Roles | Descripción |
|--------|------|-------|-------------|
| `POST` | `/api/motivos-movimiento` | Autenticado | Crear motivo |
| `GET` | `/api/motivos-movimiento/{empresaId}` | Autenticado | Listar motivos de empresa |
| `PUT` | `/api/motivos-movimiento/{id}` | Autenticado | Actualizar motivo |
| `DELETE` | `/api/motivos-movimiento/{id}` | Autenticado | Desactivar motivo |

### POST `/api/motivos-movimiento`
**Body:**
```json
{
  "empresaId": 1,
  "nombre": "string",
  "descripcion": "string",
  "tipoMovimiento": "ENTRADA | SALIDA"
}
```
**Response:**
```json
{
  "id": 1,
  "nombre": "string",
  "descripcion": "string",
  "tipoMovimiento": "ENTRADA"
}
```

### PUT `/api/motivos-movimiento/{id}`
**Body:**
```json
{
  "nombre": "string",
  "descripcion": "string",
  "tipoMovimiento": "ENTRADA | SALIDA"
}
```

---

## Roles

| Rol | Descripción |
|-----|-------------|
| `ROLE_SUPER_ADMIN` | Acceso total |
| `ROLE_ADMIN` | Admin de empresa |
| `ROLE_USER` | Usuario autenticado (acceso básico) |
