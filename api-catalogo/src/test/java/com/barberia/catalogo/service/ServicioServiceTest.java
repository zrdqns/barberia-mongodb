package com.barberia.catalogo.service;

import com.barberia.catalogo.cliente.CitasCliente;
import com.barberia.catalogo.dto.ServicioDTORequest;
import com.barberia.catalogo.model.Servicio;
import com.barberia.catalogo.repository.ServicioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Pruebas unitarias: el repositorio de MongoDB y la llamada a api-citas son simulados con Mockito.
@ExtendWith(MockitoExtension.class)
class ServicioServiceTest {

    @Mock
    private ServicioRepository servicioRepository;

    @Mock
    private CitasCliente citasCliente;

    @InjectMocks
    private ServicioService servicioService;

    private static ServicioDTORequest solicitud(String nombre, int precio, int duracion, Boolean activo) {
        ServicioDTORequest request = new ServicioDTORequest();
        request.setNombre(nombre);
        request.setPrecio(precio);
        request.setDuracionMinutos(duracion);
        request.setActivo(activo);
        return request;
    }

    @Test
    void crearGuardaElServicioActivoYSinEspacios() {
        when(servicioRepository.existsByNombreIgnoreCaseAndIdNot("Cejas", null)).thenReturn(false);
        when(servicioRepository.save(any(Servicio.class))).thenAnswer(invocacion -> {
            Servicio guardado = invocacion.getArgument(0);
            guardado.setId("a1");
            return guardado;
        });

        Servicio creado = servicioService.crear(solicitud("  Cejas ", 12000, 15, null));

        assertEquals("a1", creado.getId());
        assertEquals("Cejas", creado.getNombre());
        assertTrue(creado.isActivo());
    }

    @Test
    void crearConNombreRepetidoLanzaConflicto() {
        when(servicioRepository.existsByNombreIgnoreCaseAndIdNot("Tinte", null)).thenReturn(true);

        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> servicioService.crear(solicitud("Tinte", 60000, 90, true)));

        assertEquals("Ya existe un servicio con el nombre Tinte.", error.getMessage());
        verify(servicioRepository, never()).save(any());
    }

    @Test
    void buscarUnIdQueNoExisteLanzaNoEncontrado() {
        when(servicioRepository.findById("nada")).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> servicioService.buscarPorId("nada"));
    }

    @Test
    void actualizarSinActivoConservaElEstadoAnterior() {
        Servicio actual = new Servicio("a1", "Tinte", null, 60000, 90, false);
        when(servicioRepository.findById("a1")).thenReturn(Optional.of(actual));
        when(servicioRepository.existsByNombreIgnoreCaseAndIdNot("Tinte", "a1")).thenReturn(false);
        when(servicioRepository.save(actual)).thenReturn(actual);

        Servicio actualizado = servicioService.actualizar("a1", solicitud("Tinte", 65000, 90, null));

        assertEquals(65000, actualizado.getPrecio());
        assertFalse(actualizado.isActivo());
    }

    @Test
    void eliminarConCitasProgramadasNoBorraYExplicaPorQue() {
        Servicio servicio = new Servicio("a1", "Corte clásico", null, 25000, 30, true);
        when(servicioRepository.findById("a1")).thenReturn(Optional.of(servicio));
        when(citasCliente.contarProgramadas("servicioId", "a1")).thenReturn(2);

        IllegalStateException error = assertThrows(IllegalStateException.class, () -> servicioService.eliminar("a1"));

        assertTrue(error.getMessage().startsWith("El servicio Corte clásico tiene 2 citas programadas."));
        verify(servicioRepository, never()).delete(any());
    }

    @Test
    void eliminarSinCitasProgramadasBorraDeMongo() {
        Servicio servicio = new Servicio("a1", "Corte clásico", null, 25000, 30, true);
        when(servicioRepository.findById("a1")).thenReturn(Optional.of(servicio));
        when(citasCliente.contarProgramadas("servicioId", "a1")).thenReturn(0);

        servicioService.eliminar("a1");

        verify(servicioRepository).delete(servicio);
    }
}
