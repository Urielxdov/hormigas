# 🐜 Hormigas — Backend API

> API REST del sistema de inventarios Hormigas. Gestiona el modelo multiempresa, autenticación JWT y protección de rutas por roles.

---

## 📋 Descripción general

El backend de **Hormigas** es una API REST construida con **Spring Boot** que actúa como capa de persistencia en la nube para las aplicaciones cliente. Implementa una **arquitectura basada en dominio**, gestiona el modelo multiempresa (empresas → sucursales → inventarios → productos) y provee seguridad mediante **JWT + roles**.

La creación de empresas no es libre: un **superusuario** del sistema crea la empresa y automáticamente genera un **usuario administrador** para ella. Desde ese punto, el admin gestiona su propia empresa de forma autónoma.

---

## 🏗️ Arquitectura

El proyecto sigue una arquitectura orientada al dominio, con separación en capas internas por paquete:

```
┌─────────────────────────────────────┐
│           Cliente (React Native)    │
└────────────────┬────────────────────┘
                 │ HTTP + JWT
┌────────────────▼────────────────────┐
│         Controllers (REST)          │  ← entrada de peticiones
├─────────────────────────────────────┤
│         Services (lógica)           │  ← reglas de negocio
├─────────────────────────────────────┤
│      Repositories (acceso datos)    │  ← Spring Data JPA
├─────────────────────────────────────┤
│         Entities / DTOs             │  ← modelo de datos
├─────────────────────────────────────┤
│         PostgreSQL                  │  ← base de datos
└─────────────────────────────────────┘

Infrastructure (transversal):
  ├── Seguridad JWT (filtros, configuración Spring Security)
  └── Comandos de arranque (data seeders)
```

### Modelo de negocio

```
Superusuario
  └── crea Empresa + Usuario Admin
        └── Sucursal (N por empresa)
              ├── Inventario (ligado a la sucursal)
              └── Producto (N por sucursal)
```

---

## 📁 Estructura del proyecto

```
hormigas-backend/
├── src/main/java/com/hormigas/
│   ├── {dominio}/                  # Un paquete por cada dominio
│   │   ├── controller/             # Endpoints REST
│   │   ├── service/                # Lógica de negocio
│   │   ├── repository/             # Interfaces JPA
│   │   ├── entity/                 # Entidades JPA
│   │   └── dto/                    # Objetos de transferencia
│   │
│   └── infrastructure/             # Capa transversal
│       ├── security/               # JWT, filtros, Spring Security
│       ├── seeders/                # Comandos al arrancar el sistema
│       └── user/                   # ⚠️ Ver deuda técnica
│
├── src/main/resources/
│   └── application.properties      # Configuración general
└── pom.xml
```

---

## 🛠️ Stack tecnológico

| Capa | Tecnología |
|---|---|
| Lenguaje | Java 21 |
| Framework | Spring Boot |
| Seguridad | Spring Security + JWT |
| Persistencia | Spring Data JPA |
| Base de datos | PostgreSQL |
| Gestor de dependencias | Maven |

---

## 🔐 Seguridad

La API usa **JWT (JSON Web Token)** para autenticación. Las rutas están protegidas mediante **roles hardcodeados** en la configuración de Spring Security.

| Rol | Descripción |
|---|---|
| `SUPER_ADMIN` | Crea empresas y sus usuarios administradores |
| `ADMIN` | Administra su propia empresa y sucursales |
| `USUARIO` | Acceso operativo limitado *(en definición)* |

> ⚠️ El esquema de roles actual es funcional pero está hardcodeado. Ver sección de deuda técnica.

---

## 📡 Endpoints

### Autenticación

| Método | Ruta | Descripción | Estado |
|---|---|---|---|
| `POST` | `/auth/login` | Obtener token JWT | ✅ |
| `POST` | `/auth/register` | Registro interno *(restringido)* | ✅ |

### Empresas — flujo controlado por Superusuario

| Método | Ruta | Descripción | Estado |
|---|---|---|---|
| `POST` | `/empresas` | Crea empresa + usuario admin automáticamente | ✅ |
| `GET` | `/empresas` | Lista empresas | ✅ |
| `GET` | `/empresas/{id}` | Detalle de empresa | ✅ |
| `PUT` | `/empresas/{id}` | Actualizar empresa | ✅ |
| `DELETE` | `/empresas/{id}` | Eliminar empresa | ✅ |

### Sucursales, Inventarios, Productos, Usuarios — CRUD básico

| Método | Ruta | Descripción | Estado |
|---|---|---|---|
| `GET` | `/{recurso}` | Listar | 🔄 En proceso |
| `GET` | `/{recurso}/{id}` | Detalle | 🔄 En proceso |
| `POST` | `/{recurso}` | Crear | 🔄 En proceso |
| `PUT` | `/{recurso}/{id}` | Actualizar | 🔄 En proceso |
| `DELETE` | `/{recurso}/{id}` | Eliminar | 🔄 En proceso |

> Recursos disponibles: `sucursales`, `inventarios`, `productos`, `usuarios`

---

## ⚠️ Deuda técnica

Esta sección documenta de forma honesta las áreas del proyecto con problemas de diseño conocidos y su proyección de mejora.

### 1. Entidad `Usuario` acoplada en `infrastructure`

**Problema:** Por desconocimiento al inicio del proyecto, la entidad `Usuario` y su lógica asociada quedaron dentro del paquete `infrastructure` en lugar de tener su propio dominio.

**Impacto:** Viola el principio de separación por dominio. `infrastructure` debería ser exclusivamente transversal (seguridad, seeders, configuración).

**Solución planeada:** Extraer `Usuario` a su propio paquete de dominio `usuario/` con su `controller`, `service`, `repository`, `entity` y `dto` correspondientes, dejando en `infrastructure` únicamente los filtros JWT y la configuración de Spring Security.

---

### 2. Roles hardcodeados

**Problema:** Los roles del sistema (`SUPERUSUARIO`, `ADMIN`, `USUARIO`) están definidos directamente en la configuración de Spring Security, no en base de datos.

**Impacto:** Poca flexibilidad para agregar o modificar roles sin tocar código.

**Solución planeada:** Migrar a un esquema de roles y permisos dinámico, almacenado en base de datos y gestionable desde la API.

---

## 🗺️ Roadmap

### ✅ Implementado
- [x] Autenticación con JWT
- [x] Protección de rutas por roles
- [x] Modelo multiempresa (Empresa → Sucursal → Inventario → Producto)
- [x] Creación de empresa con usuario admin automático vía superusuario
- [x] CRUD completo de Empresas
- [x] Seeders de arranque (datos iniciales)

### 🔄 En proceso
- [ ] CRUD de Sucursales
- [ ] CRUD de Inventarios
- [ ] CRUD de Productos
- [ ] CRUD de Usuarios

### 🔮 Mejoras futuras
- [ ] Extraer `Usuario` de `infrastructure` a su propio dominio
- [ ] Sistema de roles dinámico (base de datos)
- [ ] Endpoints para sincronización batch (soporte offline de los clientes)
- [ ] Paginación y filtros en listados
- [ ] Documentación con Swagger / OpenAPI
- [ ] Tests unitarios e integración

---

## ⚙️ Instalación y configuración

**Requisitos previos:** Java 21, Maven, PostgreSQL

```bash
# Clonar el repositorio
git clone https://github.com/tu-usuario/hormigas-backend.git
cd hormigas-backend
```

Configurar las variables en `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/hormigas
spring.datasource.username=tu_usuario
spring.datasource.password=tu_password

spring.jpa.hibernate.ddl-auto=update

jwt.secret=tu_clave_secreta
jwt.expiration=86400000
```

```bash
# Compilar y ejecutar
mvn spring-boot:run
```

Al arrancar, los seeders de `infrastructure` crean automáticamente el **superusuario** inicial si no existe.

---

*Proyecto en desarrollo activo. 🐜*
