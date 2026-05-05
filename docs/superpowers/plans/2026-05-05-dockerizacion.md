# Dockerización Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Dockerizar la app Spring Boot + PostgreSQL con un solo docker-compose para dev y prod.

**Architecture:** Multi-stage Dockerfile (Maven build → JRE runtime). docker-compose con servicios `app` y `postgres`. Variables de entorno via `.env` (dev) y `--env-file .env.prod` (prod).

**Tech Stack:** Docker, Docker Compose, eclipse-temurin:21-jre-jammy, postgres:16-alpine, Maven 3.9

---

## File Map

| Archivo | Acción | Responsabilidad |
|---|---|---|
| `.gitignore` | Modificar | Excluir `.env` y `.env.prod` |
| `src/main/resources/application.properties` | Modificar | Leer config desde variables de entorno |
| `.env` | Crear | Variables dev (git-ignored) |
| `.env.example` | Crear | Plantilla commiteada sin valores reales |
| `Dockerfile` | Crear | Build multi-stage Maven → JRE |
| `docker-compose.yml` | Crear | Orquestación app + postgres |

---

### Task 1: Actualizar .gitignore

**Files:**
- Modify: `.gitignore`

- [ ] **Step 1: Verificar contenido actual de .gitignore**

```bash
cat /home/uhernand/hormigas/.gitignore
```

- [ ] **Step 2: Agregar entradas para archivos de entorno**

Agregar al final de `.gitignore`:
```
# Environment files
.env
.env.prod
```

- [ ] **Step 3: Verificar que git ignora .env**

```bash
cd /home/uhernand/hormigas && echo "TEST=1" > .env && git status
```

Expected: `.env` NO aparece en "Untracked files".

- [ ] **Step 4: Limpiar archivo de prueba**

```bash
rm /home/uhernand/hormigas/.env
```

- [ ] **Step 5: Commit**

```bash
git add .gitignore
git commit -m "chore: ignore .env files from git tracking"
```

---

### Task 2: Externalizar configuración en application.properties

**Files:**
- Modify: `src/main/resources/application.properties`

- [ ] **Step 1: Reemplazar valores hardcoded por variables de entorno**

Reemplazar el contenido completo de `src/main/resources/application.properties` con:

```properties
spring.application.name=hormigas

spring.datasource.url=jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:inventarios}
spring.datasource.username=${DB_USER:postgres}
spring.datasource.password=${DB_PASSWORD}

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect

jwt.secret=${JWT_SECRET}
jwt.expiration=${JWT_EXPIRATION:86400000}

# Superadmin inicial
app.bootstrap.admin.email=${ADMIN_EMAIL}
app.bootstrap.admin.password=${ADMIN_PASSWORD}
app.bootstrap.admin.nombre=${ADMIN_NOMBRE:Super Admin}
```

Nota: El formato `${VAR:default}` permite que la app siga funcionando sin Docker usando los defaults, excepto `DB_PASSWORD`, `JWT_SECRET`, `ADMIN_EMAIL`, `ADMIN_PASSWORD` que no tienen default por seguridad.

- [ ] **Step 2: Commit**

```bash
git add src/main/resources/application.properties
git commit -m "chore: externalize config to environment variables"
```

---

### Task 3: Crear .env.example y .env

**Files:**
- Create: `.env.example`
- Create: `.env` (no se commitea)

- [ ] **Step 1: Crear .env.example**

Crear archivo `.env.example` con:

```env
# Base de datos (compartido entre app y postgres)
DB_HOST=postgres
DB_PORT=5432
DB_NAME=inventarios
DB_USER=postgres
DB_PASSWORD=changeme

# Requeridos por la imagen de postgres
POSTGRES_DB=inventarios
POSTGRES_USER=postgres
POSTGRES_PASSWORD=changeme

# JWT
JWT_SECRET=cambia_esto_por_una_clave_larga_y_segura_minimo_32_chars
JWT_EXPIRATION=86400000

# Superadmin inicial
ADMIN_EMAIL=admin@example.com
ADMIN_PASSWORD=changeme
ADMIN_NOMBRE=Super Admin
```

- [ ] **Step 2: Crear .env con valores dev**

Crear archivo `.env` con:

```env
# Base de datos (compartido entre app y postgres)
DB_HOST=postgres
DB_PORT=5432
DB_NAME=inventarios
DB_USER=postgres
DB_PASSWORD=Circulito1.

# Requeridos por la imagen de postgres
POSTGRES_DB=inventarios
POSTGRES_USER=postgres
POSTGRES_PASSWORD=Circulito1.

# JWT
JWT_SECRET=unaClaveSecretaMuyLargaYSeguraParaFirmarTokens1234567890
JWT_EXPIRATION=86400000

# Superadmin inicial
ADMIN_EMAIL=urieledgar878@gmail.com
ADMIN_PASSWORD=Circulito1.
ADMIN_NOMBRE=Super Admin
```

- [ ] **Step 3: Verificar que .env está ignorado y .env.example no**

```bash
cd /home/uhernand/hormigas && git status
```

Expected: `.env` no aparece. `.env.example` aparece como "Untracked files".

- [ ] **Step 4: Commit .env.example**

```bash
git add .env.example
git commit -m "chore: add env template for Docker setup"
```

---

### Task 4: Crear Dockerfile multi-stage

**Files:**
- Create: `Dockerfile`

- [ ] **Step 1: Crear Dockerfile**

Crear `Dockerfile` en la raíz del proyecto:

```dockerfile
# Stage 1: Build
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -q
COPY src ./src
RUN mvn package -DskipTests -q

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-jammy AS runner
WORKDIR /app
COPY --from=builder /app/target/hormigas-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

Notas:
- `dependency:go-offline` primero: Docker cachea esta capa. Si solo cambia src, no re-descarga deps.
- `-q` en mvn: silencia output verboso en logs de build.
- `eclipse-temurin:21-jre-jammy`: JRE (no JDK) — imagen más liviana para runtime.

- [ ] **Step 2: Verificar sintaxis del Dockerfile**

```bash
docker build --no-cache -t hormigas-test /home/uhernand/hormigas 2>&1 | tail -20
```

Expected: termina con `Successfully built <id>` o similar. Si falla, revisar el error.

- [ ] **Step 3: Limpiar imagen de prueba**

```bash
docker rmi hormigas-test 2>/dev/null || true
```

- [ ] **Step 4: Commit**

```bash
git add Dockerfile
git commit -m "feat: add multi-stage Dockerfile for Spring Boot app"
```

---

### Task 5: Crear docker-compose.yml

**Files:**
- Create: `docker-compose.yml`

- [ ] **Step 1: Crear docker-compose.yml**

Crear `docker-compose.yml` en la raíz:

```yaml
services:
  postgres:
    image: postgres:16-alpine
    environment:
      POSTGRES_DB: ${POSTGRES_DB}
      POSTGRES_USER: ${POSTGRES_USER}
      POSTGRES_PASSWORD: ${POSTGRES_PASSWORD}
    volumes:
      - postgres_data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U ${POSTGRES_USER} -d ${POSTGRES_DB}"]
      interval: 10s
      timeout: 5s
      retries: 5
    restart: unless-stopped

  app:
    build:
      context: .
      dockerfile: Dockerfile
    ports:
      - "8080:8080"
    environment:
      DB_HOST: ${DB_HOST}
      DB_PORT: ${DB_PORT}
      DB_NAME: ${DB_NAME}
      DB_USER: ${DB_USER}
      DB_PASSWORD: ${DB_PASSWORD}
      JWT_SECRET: ${JWT_SECRET}
      JWT_EXPIRATION: ${JWT_EXPIRATION}
      ADMIN_EMAIL: ${ADMIN_EMAIL}
      ADMIN_PASSWORD: ${ADMIN_PASSWORD}
      ADMIN_NOMBRE: ${ADMIN_NOMBRE}
    depends_on:
      postgres:
        condition: service_healthy
    restart: unless-stopped

volumes:
  postgres_data:
```

- [ ] **Step 2: Validar sintaxis del Compose**

```bash
cd /home/uhernand/hormigas && docker compose --env-file .env config
```

Expected: imprime la config interpolada sin errores. Verifica que los valores de `.env` aparecen correctamente.

- [ ] **Step 3: Commit**

```bash
git add docker-compose.yml
git commit -m "feat: add docker-compose with app and postgres services"
```

---

### Task 6: Smoke test del stack completo

**Files:** ninguno — solo verificación

- [ ] **Step 1: Levantar el stack**

```bash
cd /home/uhernand/hormigas && docker compose up --build -d
```

Expected: build exitoso, ambos contenedores en estado `running`.

- [ ] **Step 2: Verificar que postgres está healthy**

```bash
docker compose ps
```

Expected: `postgres` muestra `healthy`, `app` muestra `running`.

- [ ] **Step 3: Seguir logs de la app hasta arranque**

```bash
docker compose logs -f app 2>&1 | grep -m1 "Started HormigasApplication"
```

Expected: línea como `Started HormigasApplication in X.XXX seconds`.

- [ ] **Step 4: Verificar endpoint de autenticación**

```bash
curl -s -o /dev/null -w "%{http_code}" -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"urieledgar878@gmail.com","password":"Circulito1."}'
```

Expected: `200` (o `400`/`401` — cualquier respuesta HTTP confirma que la app está viva y conectada a Postgres).

- [ ] **Step 5: Bajar el stack**

```bash
docker compose down
```

- [ ] **Step 6: Commit doc de spec (si no fue commiteado antes)**

```bash
cd /home/uhernand/hormigas && git add docs/ && git commit -m "docs: add dockerization spec and implementation plan" 2>/dev/null || echo "nothing to commit"
```

---

## Uso en Producción

```bash
# Crear .env.prod con credenciales reales (nunca commitear)
cp .env.example .env.prod
# editar .env.prod con valores de producción

# Levantar
docker compose --env-file .env.prod up --build -d

# Ver logs
docker compose logs -f app

# Bajar sin borrar datos
docker compose down

# Bajar borrando datos (¡destructivo!)
docker compose down -v
```
