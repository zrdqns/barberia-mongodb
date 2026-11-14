package com.barberia.citas.controller;

import com.barberia.citas.config.JwtConfig;
import com.barberia.citas.config.SeguridadConfig;
import com.barberia.citas.model.DiaHorario;
import com.barberia.citas.model.EstadoCita;
import com.barberia.citas.service.CitaService;
import com.barberia.citas.service.ClienteService;
import com.barberia.citas.service.HorarioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Rutas, códigos HTTP, validación (@Valid) y permisos por rol de los controllers de api-citas.
@WebMvcTest({CitaController.class, ClienteController.class, HorarioController.class})
@Import({SeguridadConfig.class, JwtConfig.class})
class CitaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CitaService citaService;
    @MockitoBean
    private ClienteService clienteService;
    @MockitoBean
    private HorarioService horarioService;

    private static RequestPostProcessor comoRecepcion() {
        return jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Test
    void lasCitasExigenToken() throws Exception {
        mockMvc.perform(get("/api/citas"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void laRecepcionListaLasCitasConFiltros() throws Exception {
        when(citaService.listar(any(), any(), any(), any(), any())).thenReturn(List.of());

        mockMvc.perform(get("/api/citas?barberoId=b1&estado=PROGRAMADA&fecha=2026-11-20").with(comoRecepcion()))
                .andExpect(status().isOk());

        verify(citaService).listar(null, "b1", null, EstadoCita.PROGRAMADA, LocalDate.of(2026, 11, 20));
    }

    @Test
    void unEstadoQueNoExisteResponde400() throws Exception {
        mockMvc.perform(get("/api/citas?estado=PENDIENTE").with(comoRecepcion()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("El valor de estado no es válido."));
    }

    @Test
    void agendarSinDatosResponde400ConCadaCampo() throws Exception {
        mockMvc.perform(post("/api/citas").with(comoRecepcion())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"servicioId\":\"s1\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.clienteId").value("Indique el cliente de la cita."))
                .andExpect(jsonPath("$.errores.barberoId").value("Indique el barbero de la cita."))
                .andExpect(jsonPath("$.errores.fechaHora").value("La fecha y la hora de la cita son obligatorias."));
        verify(citaService, never()).crear(any());
    }

    @Test
    void unClienteConTelefonoInvalidoResponde400() throws Exception {
        mockMvc.perform(post("/api/clientes").with(comoRecepcion())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Ana\",\"documento\":\"1012345678\",\"telefono\":\"12\",\"correo\":\"ana@\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.telefono").value("El teléfono debe tener entre 7 y 10 dígitos."))
                .andExpect(jsonPath("$.errores.correo").value("El correo no tiene un formato válido."));
    }

    @Test
    void laRecepcionNoEliminaClientes() throws Exception {
        mockMvc.perform(delete("/api/clientes/c1").with(comoRecepcion()))
                .andExpect(status().isForbidden());
        verify(clienteService, never()).eliminar(any());
    }

    @Test
    void elHorarioSeConsultaSinToken() throws Exception {
        when(horarioService.listar()).thenReturn(List.of(new DiaHorario(1, LocalTime.of(9, 0), LocalTime.of(19, 0))));

        mockMvc.perform(get("/api/horario"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].dia").value("lunes"))
                .andExpect(jsonPath("$[0].apertura").value("09:00"));
    }

    @Test
    void laRecepcionNoCambiaElHorario() throws Exception {
        mockMvc.perform(put("/api/horario/7").with(comoRecepcion())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"apertura\":\"10:00\",\"cierre\":\"14:00\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void unEstadoInvalidoEnElPatchResponde400() throws Exception {
        mockMvc.perform(patch("/api/citas/x1/estado").with(comoRecepcion())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estado\":\"PENDIENTE\"}"))
                .andExpect(status().isBadRequest());
    }
}
