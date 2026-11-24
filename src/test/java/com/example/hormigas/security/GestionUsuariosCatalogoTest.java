package com.example.hormigas.security;

import com.example.hormigas.empresa.entity.Empresa;
import com.example.hormigas.empresa.repository.EmpresaRepository;
import com.example.hormigas.security.domain.Role;
import com.example.hormigas.security.domain.Usuario;
import com.example.hormigas.security.domain.repository.UsuarioRepository;
import com.example.hormigas.security.domain.services.TokenService;
import com.example.hormigas.sucursal.entity.Sucursal;
import com.example.hormigas.sucursal.repository.SucursalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Usuarios (estado, sucursal, activar/desactivar), empresas y categorias desde la API. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class GestionUsuariosCatalogoTest {

    @Autowired MockMvc mvc;
    @Autowired TokenService tokenService;
    @Autowired EmpresaRepository empresaRepository;
    @Autowired SucursalRepository sucursalRepository;
    @Autowired UsuarioRepository usuarioRepository;

    String sfx;
    Empresa empresa;
    Sucursal sucursal;
    Sucursal sucursalAjena;
    Usuario admin;
    Usuario superAdmin;

    @BeforeEach
    void setUp() {
        sfx = UUID.randomUUID().toString().substring(0, 8);
        empresa = empresaRepository.save(new Empresa("Gest " + sfx, "G" + sfx, "Dir", sfx));
        Empresa otra = empresaRepository.save(new Empresa("Ajena " + sfx, "A" + sfx, "Dir", "T" + sfx));
        Empresa sistema = empresaRepository.save(new Empresa("Sis " + sfx, "S" + sfx, "Dir", "S" + sfx));
        sucursal = sucursal(empresa);
        sucursalAjena = sucursal(otra);
        admin = usuario(empresa, "admin", Role.ADMIN);
        superAdmin = usuario(sistema, "super", Role.SUPER_ADMIN);
    }

    private Sucursal sucursal(Empresa e) {
        Sucursal s = new Sucursal();
        s.setEmpresa(e);
        s.setNombre("Suc " + sfx);
        s.setDireccion("Dir");
        s.setActiva(true);
        return sucursalRepository.save(s);
    }

    private Usuario usuario(Empresa e, String nombre, Role rol) {
        Usuario u = new Usuario();
        u.setEmpresa(e);
        u.setNombre(nombre);
        u.setCorreo(nombre + "-" + sfx + "@gest.local");
        u.setPasswordHash("x");
        u.setActivo(true);
        u.setFechaCreacion(LocalDateTime.now());
        u.setRoles(new HashSet<>(Set.of(rol)));
        return usuarioRepository.save(u);
    }

    private MockHttpServletRequestBuilder como(Usuario u, MockHttpServletRequestBuilder req) {
        String token = tokenService.generateToken(new UsernamePasswordAuthenticationToken(u.getCorreo(), null, List.of()));
        return req.header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON);
    }

    @Test
    void cajeroCreadoQuedaActivoConSucursalYSeListaConEstado() throws Exception {
        String correo = "Cajero-" + sfx + "@Gest.local";
        mvc.perform(como(admin, post("/api/usuario/create")).content("""
                {"correo":"%s","password":"Secreta123","nombre":"  Cajero Uno ","sucursalId":%d}
                """.formatted(correo, sucursal.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activo").value(true))
                .andExpect(jsonPath("$.correo").value(correo.toLowerCase()))
                .andExpect(jsonPath("$.name").value("Cajero Uno"))
                .andExpect(jsonPath("$.sucursalId").value(sucursal.getId()))
                .andExpect(jsonPath("$.sucursalNombre").value(sucursal.getNombre()))
                .andExpect(jsonPath("$.roles", contains("USER")));

        mvc.perform(como(admin, get("/api/usuario/list")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].activo", everyItem(is(true))))
                .andExpect(jsonPath("$[*].correo", hasItem(correo.toLowerCase())));
    }

    @Test
    void cajeroSinSucursalOSucursalAjenaSeRechaza() throws Exception {
        mvc.perform(como(admin, post("/api/usuario/create")).content("""
                {"correo":"sin-%s@gest.local","password":"Secreta123","nombre":"Sin"}
                """.formatted(sfx)))
                .andExpect(status().isBadRequest());
        mvc.perform(como(admin, post("/api/usuario/create")).content("""
                {"correo":"ajena-%s@gest.local","password":"Secreta123","nombre":"Ajena","sucursalId":%d}
                """.formatted(sfx, sucursalAjena.getId())))
                .andExpect(status().isNotFound());
        mvc.perform(como(admin, post("/api/usuario/create")).content("""
                {"correo":"no-es-correo","password":"corta","nombre":"X","sucursalId":%d}
                """.formatted(sucursal.getId())))
                .andExpect(status().isBadRequest());
    }

    @Test
    void desactivarUsuarioLoMantieneEnLaListaYNoPuedeDesactivarseASiMismo() throws Exception {
        Usuario cajero = usuario(empresa, "cajero", Role.USER);
        mvc.perform(como(admin, patch("/api/usuario/" + cajero.getId() + "/toggle")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activo").value(false));
        mvc.perform(como(admin, get("/api/usuario/list")))
                .andExpect(jsonPath("$[?(@.id == %d)].activo".formatted(cajero.getId()), contains(false)));
        // El cajero desactivado ya no puede usar su token
        mvc.perform(como(cajero, get("/api/categoria/")))
                .andExpect(status().isUnauthorized());

        mvc.perform(como(admin, patch("/api/usuario/" + admin.getId() + "/toggle")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void empresaSeListaConRfcEstadoYSinLaDelSistema() throws Exception {
        mvc.perform(como(superAdmin, get("/api/empresa/all")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == %d)].rfc".formatted(empresa.getId()), contains(empresa.getRfc())))
                .andExpect(jsonPath("$[?(@.id == %d)].activo".formatted(empresa.getId()), contains(true)))
                .andExpect(jsonPath("$[*].id", not(hasItem(superAdmin.getEmpresa().getId().intValue()))));

        // La empresa desactivada sigue en la lista para poder reactivarla
        mvc.perform(como(superAdmin, delete("/api/empresa/delete/" + empresa.getId())))
                .andExpect(status().isNoContent());
        mvc.perform(como(superAdmin, get("/api/empresa/all")))
                .andExpect(jsonPath("$[?(@.id == %d)].activo".formatted(empresa.getId()), contains(false)));

        mvc.perform(como(superAdmin, delete("/api/empresa/delete/" + superAdmin.getEmpresa().getId())))
                .andExpect(status().isBadRequest());
    }

    @Test
    void crearEmpresaValidaRfcYCorreoDelAdminSinDejarEmpresaHuerfana() throws Exception {
        mvc.perform(como(superAdmin, post("/api/empresa/create")).content("""
                {"empresa":{"nombre":"Mala %s","rfc":"123","telefono":"55"},
                 "admin":{"nombre":"A","correo":"a-%s@gest.local","password":"Secreta123"}}
                """.formatted(sfx, sfx)))
                .andExpect(status().isBadRequest());

        String rfc = ("emp" + "010101" + "ab1").toLowerCase();
        mvc.perform(como(superAdmin, post("/api/empresa/create")).content("""
                {"empresa":{"nombre":"Dup %s","rfc":"%s","telefono":"5512345678"},
                 "admin":{"nombre":"A","correo":"%s","password":"Secreta123"}}
                """.formatted(sfx, rfc, admin.getCorreo())))
                .andExpect(status().isBadRequest());
        // No se creo la empresa con el correo repetido
        mvc.perform(como(superAdmin, get("/api/empresa/all")))
                .andExpect(jsonPath("$[*].nombre", not(hasItem("Dup " + sfx))));
    }

    @Test
    void categoriasDuplicadasInactivasYAsignacionAProductos() throws Exception {
        String nombre = "Bebidas " + sfx;
        String body = mvc.perform(como(admin, post("/api/categoria/nueva")).content("""
                {"nombre":"  %s  "}
                """.formatted(nombre)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value(nombre))
                .andReturn().getResponse().getContentAsString();
        long catId = Long.parseLong(body.replaceAll(".*\"id\":(\\d+).*", "$1"));

        mvc.perform(como(admin, post("/api/categoria/nueva")).content("""
                {"nombre":"%s"}
                """.formatted(nombre.toUpperCase())))
                .andExpect(status().isConflict());

        mvc.perform(como(admin, patch("/api/categoria/" + catId + "/toggle")))
                .andExpect(jsonPath("$.activo").value(false));
        mvc.perform(como(admin, get("/api/categoria/")))
                .andExpect(jsonPath("$[*].id", not(hasItem((int) catId))));
        mvc.perform(como(admin, get("/api/categoria/?todas=true")))
                .andExpect(jsonPath("$[?(@.id == %d)].activo".formatted(catId), contains(false)));

        // No se puede asignar una categoria inactiva ni una inexistente
        mvc.perform(como(admin, post("/api/producto/nuevo")).content("""
                {"nombre":"P %s","sku":"SKU-%s","precio":10,"categoriaId":%d}
                """.formatted(sfx, sfx, catId)))
                .andExpect(status().isBadRequest());
        mvc.perform(como(admin, post("/api/producto/nuevo")).content("""
                {"nombre":"P %s","sku":"SKU-%s","precio":10,"categoriaId":999999}
                """.formatted(sfx, sfx)))
                .andExpect(status().isNotFound());

        mvc.perform(como(admin, patch("/api/categoria/" + catId + "/toggle")))
                .andExpect(jsonPath("$.activo").value(true));
        mvc.perform(como(admin, post("/api/producto/nuevo")).content("""
                {"nombre":"P %s","sku":"SKU-%s","precio":10,"categoriaId":%d}
                """.formatted(sfx, sfx, catId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categoriaId").value(catId))
                .andExpect(jsonPath("$.categoria").value(nombre));

        // Renombrar la categoria se refleja en el producto
        mvc.perform(como(admin, put("/api/categoria/" + catId)).content("""
                {"nombre":"Refrescos %s"}
                """.formatted(sfx)))
                .andExpect(status().isOk());
        mvc.perform(como(admin, get("/api/producto/?todos=true&size=500")))
                .andExpect(jsonPath("$.content[?(@.sku == 'SKU-%s')].categoria".formatted(sfx), contains("Refrescos " + sfx)))
                .andExpect(jsonPath("$.content[?(@.sku == 'SKU-%s')].stockTotal".formatted(sfx), contains(0)));
    }

    @Test
    void cajeroNoPuedeModificarCatalogo() throws Exception {
        Usuario cajero = usuario(empresa, "caj2", Role.USER);
        cajero.setSucursal(sucursal);
        usuarioRepository.save(cajero);
        mvc.perform(como(cajero, post("/api/producto/nuevo")).content("""
                {"nombre":"X %s","sku":"X-%s","precio":1}
                """.formatted(sfx, sfx)))
                .andExpect(status().isForbidden());
        mvc.perform(como(cajero, post("/api/categoria/nueva")).content("""
                {"nombre":"X %s"}
                """.formatted(sfx)))
                .andExpect(status().isForbidden());
    }
}
