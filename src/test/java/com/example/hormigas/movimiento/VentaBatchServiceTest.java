package com.example.hormigas.movimiento;

import com.example.hormigas.empresa.entity.Empresa;
import com.example.hormigas.empresa.repository.EmpresaRepository;
import com.example.hormigas.inventario.entity.Inventario;
import com.example.hormigas.inventario.repository.InventarioRepository;
import com.example.hormigas.movimiento.dto.VentaBatchDTO;
import com.example.hormigas.movimiento.dto.VentaBatchItemDTO;
import com.example.hormigas.movimiento.dto.VentaBatchResponseDTO;
import com.example.hormigas.movimiento.repository.MovimientoRepository;
import com.example.hormigas.movimiento.service.MovimientoService;
import com.example.hormigas.producto.entity.Producto;
import com.example.hormigas.producto.repository.ProductoRepository;
import com.example.hormigas.security.domain.Role;
import com.example.hormigas.security.domain.Usuario;
import com.example.hormigas.security.domain.repository.UsuarioRepository;
import com.example.hormigas.security.domain.services.UsuarioService;
import com.example.hormigas.sucursal.entity.Sucursal;
import com.example.hormigas.sucursal.repository.SucursalRepository;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
class VentaBatchServiceTest {

    @Autowired MovimientoService service;
    @Autowired MovimientoRepository movimientoRepository;
    @Autowired InventarioRepository inventarioRepository;
    @Autowired EmpresaRepository empresaRepository;
    @Autowired SucursalRepository sucursalRepository;
    @Autowired ProductoRepository productoRepository;
    @Autowired UsuarioRepository usuarioRepository;
    @MockBean UsuarioService usuarioService;
    @Autowired Validator validator;

    Sucursal sucursal;
    Producto cola;
    Producto papas;

    @BeforeEach
    void setUp() {
        String sfx = UUID.randomUUID().toString().substring(0, 8);
        Empresa empresa = empresaRepository.save(new Empresa("Test " + sfx, sfx, "Dir", sfx));
        Usuario user = new Usuario();
        user.setEmpresa(empresa);
        user.setNombre("Cajero");
        user.setCorreo(sfx + "@test.local");
        user.setPasswordHash("x");
        user.setActivo(true);
        user.setFechaCreacion(LocalDateTime.now());
        user.setRoles(Set.of(Role.USER));
        user = usuarioRepository.save(user);
        when(usuarioService.getUsuarioLogueado()).thenReturn(user);

        Sucursal s = new Sucursal();
        s.setEmpresa(empresa);
        s.setNombre("Centro");
        s.setDireccion("Dir");
        s.setActiva(true);
        sucursal = sucursalRepository.save(s);

        cola = producto(empresa, "Cola " + sfx, "C-" + sfx, "20.00");
        papas = producto(empresa, "Papas " + sfx, "P-" + sfx, "15.50");
        inventario(cola, 10);
        inventario(papas, 1);
    }

    private Producto producto(Empresa e, String nombre, String sku, String precio) {
        Producto p = new Producto();
        p.setEmpresa(e);
        p.setNombre(nombre);
        p.setSku(sku);
        p.setPrecio(new BigDecimal(precio));
        p.setActivo(true);
        return productoRepository.save(p);
    }

    private void inventario(Producto p, int stock) {
        Inventario i = new Inventario();
        i.setProducto(p);
        i.setSucursal(sucursal);
        i.setStockActual(stock);
        i.setStockMaximo(100);
        i.setStockMinimo(0);
        inventarioRepository.save(i);
    }

    private int stock(Producto p) {
        return inventarioRepository.findBySucursalIdAndProductoId(sucursal.getId(), p.getId()).orElseThrow().getStockActual();
    }

    @Test
    void ventaFelizDescuentaStockYGuardaPrecioYFecha() {
        OffsetDateTime fecha = OffsetDateTime.now(ZoneOffset.UTC).minusHours(3);
        VentaBatchResponseDTO r = service.registrarVentaBatch(new VentaBatchDTO(
                sucursal.getId(),
                List.of(new VentaBatchItemDTO(cola.getId(), 3, new BigDecimal("18.00"))),
                null, "venta-1", fecha));

        assertEquals(1, r.procesados().size());
        assertTrue(r.rechazados().isEmpty());
        assertFalse(r.duplicada());
        assertEquals(7, stock(cola));
        assertEquals(0, new BigDecimal("18.00").compareTo(r.procesados().get(0).precioUnitario()));
        assertTrue(r.procesados().get(0).fecha().isBefore(LocalDateTime.now().minusHours(2)));
        assertEquals("venta-1", r.procesados().get(0).referencia());
    }

    @Test
    void sinPrecioDelClienteUsaPrecioDelProducto() {
        VentaBatchResponseDTO r = service.registrarVentaBatch(new VentaBatchDTO(
                sucursal.getId(), List.of(new VentaBatchItemDTO(cola.getId(), 1, null)), null, "venta-2", null));
        assertEquals(0, new BigDecimal("20.00").compareTo(r.procesados().get(0).precioUnitario()));
    }

    @Test
    void reintentoConMismoClientIdNoDuplicaMovimientosNiStock() {
        VentaBatchDTO dto = new VentaBatchDTO(
                sucursal.getId(), List.of(new VentaBatchItemDTO(cola.getId(), 4, null)), null, "venta-3", null);
        service.registrarVentaBatch(dto);
        VentaBatchResponseDTO segundo = service.registrarVentaBatch(dto);

        assertTrue(segundo.duplicada());
        assertEquals(1, segundo.procesados().size());
        assertEquals(6, stock(cola));
        assertEquals(1, movimientoRepository
                .findByVentaClientIdAndInventarioSucursalEmpresaId("venta-3", sucursal.getEmpresa().getId()).size());
    }

    @Test
    void ventaParcialRegistraLosValidosYReportaLosRechazados() {
        VentaBatchResponseDTO r = service.registrarVentaBatch(new VentaBatchDTO(
                sucursal.getId(),
                List.of(new VentaBatchItemDTO(cola.getId(), 2, null),
                        new VentaBatchItemDTO(papas.getId(), 5, null),
                        new VentaBatchItemDTO(999999L, 1, null)),
                null, "venta-4", null));

        assertEquals(1, r.procesados().size());
        assertEquals(2, r.rechazados().size());
        assertEquals(8, stock(cola));
        assertEquals(1, stock(papas));
        assertTrue(r.rechazados().stream().anyMatch(x -> x.productoId().equals(papas.getId())
                && x.motivo().contains("Stock insuficiente")));
    }

    @Test
    void reintentoTrasVentaParcialSoloProcesaLoPendiente() {
        VentaBatchDTO dto = new VentaBatchDTO(
                sucursal.getId(),
                List.of(new VentaBatchItemDTO(cola.getId(), 2, null), new VentaBatchItemDTO(papas.getId(), 5, null)),
                null, "venta-5", null);
        service.registrarVentaBatch(dto);
        Inventario inv = inventarioRepository.findBySucursalIdAndProductoId(sucursal.getId(), papas.getId()).orElseThrow();
        inv.setStockActual(10);
        inventarioRepository.save(inv);

        VentaBatchResponseDTO r = service.registrarVentaBatch(dto);

        assertEquals(2, r.procesados().size());
        assertTrue(r.rechazados().isEmpty());
        assertEquals(8, stock(cola));
        assertEquals(5, stock(papas));
    }

    @Test
    void itemsRepetidosSeFusionan() {
        VentaBatchResponseDTO r = service.registrarVentaBatch(new VentaBatchDTO(
                sucursal.getId(),
                List.of(new VentaBatchItemDTO(cola.getId(), 2, null), new VentaBatchItemDTO(cola.getId(), 3, null)),
                null, "venta-6", null));
        assertEquals(1, r.procesados().size());
        assertEquals(5, r.procesados().get(0).cantidad());
        assertEquals(5, stock(cola));
    }

    @Test
    void referenciaDemasiadoLargaSeRechazaComoDatoInvalidoYNoComoYaRegistrado() {
        VentaBatchResponseDTO r = service.registrarVentaBatch(new VentaBatchDTO(
                sucursal.getId(), List.of(new VentaBatchItemDTO(cola.getId(), 1, null)),
                "x".repeat(300), "venta-8", null));

        assertTrue(r.procesados().isEmpty());
        assertEquals(1, r.rechazados().size());
        assertEquals("Datos inválidos", r.rechazados().get(0).motivo());
        assertEquals(10, stock(cola));
    }

    @Test
    void productoInactivoSeRechaza() {
        cola.setActivo(false);
        productoRepository.save(cola);

        VentaBatchResponseDTO r = service.registrarVentaBatch(new VentaBatchDTO(
                sucursal.getId(), List.of(new VentaBatchItemDTO(cola.getId(), 1, null)), null, "venta-9", null));

        assertTrue(r.procesados().isEmpty());
        assertTrue(r.rechazados().get(0).motivo().contains("inactivo"));
        assertEquals(10, stock(cola));
    }

    @Test
    void itemsRepetidosConPrecioDistintoNoSeFusionan() {
        VentaBatchResponseDTO r = service.registrarVentaBatch(new VentaBatchDTO(
                sucursal.getId(),
                List.of(new VentaBatchItemDTO(cola.getId(), 2, new BigDecimal("20.00")),
                        new VentaBatchItemDTO(cola.getId(), 3, new BigDecimal("18.00"))),
                null, "venta-10", null));

        assertEquals(1, r.procesados().size());
        assertEquals(2, r.procesados().get(0).cantidad());
        assertEquals(1, r.rechazados().size());
        assertEquals(3, r.rechazados().get(0).cantidad());
        assertTrue(r.rechazados().get(0).motivo().contains("Precio distinto"));
        assertEquals(8, stock(cola));
    }

    @Test
    void clientIdEsObligatorio() {
        VentaBatchDTO sinClientId = new VentaBatchDTO(
                sucursal.getId(), List.of(new VentaBatchItemDTO(cola.getId(), 1, null)), null, " ", null);

        assertFalse(validator.validate(sinClientId).isEmpty());
        assertThrows(IllegalArgumentException.class, () -> service.registrarVentaBatch(sinClientId));
        assertEquals(10, stock(cola));
    }

    @Test
    void validacionDelDtoRechazaItemsNulosYPreciosConMasDeDosDecimales() {
        List<VentaBatchItemDTO> conNulo = new java.util.ArrayList<>();
        conNulo.add(null);
        assertFalse(validator.validate(new VentaBatchDTO(sucursal.getId(), conNulo, null, "v", null)).isEmpty());
        assertFalse(validator.validate(new VentaBatchDTO(sucursal.getId(),
                List.of(new VentaBatchItemDTO(cola.getId(), 1, new BigDecimal("1.001"))), null, "v", null)).isEmpty());
        assertFalse(validator.validate(new VentaBatchDTO(sucursal.getId(),
                List.of(new VentaBatchItemDTO(cola.getId(), 1, null)), "x".repeat(256), "v", null)).isEmpty());
    }

    @Test
    void mismoClientIdEnOtraSucursalNoSeConsideraDuplicado() {
        Sucursal otra = new Sucursal();
        otra.setEmpresa(sucursal.getEmpresa());
        otra.setNombre("Norte");
        otra.setDireccion("Dir");
        otra.setActiva(true);
        otra = sucursalRepository.save(otra);
        Inventario i = new Inventario();
        i.setProducto(cola);
        i.setSucursal(otra);
        i.setStockActual(5);
        i.setStockMaximo(100);
        i.setStockMinimo(0);
        inventarioRepository.save(i);

        service.registrarVentaBatch(new VentaBatchDTO(
                sucursal.getId(), List.of(new VentaBatchItemDTO(cola.getId(), 1, null)), null, "venta-11", null));
        VentaBatchResponseDTO r = service.registrarVentaBatch(new VentaBatchDTO(
                otra.getId(), List.of(new VentaBatchItemDTO(cola.getId(), 2, null)), null, "venta-11", null));

        assertFalse(r.duplicada());
        assertEquals(1, r.procesados().size());
        assertEquals(otra.getId(), r.procesados().get(0).sucursalId());
        assertEquals(9, stock(cola));
        assertEquals(3, inventarioRepository.findBySucursalIdAndProductoId(otra.getId(), cola.getId())
                .orElseThrow().getStockActual());
    }

    @Test
    void fechaDeMasDeTreintaDiasSeSustituyePorLaActual() {
        VentaBatchResponseDTO r = service.registrarVentaBatch(new VentaBatchDTO(
                sucursal.getId(), List.of(new VentaBatchItemDTO(cola.getId(), 1, null)),
                null, "venta-12", OffsetDateTime.now(ZoneOffset.UTC).minusDays(45)));
        assertTrue(r.procesados().get(0).fecha().isAfter(LocalDateTime.now().minusMinutes(1)));
    }

    @Test
    void fechaFuturaSeLimitaAlMomentoDelServidor() {
        VentaBatchResponseDTO r = service.registrarVentaBatch(new VentaBatchDTO(
                sucursal.getId(), List.of(new VentaBatchItemDTO(cola.getId(), 1, null)),
                null, "venta-7", OffsetDateTime.now(ZoneOffset.UTC).plusDays(5)));
        assertFalse(r.procesados().get(0).fecha().isAfter(LocalDateTime.now().plusSeconds(1)));
    }
}
