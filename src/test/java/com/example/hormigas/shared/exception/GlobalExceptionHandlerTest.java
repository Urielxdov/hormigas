package com.example.hormigas.shared.exception;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.*;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlerTest {

    record Body(@NotBlank String nombre) {}

    @RestController
    @RequestMapping("/t")
    static class ThrowingController {
        @GetMapping("/not-found") void notFound() { throw new EntityNotFoundException("No se encontro"); }
        @GetMapping("/bad") void bad() { throw new IllegalArgumentException("Parametro invalido"); }
        @GetMapping("/conflict") void conflict() { throw new ConflictException("El SKU ya existe en esta empresa"); }
        @GetMapping("/integrity") void integrity() {
            throw new DataIntegrityViolationException("could not execute statement [insert into secreto ...]");
        }
        @GetMapping("/runtime") void runtime() { throw new RuntimeException("NullPointer en algo interno"); }
        @GetMapping("/unauth") void unauth() { throw new AuthenticationCredentialsNotFoundException("x"); }
        @GetMapping("/forbidden") void forbidden() { throw new AccessDeniedException("Acceso denegado"); }
        @GetMapping("/typed") void typed(@RequestParam Long id) {}
        @PostMapping("/body") void body(@Valid @RequestBody Body body) {}
    }

    MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(new ThrowingController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void entidadNoEncontradaEs404() throws Exception {
        mvc.perform(get("/t/not-found")).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("No se encontro"));
    }

    @Test
    void peticionesInvalidasSon400() throws Exception {
        mvc.perform(get("/t/bad")).andExpect(status().isBadRequest());
        mvc.perform(get("/t/typed").param("id", "abc")).andExpect(status().isBadRequest());
        mvc.perform(get("/t/typed")).andExpect(status().isBadRequest());
        mvc.perform(post("/t/body").contentType(MediaType.APPLICATION_JSON).content("{no es json"))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/t/body").contentType(MediaType.APPLICATION_JSON).content("{\"nombre\":\" \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(containsString("nombre")));
    }

    @Test
    void conflictosSon409SinExponerSql() throws Exception {
        mvc.perform(get("/t/conflict")).andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("El SKU ya existe en esta empresa"));
        mvc.perform(get("/t/integrity")).andExpect(status().isConflict())
                .andExpect(content().string(not(containsString("insert"))));
    }

    @Test
    void runtimeGenericaEs500ConMensajeGenerico() throws Exception {
        mvc.perform(get("/t/runtime")).andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Error interno del servidor"));
    }

    @Test
    void seguridadDevuelve401y403() throws Exception {
        mvc.perform(get("/t/unauth")).andExpect(status().isUnauthorized());
        mvc.perform(get("/t/forbidden")).andExpect(status().isForbidden());
    }
}
