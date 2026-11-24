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

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SeguridadEndpointsTest {

    @Autowired MockMvc mvc;
    @Autowired TokenService tokenService;
    @Autowired EmpresaRepository empresaRepository;
    @Autowired SucursalRepository sucursalRepository;
    @Autowired UsuarioRepository usuarioRepository;

    String sfx;
    Empresa empresa;
    Sucursal sucursalPropia;
    Sucursal sucursalAjena;
    Usuario admin;
    Usuario cajero;

    @BeforeEach
    void setUp() {
        sfx = UUID.randomUUID().toString().substring(0, 8);
        empresa = empresaRepository.save(new Empresa("Seg " + sfx, "R" + sfx, "Dir", sfx));
        Empresa otra = empresaRepository.save(new Empresa("Otra " + sfx, "O" + sfx, "Dir", "T" + sfx));
        sucursalPropia = sucursal(empresa);
        sucursalAjena = sucursal(otra);
        admin = usuario(empresa, "admin", Role.ADMIN);
        cajero = usuario(empresa, "cajero", Role.USER);
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
        u.setCorreo(nombre + "-" + sfx + "@seg.local");
        u.setPasswordHash("x");
        u.setActivo(true);
        u.setFechaCreacion(LocalDateTime.now());
        u.setRoles(new HashSet<>(Set.of(rol)));
        return usuarioRepository.save(u);
    }

    private String bearer(Usuario u) {
        return "Bearer " + tokenService.generateToken(new UsernamePasswordAuthenticationToken(u.getCorreo(), null, List.of()));
    }

    private String nuevoUsuarioJson(String correo, Long sucursalId) {
        return """
                {"correo":"%s","password":"Secreta123","nombre":"Nuevo","sucursalId":%s}
                """.formatted(correo, sucursalId);
    }

    @Test
    void registroSinTokenOConRolUserEsRechazado() throws Exception {
        mvc.perform(post("/api/auth").contentType(MediaType.APPLICATION_JSON)
                        .content(nuevoUsuarioJson("anon-" + sfx + "@seg.local", sucursalPropia.getId())))
                .andExpect(status().is4xxClientError());
        mvc.perform(post("/api/auth").header("Authorization", bearer(cajero)).contentType(MediaType.APPLICATION_JSON)
                        .content(nuevoUsuarioJson("user-" + sfx + "@seg.local", sucursalPropia.getId())))
                .andExpect(status().isForbidden());
        assertTrue(usuarioRepository.findByCorreo("anon-" + sfx + "@seg.local").isEmpty());
        assertTrue(usuarioRepository.findByCorreo("user-" + sfx + "@seg.local").isEmpty());
    }

    @Test
    void adminSoloRegistraEnSucursalesDeSuEmpresa() throws Exception {
        String ajeno = "ajeno-" + sfx + "@seg.local";
        mvc.perform(post("/api/auth").header("Authorization", bearer(admin)).contentType(MediaType.APPLICATION_JSON)
                        .content(nuevoUsuarioJson(ajeno, sucursalAjena.getId())))
                .andExpect(status().isNotFound());
        assertTrue(usuarioRepository.findByCorreo(ajeno).isEmpty());

        String propio = "propio-" + sfx + "@seg.local";
        mvc.perform(post("/api/auth").header("Authorization", bearer(admin)).contentType(MediaType.APPLICATION_JSON)
                        .content(nuevoUsuarioJson(propio, sucursalPropia.getId())))
                .andExpect(status().isOk());
        Usuario creado = usuarioRepository.findByCorreo(propio).orElseThrow();
        assertEquals(empresa.getId(), creado.getEmpresa().getId());
        assertEquals(sucursalPropia.getId(), creado.getSucursal().getId());
        assertEquals(Set.of(Role.USER), creado.getRoles());
    }

    @Test
    void loginEsPublico() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"root@test.local\",\"password\":\"Root12345!\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void tokenDeUsuarioDesactivadoOEmpresaInactivaYaNoSirve() throws Exception {
        String token = bearer(cajero);
        mvc.perform(get("/api/categoria/").header("Authorization", token)).andExpect(status().is2xxSuccessful());

        cajero.setActivo(false);
        usuarioRepository.save(cajero);
        mvc.perform(get("/api/categoria/").header("Authorization", token)).andExpect(status().isUnauthorized());

        String tokenAdmin = bearer(admin);
        empresa.setActivo(false);
        empresaRepository.save(empresa);
        mvc.perform(get("/api/categoria/").header("Authorization", tokenAdmin)).andExpect(status().isUnauthorized());
    }

    @Test
    void tokenInvalidoEs401() throws Exception {
        mvc.perform(get("/api/categoria/").header("Authorization", "Bearer basura"))
                .andExpect(status().isUnauthorized());
    }
}
