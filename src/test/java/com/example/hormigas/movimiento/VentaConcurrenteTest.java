package com.example.hormigas.movimiento;

import com.example.hormigas.empresa.entity.Empresa;
import com.example.hormigas.empresa.repository.EmpresaRepository;
import com.example.hormigas.inventario.entity.Inventario;
import com.example.hormigas.inventario.repository.InventarioRepository;
import com.example.hormigas.movimiento.dto.VentaBatchDTO;
import com.example.hormigas.movimiento.dto.VentaBatchItemDTO;
import com.example.hormigas.movimiento.dto.VentaBatchResponseDTO;
import com.example.hormigas.movimiento.service.MovimientoService;
import com.example.hormigas.producto.entity.Producto;
import com.example.hormigas.producto.repository.ProductoRepository;
import com.example.hormigas.security.domain.Role;
import com.example.hormigas.security.domain.Usuario;
import com.example.hormigas.security.domain.repository.UsuarioRepository;
import com.example.hormigas.security.domain.services.UsuarioService;
import com.example.hormigas.sucursal.entity.Sucursal;
import com.example.hormigas.sucursal.repository.SucursalRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

/** Dos cajas venden la ultima unidad al mismo tiempo: solo una venta debe pasar. */
@SpringBootTest
@ActiveProfiles("test")
class VentaConcurrenteTest {

    private static final int RONDAS = 5;

    @Autowired MovimientoService service;
    @Autowired InventarioRepository inventarioRepository;
    @Autowired EmpresaRepository empresaRepository;
    @Autowired SucursalRepository sucursalRepository;
    @Autowired ProductoRepository productoRepository;
    @Autowired UsuarioRepository usuarioRepository;
    @MockBean UsuarioService usuarioService;

    @Test
    void dosHilosVendiendoLaUltimaUnidadSoloUnoLaVende() throws Exception {
        String sfx = UUID.randomUUID().toString().substring(0, 8);
        Empresa empresa = empresaRepository.save(new Empresa("Conc " + sfx, sfx, "Dir", sfx));
        Usuario user = new Usuario();
        user.setEmpresa(empresa);
        user.setNombre("Cajero");
        user.setCorreo(sfx + "@conc.local");
        user.setPasswordHash("x");
        user.setActivo(true);
        user.setFechaCreacion(LocalDateTime.now());
        user.setRoles(Set.of(Role.USER));
        Usuario cajero = usuarioRepository.save(user);
        when(usuarioService.getUsuarioLogueado()).thenReturn(cajero);

        Sucursal s = new Sucursal();
        s.setEmpresa(empresa);
        s.setNombre("Centro");
        s.setDireccion("Dir");
        s.setActiva(true);
        Sucursal sucursal = sucursalRepository.save(s);

        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            for (int ronda = 0; ronda < RONDAS; ronda++) {
                Producto p = new Producto();
                p.setEmpresa(empresa);
                p.setNombre("Ultima " + ronda);
                p.setSku("U-" + sfx + "-" + ronda);
                p.setPrecio(new BigDecimal("10.00"));
                p.setActivo(true);
                Producto producto = productoRepository.save(p);
                Inventario i = new Inventario();
                i.setProducto(producto);
                i.setSucursal(sucursal);
                i.setStockActual(1);
                i.setStockMaximo(10);
                i.setStockMinimo(0);
                inventarioRepository.save(i);

                CountDownLatch salida = new CountDownLatch(1);
                List<Future<VentaBatchResponseDTO>> ventas = new ArrayList<>();
                for (int caja = 0; caja < 2; caja++) {
                    String clientId = "conc-" + sfx + "-" + ronda + "-" + caja;
                    ventas.add(pool.submit(() -> {
                        salida.await();
                        return service.registrarVentaBatch(new VentaBatchDTO(
                                sucursal.getId(), List.of(new VentaBatchItemDTO(producto.getId(), 1, null)),
                                null, clientId, null));
                    }));
                }
                salida.countDown();

                int procesados = 0;
                int rechazados = 0;
                for (Future<VentaBatchResponseDTO> f : ventas) {
                    VentaBatchResponseDTO r = f.get(30, TimeUnit.SECONDS);
                    procesados += r.procesados().size();
                    rechazados += r.rechazados().size();
                }

                assertEquals(1, procesados, "ronda " + ronda);
                assertEquals(1, rechazados, "ronda " + ronda);
                assertEquals(0, inventarioRepository
                        .findBySucursalIdAndProductoId(sucursal.getId(), producto.getId())
                        .orElseThrow().getStockActual(), "ronda " + ronda);
            }
        } finally {
            pool.shutdownNow();
        }
    }
}
