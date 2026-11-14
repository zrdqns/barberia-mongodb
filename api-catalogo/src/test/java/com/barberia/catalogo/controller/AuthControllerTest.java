package com.barberia.catalogo.controller;

import com.barberia.catalogo.config.JwtConfig;
import com.barberia.catalogo.config.SeguridadConfig;
import com.barberia.catalogo.model.Servicio;
import com.barberia.catalogo.service.AuthService;
import com.barberia.catalogo.service.BarberoService;
import com.barberia.catalogo.service.ServicioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Login real: AuthService firma el token y la cadena de seguridad lo valida en la siguiente petición.
@WebMvcTest({AuthController.class, ServicioController.class, BarberoController.class})
@Import({SeguridadConfig.class, JwtConfig.class, AuthService.class})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ServicioService servicioService;

    @MockitoBean
    private BarberoService barberoService;

    private String iniciarSesion(String usuario) throws Exception {
        String cuerpo = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + usuario + "\",\"password\":\"123456\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipo").value("Bearer"))
                .andReturn().getResponse().getContentAsString();
        Matcher token = Pattern.compile("\"token\":\"([^\"]+)\"").matcher(cuerpo);
        token.find();
        return token.group(1);
    }

    @Test
    void loginDelAdminDevuelveTokenConSuRol() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"123456\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.usuario").value("admin"))
                .andExpect(jsonPath("$.rol").value("ADMIN"));
    }

    @Test
    void loginConContrasenaIncorrectaResponde401() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"otra\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensaje").value("Usuario o contraseña incorrectos."));
    }

    @Test
    void elTokenDelAdminPermiteCrear() throws Exception {
        when(servicioService.crear(any())).thenReturn(new Servicio("a5", "Cejas", null, 12000, 15, true));
        String token = iniciarSesion("admin");

        mockMvc.perform(post("/api/servicios")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Cejas\",\"precio\":12000,\"duracionMinutos\":15}"))
                .andExpect(status().isCreated());
    }

    @Test
    void elTokenDeRecepcionNoPermiteCrear() throws Exception {
        String token = iniciarSesion("recepcion");

        mockMvc.perform(post("/api/barberos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Otro\",\"documento\":\"1045678901\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void unTokenAlteradoResponde401() throws Exception {
        String token = iniciarSesion("admin");

        mockMvc.perform(post("/api/servicios")
                        .header("Authorization", "Bearer " + token + "x")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Cejas\",\"precio\":12000,\"duracionMinutos\":15}"))
                .andExpect(status().isUnauthorized());
    }
}
