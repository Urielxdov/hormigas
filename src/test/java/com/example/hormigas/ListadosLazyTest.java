package com.example.hormigas;

import com.example.hormigas.empresa.entity.Empresa;
import com.example.hormigas.empresa.repository.EmpresaRepository;
import com.example.hormigas.inventario.dto.InventarioFiltroDTO;
import com.example.hormigas.inventario.dto.InventarioResponseDTO;
import com.example.hormigas.inventario.entity.Inventario;
import com.example.hormigas.inventario.repository.InventarioRepository;
import com.example.hormigas.inventario.service.InventarioService;
import com.example.hormigas.movimiento.dto.MovimientoFiltroDTO;
import com.example.hormigas.movimiento.dto.MovimientoResponseDTO;
import com.example.hormigas.movimiento.dto.VentaBatchDTO;
import com.example.hormigas.movimiento.dto.VentaBatchItemDTO;
import com.example.hormigas.movimiento.service.MovimientoService;
import com.example.hormigas.producto.dto.ProductoConStockDTO;
import com.example.hormigas.producto.entity.Producto;
import com.example.hormigas.producto.repository.ProductoRepository;
import com.example.hormigas.producto.service.ProductoService;
import com.example.hormigas.security.domain.Role;
import com.example.hormigas.security.domain.Usuario;
import com.example.hormigas.security.domain.repository.UsuarioRepository;
import com.example.hormigas.security.domain.services.UsuarioService;
import com.example.hormigas.sucursal.entity.Sucursal;
import com.example.hormigas.sucursal.repository.SucursalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Los listados mapean relaciones LAZY (inventario, producto, sucursal, usuario) sin
 * Open Session In View: deben cargarse dentro del servicio sin LazyInitializationException.
 */
@SpringBootTest
@ActiveProfiles("test")
class ListadosLazyTest {

    @Autowired MovimientoService movimientoService;
    @Autowired InventarioService inventarioService;
    @Autowired ProductoService productoService;
    @Autowired InventarioRepository inventarioRepository;
    @Autowired EmpresaRepository empresaRepository;
    @Autowired SucursalRepository sucursalRepository;
    @Autowired ProductoRepository productoRepository;
    @Autowired UsuarioRepository usuarioRepository;
    @MockBean UsuarioService usuarioService;

    Sucursal sucursal;
    Producto cola;
    Producto papas;

    @BeforeEach
    void setUp() {
        String sfx = UUID.randomUUID().toString().substring(0, 8);
        Empresa empresa = empresaRepository.save(new Empresa("Lazy " + sfx, sfx, "Dir", sfx));
        Usuario user = new Usuario();
        user.setEmpresa(empresa);
        user.setNombre("Cajero");
        user.setCorreo(sfx + "@lazy.local");
        user.setPasswordHash("x");
        user.setActivo(true);
        user.setFechaCreacion(LocalDateTime.now());
        user.setRoles(Set.of(Role.USER));
        user = usuarioRepository.save(user);
        when(usuarioService.getUsuarioLogueado()).thenReturn(user);

        Sucursal s = new Sucursal();
        s.setEmpresa(empresa);
        s.setNombre("Centro " + sfx);
        s.setDireccion("Dir");
        s.setActiva(true);
        sucursal = sucursalRepository.save(s);

        cola = producto(empresa, "Cola " + sfx, "C-" + sfx);
        papas = producto(empresa, "Papas " + sfx, "P-" + sfx);
        Inventario i = new Inventario();
        i.setProducto(cola);
        i.setSucursal(sucursal);
        i.setStockActual(10);
        i.setStockMinimo(20);
        i.setStockMaximo(100);
        inventarioRepository.save(i);
    }

    private Producto producto(Empresa e, String nombre, String sku) {
        Producto p = new Producto();
        p.setEmpresa(e);
        p.setNombre(nombre);
        p.setSku(sku);
        p.setPrecio(new BigDecimal("10.00"));
        p.setActivo(true);
        return productoRepository.save(p);
    }

    @Test
    void listarMovimientosMapeaRelacionesLazy() {
        movimientoService.registrarVentaBatch(new VentaBatchDTO(
                sucursal.getId(), List.of(new VentaBatchItemDTO(cola.getId(), 2, null)), null, "lazy-1", null));

        Page<MovimientoResponseDTO> page = movimientoService.obtenerMovimientos(
                new MovimientoFiltroDTO(sucursal.getId(), null, null, null), PageRequest.of(0, 20));

        assertEquals(1, page.getTotalElements());
        MovimientoResponseDTO m = page.getContent().get(0);
        assertEquals(cola.getNombre(), m.productoNombre());
        assertEquals(sucursal.getNombre(), m.sucursalNombre());
        assertEquals("Cajero", m.usuarioNombre());
    }

    @Test
    void listarInventarioYStockBajoMapeanRelacionesLazy() {
        List<InventarioResponseDTO> inv = inventarioService.obtenerInventario(new InventarioFiltroDTO(sucursal.getId(), null));
        assertEquals(1, inv.size());
        assertEquals(cola.getNombre(), inv.get(0).productoNombre());
        assertEquals(sucursal.getNombre(), inv.get(0).sucursalNombre());

        assertTrue(inventarioService.obtenerStockBajo().stream()
                .anyMatch(x -> x.productoId().equals(cola.getId())));
    }

    @Test
    void buscarConStockIncluyeProductosSinInventarioYRespetaLimite() {
        List<ProductoConStockDTO> todos = productoService.buscarConStock("", sucursal.getId(), null);
        assertEquals(2, todos.size());
        ProductoConStockDTO c = todos.stream().filter(p -> p.id().equals(cola.getId())).findFirst().orElseThrow();
        ProductoConStockDTO p = todos.stream().filter(x -> x.id().equals(papas.getId())).findFirst().orElseThrow();
        assertEquals(10, c.stockActual());
        assertNotNull(c.inventarioId());
        assertEquals(0, p.stockActual());
        assertNull(p.inventarioId());

        assertEquals(1, productoService.buscarConStock("", sucursal.getId(), 1).size());
        assertEquals(1, productoService.buscarConStock("papas", sucursal.getId(), null).size());
    }
}
