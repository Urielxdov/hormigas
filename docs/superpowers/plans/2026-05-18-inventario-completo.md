# Inventario Completo — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Corregir 5 bugs críticos y agregar reglas de negocio, traslados atómicos, alertas de stock y reportes al API de inventarios.

**Architecture:** Fix-first incremental sobre la arquitectura existente (Controller → Service → Repository + DTOs/Mappers/Specifications por módulo). Nuevos módulos `traslado` y `reporte` siguen el mismo patrón.

**Tech Stack:** Spring Boot 3.2.5, Java 21, JPA/Hibernate, PostgreSQL, Spring Security (JWT), spring-boot-starter-validation (a agregar).

---

## Mapa de archivos

| Archivo | Acción |
|---------|--------|
| `pom.xml` | Modificar — agregar `spring-boot-starter-validation` |
| `inventario/dto/InventarioFiltroDTO.java` | Modificar — fix typo `prodcutoId` → `productoId` |
| `inventario/repository/InventarioSpecification.java` | Modificar — fix referencia a `productoId` |
| `movimiento/entity/Movimiento.java` | Modificar — fix `@Index`, descomentar `motivo` |
| `movimiento/dto/CrearMovimientoDTO.java` | Modificar — agregar `@Positive`, `motivoId` |
| `movimiento/dto/MovimientoFiltroDTO.java` | Modificar — agregar `fechaInicio`, `fechaFin` |
| `movimiento/dto/MovimientoResponseDTO.java` | Modificar — agregar `stockAnterior`, `stockNuevo`, `alerta` |
| `movimiento/mapper/MovimientoMapper.java` | Modificar — mapear nuevos campos |
| `movimiento/entity/TipoMovimiento.java` | Modificar — agregar `DEVOLUCION_CLIENTE`, `DEVOLUCION_PROVEEDOR` |
| `movimiento/repository/MovimientoRepository.java` | Modificar — agregar query productos-top |
| `movimiento/repository/MovimientoSpecification.java` | Modificar — agregar filtro de fechas |
| `movimiento/service/MovimientoService.java` | Modificar — fix bugs + validaciones + alerta |
| `movimiento/controller/MovimientoController.java` | Modificar — agregar params de fecha |
| `inventario/service/InventarioService.java` | Modificar — fix `ultimaActualizacion`, proteger `agregarASucursal` |
| `inventario/dto/AlertaStock.java` | Crear |
| `inventario/service/AlertaStockService.java` | Crear |
| `traslado/dto/CrearTrasladoDTO.java` | Crear |
| `traslado/dto/TrasladoResponseDTO.java` | Crear |
| `traslado/service/TrasladoService.java` | Crear |
| `traslado/controller/TrasladoController.java` | Crear |
| `reporte/dto/ProductoTopDTO.java` | Crear |
| `reporte/dto/ValorInventarioDTO.java` | Crear |
| `reporte/dto/DetalleValorDTO.java` | Crear |
| `reporte/service/ReporteService.java` | Crear |
| `reporte/controller/ReporteController.java` | Crear |

Todos los paths son relativos a `src/main/java/com/example/hormigas/`.

---

## Task 1: Agregar dependencia de validación

**Files:**
- Modify: `pom.xml`

- [ ] **Step 1: Agregar dependencia en pom.xml**

Buscar el bloque `<dependencies>` y agregar:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```

- [ ] **Step 2: Verificar compilación**

```bash
./mvnw compile -q
```
Expected: `BUILD SUCCESS`

- [ ] **Step 3: Commit**

```bash
git add pom.xml
git commit -m "build: add spring-boot-starter-validation dependency"
```

---

## Task 2: Fix typo InventarioFiltroDTO

**Files:**
- Modify: `src/main/java/com/example/hormigas/inventario/dto/InventarioFiltroDTO.java`
- Modify: `src/main/java/com/example/hormigas/inventario/repository/InventarioSpecification.java`

- [ ] **Step 1: Corregir InventarioFiltroDTO**

Reemplazar contenido completo:

```java
package com.example.hormigas.inventario.dto;

public record InventarioFiltroDTO(
        Long sucursalId,
        Long productoId
) {}
```

- [ ] **Step 2: Corregir referencia en InventarioSpecification**

Localizar la línea `if (filter.prodcutoId() != null)` y reemplazar ese bloque:

```java
if (filter.productoId() != null) {
    predicates = criteriaBuilder.and(predicates,
            criteriaBuilder.equal(
                    root.get("producto")
                            .get("id"),
                    filter.productoId()
            )
    );
}
```

- [ ] **Step 3: Verificar compilación**

```bash
./mvnw compile -q
```
Expected: `BUILD SUCCESS`

- [ ] **Step 4: Commit**

```bash
git add src/main/java/com/example/hormigas/inventario/dto/InventarioFiltroDTO.java \
        src/main/java/com/example/hormigas/inventario/repository/InventarioSpecification.java
git commit -m "fix: correct typo prodcutoId -> productoId in InventarioFiltroDTO"
```

---

## Task 3: Fix @Index inválidos en Movimiento

**Files:**
- Modify: `src/main/java/com/example/hormigas/movimiento/entity/Movimiento.java`

- [ ] **Step 1: Corregir anotación @Table**

Reemplazar el bloque `@Table(...)` actual (líneas 13-20) por:

```java
@Table(
        name = "movimiento",
        indexes = {
                @Index(name = "idx_mov_fecha", columnList = "fecha")
        }
)
```

Los índices `idx_mov_producto` e `idx_mov_sucursal` referenciaban columnas que no existen directamente en `movimiento` — solo existe `inventario_id`.

- [ ] **Step 2: Verificar compilación**

```bash
./mvnw compile -q
```
Expected: `BUILD SUCCESS`

- [ ] **Step 3: Commit**

```bash
git add src/main/java/com/example/hormigas/movimiento/entity/Movimiento.java
git commit -m "fix: remove invalid @Index on non-existent columns in Movimiento entity"
```

---

## Task 4: Reconectar MotivoMovimiento a Movimiento

**Files:**
- Modify: `src/main/java/com/example/hormigas/movimiento/entity/Movimiento.java`
- Modify: `src/main/java/com/example/hormigas/movimiento/dto/CrearMovimientoDTO.java`
- Modify: `src/main/java/com/example/hormigas/movimiento/service/MovimientoService.java`

- [ ] **Step 1: Descomentar campo motivo en Movimiento**

Reemplazar las líneas comentadas (41-46) por:

```java
@ManyToOne
@JoinColumn(
        name = "motivo_id",
        foreignKey = @ForeignKey(name = "fk_movimiento_motivo")
)
private MotivoMovimiento motivo;
```

Agregar getter y setter al final de la clase:

```java
public MotivoMovimiento getMotivo() {
    return motivo;
}

public void setMotivo(MotivoMovimiento motivo) {
    this.motivo = motivo;
}
```

- [ ] **Step 2: Agregar motivoId a CrearMovimientoDTO**

```java
package com.example.hormigas.movimiento.dto;

import com.example.hormigas.movimiento.entity.TipoMovimiento;
import jakarta.validation.constraints.Positive;

public record CrearMovimientoDTO(
        Long sucursalId,
        Long productoId,
        TipoMovimiento tipoMovimiento,
        @Positive int cantidad,
        String referencia,
        Long motivoId
) {}
```

- [ ] **Step 3: Agregar MotivoMovimientoRepository a MovimientoService e inyectarlo**

En `MovimientoService`, agregar campo:

```java
private final MotivoMovimientoRepository motivoRepository;
```

Actualizar constructor para recibirlo:

```java
public MovimientoService(
        InventarioRepository inventarioRepository,
        MovimientoRepository movimientoRepository,
        UsuarioService usuarioService,
        SucursalRepository sucursalRepository,
        ProductoRepository productoRepository,
        MotivoMovimientoRepository motivoRepository
) {
    this.inventarioRepository = inventarioRepository;
    this.movimientoRepository = movimientoRepository;
    this.usuarioService = usuarioService;
    this.sucursalRepository = sucursalRepository;
    this.productoRepository = productoRepository;
    this.motivoRepository = motivoRepository;
}
```

- [ ] **Step 4: Asignar motivo en registrarMovimiento**

Después de `movimiento.setUsuario(user)` y antes de `movimientoRepository.save(movimiento)`, agregar:

```java
if (dto.motivoId() != null) {
    MotivoMovimiento motivo = motivoRepository.findById(dto.motivoId())
            .orElseThrow(() -> new EntityNotFoundException("Motivo no encontrado: " + dto.motivoId()));
    movimiento.setMotivo(motivo);
}
```

- [ ] **Step 5: Compilar**

```bash
./mvnw compile -q
```
Expected: `BUILD SUCCESS`

- [ ] **Step 6: Commit**

```bash
git add src/main/java/com/example/hormigas/movimiento/entity/Movimiento.java \
        src/main/java/com/example/hormigas/movimiento/dto/CrearMovimientoDTO.java \
        src/main/java/com/example/hormigas/movimiento/service/MovimientoService.java
git commit -m "fix: reconnect MotivoMovimiento to Movimiento entity and service"
```

---

## Task 5: Fix setInventario + setUltimaActualizacion en MovimientoService

**Files:**
- Modify: `src/main/java/com/example/hormigas/movimiento/service/MovimientoService.java`

- [ ] **Step 1: Agregar setInventario y setUltimaActualizacion**

En `registrarMovimiento`, reemplazar el bloque de creación del movimiento (desde `Movimiento movimiento = new Movimiento()` hasta el `save`) por:

```java
inventario.setUltimaActualizacion(LocalDateTime.now());
inventarioRepository.save(inventario);

Movimiento movimiento = new Movimiento();
movimiento.setInventario(inventario);
movimiento.setTipoMovimiento(tipo);
movimiento.setCantidad(dto.cantidad());
movimiento.setStockAnterior(stockActual);
movimiento.setStockNuevo(nuevoStock);
movimiento.setUsuario(user);
movimiento.setFecha(LocalDateTime.now());
movimiento.setReferencia(dto.referencia());

if (dto.motivoId() != null) {
    MotivoMovimiento motivo = motivoRepository.findById(dto.motivoId())
            .orElseThrow(() -> new EntityNotFoundException("Motivo no encontrado: " + dto.motivoId()));
    movimiento.setMotivo(motivo);
}

movimientoRepository.save(movimiento);
```

Nota: `movimiento.setFecha(LocalDateTime.now())` también corrige que `fecha` nunca se seteaba.

- [ ] **Step 2: Compilar**

```bash
./mvnw compile -q
```
Expected: `BUILD SUCCESS`

- [ ] **Step 3: Commit**

```bash
git add src/main/java/com/example/hormigas/movimiento/service/MovimientoService.java
git commit -m "fix: set inventario, fecha and ultimaActualizacion when registering movement"
```

---

## Task 6: Fix InventarioService — ultimaActualizacion y proteger agregarASucursal

**Files:**
- Modify: `src/main/java/com/example/hormigas/inventario/service/InventarioService.java`

- [ ] **Step 1: Setear ultimaActualizacion al crear inventario**

En el método `crear`, antes de `inventarioRepository.save(inventario)`, agregar:

```java
inventario.setUltimaActualizacion(LocalDateTime.now());
```

Agregar import si no existe: `import java.time.LocalDateTime;`

- [ ] **Step 2: Proteger agregarASucursal contra violación de unique constraint**

Reemplazar el método `agregarASucursal` completo por:

```java
@Transactional
public void agregarASucursal(Long sucursalId, Long inventarioId) {
    Usuario user = usuarioService.getUsuarioLogueado();

    Inventario inventario = inventarioRepository.findById(inventarioId)
            .orElseThrow(() -> new EntityNotFoundException("Inventario no encontrado"));

    if (!inventario.getSucursal().getEmpresa().getId().equals(user.getEmpresa().getId())) {
        throw new IllegalArgumentException("Inventario o sucursal invalidos o no pertenecen a la empresa");
    }

    boolean yaExiste = inventarioRepository
            .findBySucursalIdAndProductoId(sucursalId, inventario.getProducto().getId())
            .isPresent();

    if (yaExiste) {
        throw new IllegalArgumentException(
                "Ya existe un inventario para ese producto en la sucursal destino. Use TRASLADO para mover stock."
        );
    }

    int filas = inventarioRepository.asignarASucursal(
            inventarioId,
            sucursalId,
            user.getEmpresa().getId()
    );

    if (filas == 0) {
        throw new IllegalArgumentException("Inventario o sucursal invalidos o no pertenecen a la empresa");
    }
    if (filas > 1) {
        throw new IllegalStateException("Error critico: multiples inventarios afectados");
    }
}
```

- [ ] **Step 3: Compilar**

```bash
./mvnw compile -q
```
Expected: `BUILD SUCCESS`

- [ ] **Step 4: Commit**

```bash
git add src/main/java/com/example/hormigas/inventario/service/InventarioService.java
git commit -m "fix: set ultimaActualizacion on inventory create and guard agregarASucursal against constraint violation"
```

---

## Task 7: Agregar DEVOLUCION_CLIENTE y DEVOLUCION_PROVEEDOR al enum

**Files:**
- Modify: `src/main/java/com/example/hormigas/movimiento/entity/TipoMovimiento.java`

- [ ] **Step 1: Agregar nuevos valores al enum**

Reemplazar el contenido completo del archivo:

```java
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
```

El método `esEntrada()` se usará en Fase 2 y en los reportes.

- [ ] **Step 2: Compilar**

```bash
./mvnw compile -q
```
Expected: `BUILD SUCCESS`

- [ ] **Step 3: Commit**

```bash
git add src/main/java/com/example/hormigas/movimiento/entity/TipoMovimiento.java
git commit -m "feat: add DEVOLUCION_CLIENTE and DEVOLUCION_PROVEEDOR movement types"
```

---

## Task 8: Validación de cantidad y stockMaximo

**Files:**
- Modify: `src/main/java/com/example/hormigas/movimiento/service/MovimientoService.java`
- Modify: `src/main/java/com/example/hormigas/movimiento/controller/MovimientoController.java`

- [ ] **Step 1: Agregar validación de stockMaximo en registrarMovimiento**

En `MovimientoService.registrarMovimiento`, después de la línea `if (nuevoStock < 0)`, agregar:

```java
if (tipo.esEntrada() && nuevoStock > inventario.getStockMaximo()) {
    throw new IllegalArgumentException(
            "Stock resultante (" + nuevoStock + ") excede el máximo permitido (" + inventario.getStockMaximo() + ")"
    );
}
```

- [ ] **Step 2: Habilitar @Validated en el controller**

En `MovimientoController`, agregar `@Validated` a la clase y `@Valid` al parámetro del POST:

```java
import org.springframework.validation.annotation.Validated;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/movimiento")
@Validated
public class MovimientoController {
    // ...

    @PostMapping("/crear")
    public MovimientoResponseDTO createMovimiento(@Valid @RequestBody CrearMovimientoDTO dto) {
        return movimientoService.registrarMovimiento(dto);
    }
```

- [ ] **Step 3: Compilar**

```bash
./mvnw compile -q
```
Expected: `BUILD SUCCESS`

- [ ] **Step 4: Commit**

```bash
git add src/main/java/com/example/hormigas/movimiento/service/MovimientoService.java \
        src/main/java/com/example/hormigas/movimiento/controller/MovimientoController.java
git commit -m "feat: validate cantidad > 0 and enforce stockMaximo on entry movements"
```

---

## Task 9: Crear AlertaStock y AlertaStockService

**Files:**
- Create: `src/main/java/com/example/hormigas/inventario/dto/AlertaStock.java`
- Create: `src/main/java/com/example/hormigas/inventario/service/AlertaStockService.java`
- Create: `src/test/java/com/example/hormigas/inventario/service/AlertaStockServiceTest.java`

- [ ] **Step 1: Escribir el test**

```java
package com.example.hormigas.inventario.service;

import com.example.hormigas.inventario.dto.AlertaStock;
import com.example.hormigas.inventario.entity.Inventario;
import com.example.hormigas.producto.entity.Producto;
import com.example.hormigas.sucursal.entity.Sucursal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AlertaStockServiceTest {

    private AlertaStockService service;

    @BeforeEach
    void setUp() {
        service = new AlertaStockService();
    }

    private Inventario inventario(int actual, Integer minimo, int maximo) {
        Inventario inv = new Inventario();
        inv.setStockActual(actual);
        inv.setStockMinimo(minimo);
        inv.setStockMaximo(maximo);
        return inv;
    }

    @Test
    void noAlerta_cuandoStockEnRangoNormal() {
        assertThat(service.evaluar(inventario(10, 5, 20))).isNull();
    }

    @Test
    void alertaCritico_cuandoStockEsCero() {
        AlertaStock alerta = service.evaluar(inventario(0, 5, 20));
        assertThat(alerta).isNotNull();
        assertThat(alerta.tipo()).isEqualTo("STOCK_CRITICO");
    }

    @Test
    void alertaBajo_cuandoStockMenorQueMinimo() {
        AlertaStock alerta = service.evaluar(inventario(3, 5, 20));
        assertThat(alerta).isNotNull();
        assertThat(alerta.tipo()).isEqualTo("STOCK_BAJO");
    }

    @Test
    void alertaExcedido_cuandoStockMayorQueMaximo() {
        AlertaStock alerta = service.evaluar(inventario(25, 5, 20));
        assertThat(alerta).isNotNull();
        assertThat(alerta.tipo()).isEqualTo("STOCK_EXCEDIDO");
    }

    @Test
    void alertaCritico_tienePrioridadSobreBajo() {
        AlertaStock alerta = service.evaluar(inventario(0, 5, 20));
        assertThat(alerta.tipo()).isEqualTo("STOCK_CRITICO");
    }

    @Test
    void noAlerta_cuandoStockMinimoEsNull() {
        assertThat(service.evaluar(inventario(3, null, 20))).isNull();
    }
}
```

- [ ] **Step 2: Correr test — debe fallar (clases no existen aún)**

```bash
./mvnw test -pl . -Dtest=AlertaStockServiceTest -q 2>&1 | tail -5
```
Expected: error de compilación o `FAILED`

- [ ] **Step 3: Crear AlertaStock record**

```java
package com.example.hormigas.inventario.dto;

public record AlertaStock(String tipo, String mensaje) {}
```

- [ ] **Step 4: Crear AlertaStockService**

```java
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
```

- [ ] **Step 5: Correr test — debe pasar**

```bash
./mvnw test -pl . -Dtest=AlertaStockServiceTest -q 2>&1 | tail -5
```
Expected: `BUILD SUCCESS`, `Tests run: 6, Failures: 0`

- [ ] **Step 6: Commit**

```bash
git add src/main/java/com/example/hormigas/inventario/dto/AlertaStock.java \
        src/main/java/com/example/hormigas/inventario/service/AlertaStockService.java \
        src/test/java/com/example/hormigas/inventario/service/AlertaStockServiceTest.java
git commit -m "feat: add AlertaStock record and AlertaStockService with unit tests"
```

---

## Task 10: Agregar alerta al MovimientoResponseDTO

**Files:**
- Modify: `src/main/java/com/example/hormigas/movimiento/dto/MovimientoResponseDTO.java`
- Modify: `src/main/java/com/example/hormigas/movimiento/mapper/MovimientoMapper.java`
- Modify: `src/main/java/com/example/hormigas/movimiento/service/MovimientoService.java`

- [ ] **Step 1: Actualizar MovimientoResponseDTO**

```java
package com.example.hormigas.movimiento.dto;

import com.example.hormigas.inventario.dto.AlertaStock;
import com.example.hormigas.movimiento.entity.TipoMovimiento;

import java.time.LocalDateTime;

public record MovimientoResponseDTO(
        Long id,
        Long productoId,
        String productoNombre,
        Long sucursalId,
        String sucursalNombre,
        TipoMovimiento tipoMovimiento,
        int cantidad,
        int stockAnterior,
        int stockNuevo,
        String usuarioNombre,
        String referencia,
        LocalDateTime fecha,
        AlertaStock alerta
) {}
```

- [ ] **Step 2: Actualizar MovimientoMapper**

```java
package com.example.hormigas.movimiento.mapper;

import com.example.hormigas.inventario.dto.AlertaStock;
import com.example.hormigas.movimiento.dto.MovimientoResponseDTO;
import com.example.hormigas.movimiento.entity.Movimiento;

public class MovimientoMapper {

    public static MovimientoResponseDTO toResponse(Movimiento movimiento, AlertaStock alerta) {
        Long productoId = movimiento.getInventario() != null && movimiento.getInventario().getProducto() != null
                ? movimiento.getInventario().getProducto().getId() : null;
        String productoNombre = movimiento.getInventario() != null && movimiento.getInventario().getProducto() != null
                ? movimiento.getInventario().getProducto().getNombre() : null;
        Long sucursalId = movimiento.getInventario() != null && movimiento.getInventario().getSucursal() != null
                ? movimiento.getInventario().getSucursal().getId() : null;
        String sucursalNombre = movimiento.getInventario() != null && movimiento.getInventario().getSucursal() != null
                ? movimiento.getInventario().getSucursal().getNombre() : null;
        String usuarioNombre = movimiento.getUsuario() != null ? movimiento.getUsuario().getNombre() : null;

        return new MovimientoResponseDTO(
                movimiento.getId(),
                productoId,
                productoNombre,
                sucursalId,
                sucursalNombre,
                movimiento.getTipoMovimiento(),
                movimiento.getCantidad(),
                movimiento.getStockAnterior(),
                movimiento.getStockNuevo(),
                usuarioNombre,
                movimiento.getReferencia(),
                movimiento.getFecha(),
                alerta
        );
    }
}
```

- [ ] **Step 3: Inyectar AlertaStockService en MovimientoService y usarlo**

Agregar campo e inyección al constructor de `MovimientoService`:

```java
private final AlertaStockService alertaStockService;

public MovimientoService(
        InventarioRepository inventarioRepository,
        MovimientoRepository movimientoRepository,
        UsuarioService usuarioService,
        SucursalRepository sucursalRepository,
        ProductoRepository productoRepository,
        MotivoMovimientoRepository motivoRepository,
        AlertaStockService alertaStockService
) {
    this.inventarioRepository = inventarioRepository;
    this.movimientoRepository = movimientoRepository;
    this.usuarioService = usuarioService;
    this.sucursalRepository = sucursalRepository;
    this.productoRepository = productoRepository;
    this.motivoRepository = motivoRepository;
    this.alertaStockService = alertaStockService;
}
```

Al final de `registrarMovimiento`, reemplazar el `return`:

```java
movimientoRepository.save(movimiento);

AlertaStock alerta = alertaStockService.evaluar(inventario);
return MovimientoMapper.toResponse(movimiento, alerta);
```

En `obtenerMovimientos`, actualizar el stream:

```java
return movimientos.stream()
        .map(m -> MovimientoMapper.toResponse(m, null))
        .toList();
```

- [ ] **Step 4: Compilar**

```bash
./mvnw compile -q
```
Expected: `BUILD SUCCESS`

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/example/hormigas/movimiento/dto/MovimientoResponseDTO.java \
        src/main/java/com/example/hormigas/movimiento/mapper/MovimientoMapper.java \
        src/main/java/com/example/hormigas/movimiento/service/MovimientoService.java
git commit -m "feat: add stock alert to movement response"
```

---

## Task 11: Traslados Atómicos

**Files:**
- Create: `src/main/java/com/example/hormigas/traslado/dto/CrearTrasladoDTO.java`
- Create: `src/main/java/com/example/hormigas/traslado/dto/TrasladoResponseDTO.java`
- Create: `src/main/java/com/example/hormigas/traslado/service/TrasladoService.java`
- Create: `src/main/java/com/example/hormigas/traslado/controller/TrasladoController.java`

- [ ] **Step 1: Crear CrearTrasladoDTO**

```java
package com.example.hormigas.traslado.dto;

import jakarta.validation.constraints.Positive;

public record CrearTrasladoDTO(
        Long sucursalOrigenId,
        Long sucursalDestinoId,
        Long productoId,
        @Positive int cantidad,
        String referencia
) {}
```

- [ ] **Step 2: Crear TrasladoResponseDTO**

```java
package com.example.hormigas.traslado.dto;

import com.example.hormigas.movimiento.dto.MovimientoResponseDTO;

public record TrasladoResponseDTO(
        MovimientoResponseDTO movimientoSalida,
        MovimientoResponseDTO movimientoEntrada,
        String referencia
) {}
```

- [ ] **Step 3: Crear TrasladoService**

```java
package com.example.hormigas.traslado.service;

import com.example.hormigas.inventario.dto.AlertaStock;
import com.example.hormigas.inventario.entity.Inventario;
import com.example.hormigas.inventario.repository.InventarioRepository;
import com.example.hormigas.inventario.service.AlertaStockService;
import com.example.hormigas.movimiento.entity.Movimiento;
import com.example.hormigas.movimiento.entity.TipoMovimiento;
import com.example.hormigas.movimiento.mapper.MovimientoMapper;
import com.example.hormigas.movimiento.repository.MovimientoRepository;
import com.example.hormigas.security.domain.Usuario;
import com.example.hormigas.security.domain.services.UsuarioService;
import com.example.hormigas.traslado.dto.CrearTrasladoDTO;
import com.example.hormigas.traslado.dto.TrasladoResponseDTO;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class TrasladoService {

    private final InventarioRepository inventarioRepository;
    private final MovimientoRepository movimientoRepository;
    private final UsuarioService usuarioService;
    private final AlertaStockService alertaStockService;

    public TrasladoService(
            InventarioRepository inventarioRepository,
            MovimientoRepository movimientoRepository,
            UsuarioService usuarioService,
            AlertaStockService alertaStockService
    ) {
        this.inventarioRepository = inventarioRepository;
        this.movimientoRepository = movimientoRepository;
        this.usuarioService = usuarioService;
        this.alertaStockService = alertaStockService;
    }

    @Transactional
    public TrasladoResponseDTO crear(CrearTrasladoDTO dto) {
        Usuario user = usuarioService.getUsuarioLogueado();

        Inventario origen = inventarioRepository
                .findBySucursalIdAndProductoId(dto.sucursalOrigenId(), dto.productoId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "No existe inventario del producto en la sucursal origen"));

        Inventario destino = inventarioRepository
                .findBySucursalIdAndProductoId(dto.sucursalDestinoId(), dto.productoId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "No existe inventario del producto en la sucursal destino"));

        if (!origen.getSucursal().getEmpresa().getId().equals(user.getEmpresa().getId()) ||
            !destino.getSucursal().getEmpresa().getId().equals(user.getEmpresa().getId())) {
            throw new IllegalArgumentException("Las sucursales no pertenecen a la empresa del usuario");
        }

        int stockOrigen = origen.getStockActual();
        int nuevoStockOrigen = TipoMovimiento.TRASLADO_SALIDA.aplicar(stockOrigen, dto.cantidad());
        if (nuevoStockOrigen < 0) {
            throw new IllegalArgumentException("Stock insuficiente en sucursal origen");
        }

        int stockDestino = destino.getStockActual();
        int nuevoStockDestino = TipoMovimiento.TRASLADO_ENTRADA.aplicar(stockDestino, dto.cantidad());
        if (nuevoStockDestino > destino.getStockMaximo()) {
            throw new IllegalArgumentException(
                    "Stock resultante en destino (" + nuevoStockDestino + ") excede el máximo (" + destino.getStockMaximo() + ")");
        }

        String referenciaTraslado = dto.referencia() != null ? dto.referencia() : UUID.randomUUID().toString();
        LocalDateTime ahora = LocalDateTime.now();

        origen.setStockActual(nuevoStockOrigen);
        origen.setUltimaActualizacion(ahora);
        inventarioRepository.save(origen);

        destino.setStockActual(nuevoStockDestino);
        destino.setUltimaActualizacion(ahora);
        inventarioRepository.save(destino);

        Movimiento salida = new Movimiento();
        salida.setInventario(origen);
        salida.setTipoMovimiento(TipoMovimiento.TRASLADO_SALIDA);
        salida.setCantidad(dto.cantidad());
        salida.setStockAnterior(stockOrigen);
        salida.setStockNuevo(nuevoStockOrigen);
        salida.setUsuario(user);
        salida.setFecha(ahora);
        salida.setReferencia(referenciaTraslado);
        movimientoRepository.save(salida);

        Movimiento entrada = new Movimiento();
        entrada.setInventario(destino);
        entrada.setTipoMovimiento(TipoMovimiento.TRASLADO_ENTRADA);
        entrada.setCantidad(dto.cantidad());
        entrada.setStockAnterior(stockDestino);
        entrada.setStockNuevo(nuevoStockDestino);
        entrada.setUsuario(user);
        entrada.setFecha(ahora);
        entrada.setReferencia(referenciaTraslado);
        movimientoRepository.save(entrada);

        AlertaStock alertaOrigen = alertaStockService.evaluar(origen);
        AlertaStock alertaDestino = alertaStockService.evaluar(destino);

        return new TrasladoResponseDTO(
                MovimientoMapper.toResponse(salida, alertaOrigen),
                MovimientoMapper.toResponse(entrada, alertaDestino),
                referenciaTraslado
        );
    }
}
```

- [ ] **Step 4: Crear TrasladoController**

```java
package com.example.hormigas.traslado.controller;

import com.example.hormigas.traslado.dto.CrearTrasladoDTO;
import com.example.hormigas.traslado.dto.TrasladoResponseDTO;
import com.example.hormigas.traslado.service.TrasladoService;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/traslado")
@Validated
public class TrasladoController {

    private final TrasladoService trasladoService;

    public TrasladoController(TrasladoService trasladoService) {
        this.trasladoService = trasladoService;
    }

    @PostMapping("/crear")
    public TrasladoResponseDTO crear(@Valid @RequestBody CrearTrasladoDTO dto) {
        return trasladoService.crear(dto);
    }
}
```

- [ ] **Step 5: Compilar**

```bash
./mvnw compile -q
```
Expected: `BUILD SUCCESS`

- [ ] **Step 6: Commit**

```bash
git add src/main/java/com/example/hormigas/traslado/
git commit -m "feat: add atomic transfer endpoint POST /api/traslado/crear"
```

---

## Task 12: Filtros de fecha en MovimientoFiltroDTO y Specification

**Files:**
- Modify: `src/main/java/com/example/hormigas/movimiento/dto/MovimientoFiltroDTO.java`
- Modify: `src/main/java/com/example/hormigas/movimiento/repository/MovimientoSpecification.java`
- Modify: `src/main/java/com/example/hormigas/movimiento/controller/MovimientoController.java`

- [ ] **Step 1: Actualizar MovimientoFiltroDTO**

```java
package com.example.hormigas.movimiento.dto;

import com.example.hormigas.movimiento.entity.TipoMovimiento;

import java.time.LocalDateTime;

public record MovimientoFiltroDTO(
        Long sucursalId,
        Long productoId,
        Long inventarioId,
        TipoMovimiento tipo,
        LocalDateTime fechaInicio,
        LocalDateTime fechaFin
) {}
```

- [ ] **Step 2: Actualizar MovimientoSpecification**

Agregar al final del bloque de predicados (antes del `return`):

```java
if (filtro.fechaInicio() != null) {
    predicates = cb.and(predicates,
            cb.greaterThanOrEqualTo(root.get("fecha"), filtro.fechaInicio())
    );
}

if (filtro.fechaFin() != null) {
    predicates = cb.and(predicates,
            cb.lessThanOrEqualTo(root.get("fecha"), filtro.fechaFin())
    );
}
```

- [ ] **Step 3: Actualizar MovimientoController.obtenerMovimientos**

```java
@GetMapping("/buscar")
public List<MovimientoResponseDTO> obtenerMovimientos(
        @RequestParam(required = false) Long sucursalId,
        @RequestParam(required = false) Long productoId,
        @RequestParam(required = false) Long inventarioId,
        @RequestParam(required = false) TipoMovimiento tipo,
        @RequestParam(required = false) LocalDateTime fechaInicio,
        @RequestParam(required = false) LocalDateTime fechaFin
) {
    MovimientoFiltroDTO filtro = new MovimientoFiltroDTO(
            sucursalId, productoId, inventarioId, tipo, fechaInicio, fechaFin);
    return movimientoService.obtenerMovimientos(filtro);
}
```

Agregar import: `import java.time.LocalDateTime;`

- [ ] **Step 4: Compilar**

```bash
./mvnw compile -q
```
Expected: `BUILD SUCCESS`

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/example/hormigas/movimiento/dto/MovimientoFiltroDTO.java \
        src/main/java/com/example/hormigas/movimiento/repository/MovimientoSpecification.java \
        src/main/java/com/example/hormigas/movimiento/controller/MovimientoController.java
git commit -m "feat: add date range filters to movement queries"
```

---

## Task 13: Agregar query productos-top en MovimientoRepository

**Files:**
- Create: `src/main/java/com/example/hormigas/reporte/dto/ProductoTopDTO.java`
- Modify: `src/main/java/com/example/hormigas/movimiento/repository/MovimientoRepository.java`

- [ ] **Step 1: Crear ProductoTopDTO**

```java
package com.example.hormigas.reporte.dto;

public record ProductoTopDTO(
        Long productoId,
        String nombre,
        String sku,
        long totalEntradas,
        long totalSalidas
) {
    public long netoCambio() {
        return totalEntradas - totalSalidas;
    }
}
```

- [ ] **Step 2: Agregar query en MovimientoRepository**

```java
package com.example.hormigas.movimiento.repository;

import com.example.hormigas.movimiento.entity.Movimiento;
import com.example.hormigas.reporte.dto.ProductoTopDTO;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface MovimientoRepository extends JpaRepository<Movimiento, Long>, JpaSpecificationExecutor<Movimiento> {

    @Query("""
        SELECT new com.example.hormigas.reporte.dto.ProductoTopDTO(
            i.producto.id,
            i.producto.nombre,
            i.producto.sku,
            SUM(CASE WHEN m.tipoMovimiento IN (
                com.example.hormigas.movimiento.entity.TipoMovimiento.COMPRA,
                com.example.hormigas.movimiento.entity.TipoMovimiento.DEVOLUCION,
                com.example.hormigas.movimiento.entity.TipoMovimiento.DEVOLUCION_CLIENTE,
                com.example.hormigas.movimiento.entity.TipoMovimiento.TRASLADO_ENTRADA
            ) THEN m.cantidad ELSE 0 END),
            SUM(CASE WHEN m.tipoMovimiento IN (
                com.example.hormigas.movimiento.entity.TipoMovimiento.VENTA,
                com.example.hormigas.movimiento.entity.TipoMovimiento.MERMA,
                com.example.hormigas.movimiento.entity.TipoMovimiento.DEVOLUCION_PROVEEDOR,
                com.example.hormigas.movimiento.entity.TipoMovimiento.TRASLADO_SALIDA
            ) THEN m.cantidad ELSE 0 END)
        )
        FROM Movimiento m JOIN m.inventario i
        WHERE i.sucursal.empresa.id = :empresaId
        AND (:sucursalId IS NULL OR i.sucursal.id = :sucursalId)
        AND (:fechaInicio IS NULL OR m.fecha >= :fechaInicio)
        AND (:fechaFin IS NULL OR m.fecha <= :fechaFin)
        GROUP BY i.producto.id, i.producto.nombre, i.producto.sku
        ORDER BY (SUM(m.cantidad)) DESC
    """)
    List<ProductoTopDTO> findProductosTop(
            @Param("empresaId") Long empresaId,
            @Param("sucursalId") Long sucursalId,
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin,
            Pageable pageable
    );
}
```

- [ ] **Step 3: Compilar**

```bash
./mvnw compile -q
```
Expected: `BUILD SUCCESS`

- [ ] **Step 4: Commit**

```bash
git add src/main/java/com/example/hormigas/reporte/dto/ProductoTopDTO.java \
        src/main/java/com/example/hormigas/movimiento/repository/MovimientoRepository.java
git commit -m "feat: add productos-top aggregation query to MovimientoRepository"
```

---

## Task 14: DTOs de valor de inventario

**Files:**
- Create: `src/main/java/com/example/hormigas/reporte/dto/DetalleValorDTO.java`
- Create: `src/main/java/com/example/hormigas/reporte/dto/ValorInventarioDTO.java`

- [ ] **Step 1: Crear DetalleValorDTO**

```java
package com.example.hormigas.reporte.dto;

import java.math.BigDecimal;

public record DetalleValorDTO(
        Long productoId,
        String nombre,
        String sku,
        int stockActual,
        BigDecimal precio,
        BigDecimal valorLinea,
        boolean sinPrecio
) {}
```

- [ ] **Step 2: Crear ValorInventarioDTO**

```java
package com.example.hormigas.reporte.dto;

import java.math.BigDecimal;
import java.util.List;

public record ValorInventarioDTO(
        Long sucursalId,
        String nombreSucursal,
        BigDecimal valorTotal,
        int productosConPrecio,
        int productosSinPrecio,
        List<DetalleValorDTO> detalle
) {}
```

- [ ] **Step 3: Commit**

```bash
git add src/main/java/com/example/hormigas/reporte/dto/DetalleValorDTO.java \
        src/main/java/com/example/hormigas/reporte/dto/ValorInventarioDTO.java
git commit -m "feat: add inventory value report DTOs"
```

---

## Task 15: ReporteService y ReporteController

**Files:**
- Create: `src/main/java/com/example/hormigas/reporte/service/ReporteService.java`
- Create: `src/main/java/com/example/hormigas/reporte/controller/ReporteController.java`

- [ ] **Step 1: Crear ReporteService**

```java
package com.example.hormigas.reporte.service;

import com.example.hormigas.inventario.entity.Inventario;
import com.example.hormigas.inventario.repository.InventarioRepository;
import com.example.hormigas.movimiento.dto.MovimientoFiltroDTO;
import com.example.hormigas.movimiento.dto.MovimientoResponseDTO;
import com.example.hormigas.movimiento.mapper.MovimientoMapper;
import com.example.hormigas.movimiento.repository.MovimientoRepository;
import com.example.hormigas.movimiento.repository.MovimientoSpecification;
import com.example.hormigas.reporte.dto.DetalleValorDTO;
import com.example.hormigas.reporte.dto.ProductoTopDTO;
import com.example.hormigas.reporte.dto.ValorInventarioDTO;
import com.example.hormigas.security.domain.Usuario;
import com.example.hormigas.security.domain.services.UsuarioService;
import com.example.hormigas.sucursal.entity.Sucursal;
import com.example.hormigas.sucursal.repository.SucursalRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReporteService {

    private final MovimientoRepository movimientoRepository;
    private final InventarioRepository inventarioRepository;
    private final SucursalRepository sucursalRepository;
    private final UsuarioService usuarioService;

    public ReporteService(
            MovimientoRepository movimientoRepository,
            InventarioRepository inventarioRepository,
            SucursalRepository sucursalRepository,
            UsuarioService usuarioService
    ) {
        this.movimientoRepository = movimientoRepository;
        this.inventarioRepository = inventarioRepository;
        this.sucursalRepository = sucursalRepository;
        this.usuarioService = usuarioService;
    }

    public List<MovimientoResponseDTO> movimientosPorPeriodo(
            LocalDateTime fechaInicio, LocalDateTime fechaFin,
            Long sucursalId, Long productoId, int page, int size
    ) {
        Usuario user = usuarioService.getUsuarioLogueado();
        MovimientoFiltroDTO filtro = new MovimientoFiltroDTO(
                sucursalId, productoId, null, null, fechaInicio, fechaFin);

        return movimientoRepository
                .findAll(MovimientoSpecification.conFiltros(user.getEmpresa().getId(), filtro),
                        PageRequest.of(page, size))
                .stream()
                .map(m -> MovimientoMapper.toResponse(m, null))
                .toList();
    }

    public List<ProductoTopDTO> productosTop(
            LocalDateTime fechaInicio, LocalDateTime fechaFin,
            Long sucursalId, int limite
    ) {
        Usuario user = usuarioService.getUsuarioLogueado();
        return movimientoRepository.findProductosTop(
                user.getEmpresa().getId(),
                sucursalId,
                fechaInicio,
                fechaFin,
                PageRequest.of(0, limite)
        );
    }

    public ValorInventarioDTO valorInventario(Long sucursalId) {
        Usuario user = usuarioService.getUsuarioLogueado();

        Sucursal sucursal = sucursalRepository.findById(sucursalId)
                .orElseThrow(() -> new EntityNotFoundException("Sucursal no encontrada"));

        if (!sucursal.getEmpresa().getId().equals(user.getEmpresa().getId())) {
            throw new IllegalArgumentException("Sucursal no pertenece a la empresa");
        }

        List<Inventario> inventarios = inventarioRepository.findBySucursalId(sucursalId);

        List<DetalleValorDTO> detalle = inventarios.stream().map(inv -> {
            BigDecimal precio = inv.getProducto().getPrecio();
            boolean sinPrecio = precio == null;
            BigDecimal precioEfectivo = sinPrecio ? BigDecimal.ZERO : precio;
            BigDecimal valorLinea = precioEfectivo.multiply(BigDecimal.valueOf(inv.getStockActual()));
            return new DetalleValorDTO(
                    inv.getProducto().getId(),
                    inv.getProducto().getNombre(),
                    inv.getProducto().getSku(),
                    inv.getStockActual(),
                    precio,
                    valorLinea,
                    sinPrecio
            );
        }).toList();

        BigDecimal valorTotal = detalle.stream()
                .map(DetalleValorDTO::valorLinea)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long conPrecio = detalle.stream().filter(d -> !d.sinPrecio()).count();
        long sinPrecio = detalle.stream().filter(DetalleValorDTO::sinPrecio).count();

        return new ValorInventarioDTO(
                sucursalId,
                sucursal.getNombre(),
                valorTotal,
                (int) conPrecio,
                (int) sinPrecio,
                detalle
        );
    }
}
```

- [ ] **Step 2: Crear ReporteController**

```java
package com.example.hormigas.reporte.controller;

import com.example.hormigas.movimiento.dto.MovimientoResponseDTO;
import com.example.hormigas.reporte.dto.ProductoTopDTO;
import com.example.hormigas.reporte.dto.ValorInventarioDTO;
import com.example.hormigas.reporte.service.ReporteService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/reportes")
public class ReporteController {

    private final ReporteService reporteService;

    public ReporteController(ReporteService reporteService) {
        this.reporteService = reporteService;
    }

    @GetMapping("/movimientos")
    public List<MovimientoResponseDTO> movimientosPorPeriodo(
            @RequestParam(required = false) LocalDateTime fechaInicio,
            @RequestParam(required = false) LocalDateTime fechaFin,
            @RequestParam(required = false) Long sucursalId,
            @RequestParam(required = false) Long productoId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return reporteService.movimientosPorPeriodo(fechaInicio, fechaFin, sucursalId, productoId, page, size);
    }

    @GetMapping("/productos-top")
    public List<ProductoTopDTO> productosTop(
            @RequestParam(required = false) LocalDateTime fechaInicio,
            @RequestParam(required = false) LocalDateTime fechaFin,
            @RequestParam(required = false) Long sucursalId,
            @RequestParam(defaultValue = "10") int limite
    ) {
        return reporteService.productosTop(fechaInicio, fechaFin, sucursalId, limite);
    }

    @GetMapping("/valor-inventario")
    public ValorInventarioDTO valorInventario(@RequestParam Long sucursalId) {
        return reporteService.valorInventario(sucursalId);
    }
}
```

- [ ] **Step 3: Compilar**

```bash
./mvnw compile -q
```
Expected: `BUILD SUCCESS`

- [ ] **Step 4: Verificar que el contexto de Spring carga**

```bash
./mvnw test -Dtest=HormigasApplicationTests -q 2>&1 | tail -5
```
Expected: `BUILD SUCCESS`

- [ ] **Step 5: Commit final**

```bash
git add src/main/java/com/example/hormigas/reporte/
git commit -m "feat: add reporting endpoints (movements by period, top products, inventory value)"
```

---

## Resumen de commits esperados

```
feat: add reporting endpoints (movements by period, top products, inventory value)
feat: add inventory value report DTOs
feat: add productos-top aggregation query to MovimientoRepository
feat: add date range filters to movement queries
feat: add atomic transfer endpoint POST /api/traslado/crear
feat: add stock alert to movement response
feat: add AlertaStock record and AlertaStockService with unit tests
feat: validate cantidad > 0 and enforce stockMaximo on entry movements
feat: add DEVOLUCION_CLIENTE and DEVOLUCION_PROVEEDOR movement types
fix: set ultimaActualizacion on inventory create and guard agregarASucursal
fix: set inventario, fecha and ultimaActualizacion when registering movement
fix: reconnect MotivoMovimiento to Movimiento entity and service
fix: remove invalid @Index on non-existent columns in Movimiento entity
fix: correct typo prodcutoId -> productoId in InventarioFiltroDTO
build: add spring-boot-starter-validation dependency
```
