package com.barberia.catalogo.controller;

import com.barberia.catalogo.config.JwtConfig;
import com.barberia.catalogo.config.SeguridadConfig;
import com.barberia.catalogo.model.Servicio;
import com.barberia.catalogo.service.ServicioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.List;
import java.util.NoSuchElementException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.xpath;

// Pruebas del controller con MockMvc: rutas, códigos HTTP, validación (@Valid) y permisos por rol.
// El service se reemplaza por un doble de Mockito, así que no se necesita MongoDB.
@WebMvcTest(ServicioController.class)
@Import({SeguridadConfig.class, JwtConfig.class})
class ServicioControllerTest {

    private static final String CUERPO_VALIDO = "{\"nombre\":\"Cejas\",\"precio\":12000,\"duracionMinutos\":15}";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ServicioService servicioService;

    private static RequestPostProcessor comoAdmin() {
        return jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }

    private static RequestPostProcessor comoRecepcion() {
        return jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Test
    void listarEsPublicoYRespondeJson() throws Exception {
        when(servicioService.listar()).thenReturn(List.of(new Servicio("a1", "Corte clásico", null, 25000, 30, true)));

        mockMvc.perform(get("/api/servicios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("a1"))
                .andExpect(jsonPath("$[0].precio").value(25000));
    }

    @Test
    void listarTambienRespondeXml() throws Exception {
        when(servicioService.listar()).thenReturn(List.of(new Servicio("a1", "Corte clásico", null, 25000, 30, true)));

        mockMvc.perform(get("/api/servicios").accept(MediaType.APPLICATION_XML))
                .andExpect(status().isOk())
                .andExpect(xpath("/List/item/nombre").string("Corte clásico"));
    }

    @Test
    void crearSinTokenResponde401() throws Exception {
        mockMvc.perform(post("/api/servicios").contentType(MediaType.APPLICATION_JSON).content(CUERPO_VALIDO))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensaje").exists());
        verify(servicioService, never()).crear(any());
    }

    @Test
    void crearConRolUserResponde403() throws Exception {
        mockMvc.perform(post("/api/servicios").with(comoRecepcion())
                        .contentType(MediaType.APPLICATION_JSON).content(CUERPO_VALIDO))
                .andExpect(status().isForbidden());
        verify(servicioService, never()).crear(any());
    }

    @Test
    void crearComoAdminResponde201() throws Exception {
        when(servicioService.crear(any())).thenReturn(new Servicio("a5", "Cejas", null, 12000, 15, true));

        mockMvc.perform(post("/api/servicios").with(comoAdmin())
                        .contentType(MediaType.APPLICATION_JSON).content(CUERPO_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("a5"))
                .andExpect(jsonPath("$.activo").value(true));
    }

    @Test
    void crearConDatosInvalidosResponde400ConCadaCampo() throws Exception {
        mockMvc.perform(post("/api/servicios").with(comoAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\" \",\"precio\":0,\"duracionMinutos\":300}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.nombre").value("El nombre del servicio es obligatorio."))
                .andExpect(jsonPath("$.errores.precio").value("El precio debe ser mayor que cero."))
                .andExpect(jsonPath("$.errores.duracionMinutos").value("La duración debe estar entre 5 y 240 minutos."));
        verify(servicioService, never()).crear(any());
    }

    @Test
    void consultarUnIdQueNoExisteResponde404() throws Exception {
        when(servicioService.buscarPorId("nada")).thenThrow(new NoSuchElementException("No se encontró un servicio con el ID nada."));

        mockMvc.perform(get("/api/servicios/nada"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("No se encontró un servicio con el ID nada."));
    }

    @Test
    void eliminarConCitasProgramadasResponde409() throws Exception {
        doThrow(new IllegalStateException("Tiene citas programadas.")).when(servicioService).eliminar("a1");

        mockMvc.perform(delete("/api/servicios/a1").with(comoAdmin()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensaje").value("Tiene citas programadas."));
    }

    @Test
    void lasInterfacesPuedenLlamarConCors() throws Exception {
        mockMvc.perform(options("/api/servicios")
                        .header("Origin", "http://localhost:5175")
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "Authorization, Content-Type"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5175"));
    }
}
