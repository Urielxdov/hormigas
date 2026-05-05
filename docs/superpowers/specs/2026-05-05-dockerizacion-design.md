# Dockerización — Hormigas

**Fecha:** 2026-05-05  
**Estado:** Aprobado

## Contexto

Spring Boot 3.2.5, Java 21, PostgreSQL. App monolítica con módulos: empresa, inventario, motivo, movimiento, producto, security, sucursal. Target: dev local + producción.

## Enfoque

Un solo `docker-compose.yml` con variables de entorno por archivo `.env`. Sin perfiles separados por ambiente — la diferencia entre dev y prod es solo el `.env` que se use.

## Archivos

```
hormigas/
├── Dockerfile
├── docker-compose.yml
├── .env                  # dev — git-ignored
├── .env.example          # plantilla — commiteada sin valores reales
└── src/main/resources/
    └── application.properties   # modificado para leer vars de entorno
```

## Dockerfile

Multi-stage build:

- **Stage 1 `builder`:** `maven:3.9-eclipse-temurin-21`  
  Corre `mvn package -DskipTests`. Produce `target/hormigas-0.0.1-SNAPSHOT.jar`.

- **Stage 2 `runner`:** `eclipse-temurin:21-jre-jammy`  
  Copia el JAR desde el stage anterior. Expone puerto `8080`. Corre con `java -jar`.

## docker-compose.yml

Dos servicios:

**postgres**
- Imagen: `postgres:16-alpine`
- Volumen persistente: `postgres_data`
- Healthcheck: `pg_isready`
- Variables: `POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD` desde `.env`

**app**
- Build: desde `Dockerfile` en raíz del proyecto
- `depends_on` postgres con condición `service_healthy`
- Variables de entorno: todas desde `.env`
- Puerto: `8080:8080`

## Variables de entorno

| Variable | Uso |
|---|---|
| `DB_HOST` | Host de postgres (valor: `postgres` en Docker) |
| `DB_PORT` | Puerto postgres (valor: `5432`) |
| `DB_NAME` | Nombre de la base de datos |
| `DB_USER` | Usuario postgres |
| `DB_PASSWORD` | Contraseña postgres |
| `JWT_SECRET` | Clave de firma JWT |
| `JWT_EXPIRATION` | Expiración del token en ms |
| `ADMIN_EMAIL` | Email del superadmin inicial |
| `ADMIN_PASSWORD` | Contraseña del superadmin inicial |
| `ADMIN_NOMBRE` | Nombre del superadmin inicial |
| `POSTGRES_DB` | Requerido por imagen postgres (= `DB_NAME`) |
| `POSTGRES_USER` | Requerido por imagen postgres (= `DB_USER`) |
| `POSTGRES_PASSWORD` | Requerido por imagen postgres (= `DB_PASSWORD`) |

## application.properties

Reemplazar valores hardcoded por referencias a variables de entorno:

```properties
spring.datasource.url=jdbc:postgresql://${DB_HOST:postgres}:${DB_PORT:5432}/${DB_NAME}
spring.datasource.username=${DB_USER}
spring.datasource.password=${DB_PASSWORD}
jwt.secret=${JWT_SECRET}
jwt.expiration=${JWT_EXPIRATION:86400000}
app.bootstrap.admin.email=${ADMIN_EMAIL}
app.bootstrap.admin.password=${ADMIN_PASSWORD}
app.bootstrap.admin.nombre=${ADMIN_NOMBRE}
```

## .gitignore

Agregar `.env` y `.env.prod` para evitar commitear credenciales reales.

## Flujo de uso

**Dev:**
```bash
cp .env.example .env   # editar con valores locales
docker compose up --build
```

**Prod:**
```bash
# Crear .env.prod con credenciales reales
docker compose --env-file .env.prod up --build -d
```
