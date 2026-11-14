package com.barberia.catalogo.service;

import com.barberia.catalogo.cliente.CitasCliente;
import com.barberia.catalogo.dto.BarberoDTORequest;
import com.barberia.catalogo.model.Barbero;
import com.barberia.catalogo.repository.BarberoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.ResourceAccessException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BarberoServiceTest {

    @Mock
    private BarberoRepository barberoRepository;

    @Mock
    private CitasCliente citasCliente;

    @InjectMocks
    private BarberoService barberoService;

    @Test
    void crearConTelefonoVacioLoGuardaComoNull() {
        BarberoDTORequest request = new BarberoDTORequest();
        request.setNombre("Camilo Ruiz");
        request.setDocumento(" 1045678901 ");
        request.setTelefono("  ");
        when(barberoRepository.existsByDocumentoAndIdNot("1045678901", null)).thenReturn(false);
        when(barberoRepository.save(any(Barbero.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        Barbero creado = barberoService.crear(request);

        assertEquals("1045678901", creado.getDocumento());
        assertNull(creado.getTelefono());
    }

    @Test
    void crearConDocumentoRepetidoLanzaConflicto() {
        BarberoDTORequest request = new BarberoDTORequest();
        request.setNombre("Otro");
        request.setDocumento("1012345678");
        when(barberoRepository.existsByDocumentoAndIdNot("1012345678", null)).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> barberoService.crear(request));
        verify(barberoRepository, never()).save(any());
    }

    @Test
    void siApiCitasNoRespondeNoSeElimina() {
        Barbero barbero = new Barbero("b1", "Julián Mora", "1023456789", null, null, true);
        when(barberoRepository.findById("b1")).thenReturn(Optional.of(barbero));
        when(citasCliente.contarProgramadas("barberoId", "b1")).thenThrow(new ResourceAccessException("apagada"));

        assertThrows(ResourceAccessException.class, () -> barberoService.eliminar("b1"));
        verify(barberoRepository, never()).delete(any());
    }
}
