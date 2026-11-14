package com.barberia.citas.service;

import com.barberia.citas.cliente.CatalogoCliente;
import com.barberia.citas.dto.CitaDTORequest;
import com.barberia.citas.model.Barbero;
import com.barberia.citas.model.Cita;
import com.barberia.citas.model.Cliente;
import com.barberia.citas.model.EstadoCita;
import com.barberia.citas.model.Servicio;
import com.barberia.citas.repository.CitaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Pruebas unitarias de las reglas para agendar. MongoDB, api-catalogo y el horario son dobles de Mockito.
@ExtendWith(MockitoExtension.class)
class CitaServiceTest {

    // El próximo lunes a las 10:00: siempre en el futuro y dentro del horario
    private static final LocalDateTime LUNES_10 =
            LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.MONDAY)).atTime(10, 0);

    @Mock
    private CitaRepository citaRepository;
    @Mock
    private MongoTemplate mongoTemplate;
    @Mock
    private ClienteService clienteService;
    @Mock
    private HorarioService horarioService;
    @Mock
    private CatalogoCliente catalogoCliente;

    @InjectMocks
    private CitaService citaService;

    private final Cliente carlos = new Cliente("c1", "Carlos Ramírez", "1098765432", "3001234567", null);
    private final Servicio corte = new Servicio("s1", "Corte clásico", 25000, 30, true);
    private final Barbero andres = new Barbero("b1", "Andrés Rojas", "Cortes clásicos", true);

    @BeforeEach
    void catalogoYClientes() {
        lenient().when(clienteService.buscarPorId("c1")).thenReturn(carlos);
        lenient().when(catalogoCliente.buscarServicio("s1")).thenReturn(corte);
        lenient().when(catalogoCliente.buscarBarbero("b1")).thenReturn(andres);
        lenient().when(citaRepository.save(any(Cita.class))).thenAnswer(invocacion -> invocacion.getArgument(0));
    }

    private static CitaDTORequest solicitud(LocalDateTime fechaHora) {
        CitaDTORequest request = new CitaDTORequest();
        request.setClienteId("c1");
        request.setServicioId("s1");
        request.setBarberoId("b1");
        request.setFechaHora(fechaHora);
        return request;
    }

    private static Cita citaProgramada(String id, String barberoId, String clienteId, LocalDateTime fechaHora) {
        Cita cita = new Cita();
        cita.setId(id);
        cita.setEstado(EstadoCita.PROGRAMADA);
        cita.setFechaHora(fechaHora);
        cita.setDuracionMinutos(30);
        cita.setBarberoId(barberoId);
        cita.setBarberoNombre("Andrés Rojas");
        cita.setClienteId(clienteId);
        cita.setServicioId("s1");
        cita.setServicioNombre("Corte clásico");
        cita.setPrecio(25000);
        return cita;
    }

    @Test
    void crearGuardaLaCitaProgramadaConLaCopiaDelCatalogo() {
        Cita creada = citaService.crear(solicitud(LUNES_10.plusSeconds(25)));

        assertEquals(EstadoCita.PROGRAMADA, creada.getEstado());
        assertEquals(LUNES_10, creada.getFechaHora());
        assertEquals("c1", creada.getClienteId());
        assertEquals("Carlos Ramírez", creada.getClienteNombre());
        assertEquals("Corte clásico", creada.getServicioNombre());
        assertEquals(25000, creada.getPrecio());
        assertEquals(30, creada.getDuracionMinutos());
        assertEquals("Andrés Rojas", creada.getBarberoNombre());
        verify(horarioService).validarAtencion(LUNES_10, LUNES_10.plusMinutes(30));
    }

    @Test
    void noSeAgendaEnUnaFechaPasada() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> citaService.crear(solicitud(LocalDateTime.now().minusDays(1))));

        assertEquals("La cita no puede agendarse en una fecha pasada.", error.getMessage());
        verify(citaRepository, never()).save(any());
    }

    @Test
    void noSeAgendaUnServicioInactivo() {
        when(catalogoCliente.buscarServicio("s1")).thenReturn(new Servicio("s1", "Tinte", 60000, 90, false));

        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> citaService.crear(solicitud(LUNES_10)));

        assertEquals("El servicio Tinte no está activo.", error.getMessage());
    }

    @Test
    void unServicioQueNoExisteEnElCatalogoEsNoEncontrado() {
        when(catalogoCliente.buscarServicio("s1")).thenReturn(null);

        assertThrows(NoSuchElementException.class, () -> citaService.crear(solicitud(LUNES_10)));
    }

    @Test
    void elBarberoNoPuedeTenerDosCitasCruzadas() {
        when(citaRepository.findByEstadoAndFechaHoraBetween(any(), any(), any()))
                .thenReturn(List.of(citaProgramada("x1", "b1", "c9", LUNES_10.minusMinutes(15))));

        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> citaService.crear(solicitud(LUNES_10)));

        assertEquals("El barbero Andrés Rojas ya tiene una cita de 9:45 a 10:15.", error.getMessage());
        verify(citaRepository, never()).save(any());
    }

    @Test
    void unaCitaQueTerminaJustoAntesNoSeCruza() {
        when(citaRepository.findByEstadoAndFechaHoraBetween(any(), any(), any()))
                .thenReturn(List.of(citaProgramada("x1", "b1", "c1", LUNES_10.minusMinutes(30))));

        Cita creada = citaService.crear(solicitud(LUNES_10));

        assertEquals(LUNES_10, creada.getFechaHora());
    }

    @Test
    void editarSinCambiarElServicioConservaElPrecioAgendado() {
        Cita cita = citaProgramada("x1", "b1", "c1", LUNES_10);
        cita.setPrecio(20000);
        when(citaRepository.findById("x1")).thenReturn(Optional.of(cita));

        Cita editada = citaService.actualizar("x1", solicitud(LUNES_10.plusHours(2)));

        assertEquals(20000, editada.getPrecio());
        assertEquals(LUNES_10.plusHours(2), editada.getFechaHora());
        verify(catalogoCliente, never()).buscarServicio(any());
    }

    @Test
    void unaCitaCanceladaNoSeEdita() {
        Cita cita = citaProgramada("x1", "b1", "c1", LUNES_10);
        cita.setEstado(EstadoCita.CANCELADA);
        when(citaRepository.findById("x1")).thenReturn(Optional.of(cita));

        assertThrows(IllegalStateException.class, () -> citaService.actualizar("x1", solicitud(LUNES_10)));
    }

    @Test
    void noSeCompletaUnaCitaQueTodaviaNoEmpieza() {
        when(citaRepository.findById("x1")).thenReturn(Optional.of(citaProgramada("x1", "b1", "c1", LUNES_10)));

        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> citaService.cambiarEstado("x1", EstadoCita.COMPLETADA));

        assertTrue(error.getMessage().startsWith("La cita todavía no ha empezado."));
    }

    @Test
    void cancelarCambiaElEstado() {
        when(citaRepository.findById("x1")).thenReturn(Optional.of(citaProgramada("x1", "b1", "c1", LUNES_10)));

        Cita cancelada = citaService.cambiarEstado("x1", EstadoCita.CANCELADA);

        assertEquals(EstadoCita.CANCELADA, cancelada.getEstado());
    }
}
