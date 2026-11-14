package com.barberia.citas.service;

import com.barberia.citas.dto.ClienteDTORequest;
import com.barberia.citas.model.Cita;
import com.barberia.citas.model.Cliente;
import com.barberia.citas.model.EstadoCita;
import com.barberia.citas.repository.CitaRepository;
import com.barberia.citas.repository.ClienteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private CitaRepository citaRepository;

    @InjectMocks
    private ClienteService clienteService;

    private final Cliente mateo = new Cliente("c2", "Mateo Herrera", "1087654321", "3012345678", null);

    private static Cita citaDe(String clienteId, String nombre) {
        Cita cita = new Cita();
        cita.setClienteId(clienteId);
        cita.setClienteNombre(nombre);
        return cita;
    }

    @Test
    void noSeEliminaUnClienteConCitasProgramadas() {
        when(clienteRepository.findById("c2")).thenReturn(Optional.of(mateo));
        when(citaRepository.existsByClienteIdAndEstado("c2", EstadoCita.PROGRAMADA)).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> clienteService.eliminar("c2"));
        verify(clienteRepository, never()).delete(any());
    }

    @Test
    void alEliminarSusCitasAnterioresConservanElNombre() {
        Cita anterior = citaDe("c2", "Mateo Herrera");
        when(clienteRepository.findById("c2")).thenReturn(Optional.of(mateo));
        when(citaRepository.existsByClienteIdAndEstado("c2", EstadoCita.PROGRAMADA)).thenReturn(false);
        when(citaRepository.findByClienteId("c2")).thenReturn(List.of(anterior));

        clienteService.eliminar("c2");

        assertNull(anterior.getClienteId());
        assertEquals("Mateo Herrera", anterior.getClienteNombre());
        verify(citaRepository).saveAll(List.of(anterior));
        verify(clienteRepository).delete(mateo);
    }

    @Test
    void siCambiaElNombreSusCitasMuestranElNuevo() {
        Cita suya = citaDe("c2", "Mateo Herrera");
        ClienteDTORequest request = new ClienteDTORequest();
        request.setNombre("Mateo Herrera Gil");
        request.setDocumento("1087654321");
        request.setTelefono("3012345678");
        when(clienteRepository.findById("c2")).thenReturn(Optional.of(mateo));
        when(clienteRepository.existsByDocumentoAndIdNot("1087654321", "c2")).thenReturn(false);
        when(clienteRepository.save(mateo)).thenReturn(mateo);
        when(citaRepository.findByClienteId("c2")).thenReturn(List.of(suya));

        clienteService.actualizar("c2", request);

        assertEquals("Mateo Herrera Gil", suya.getClienteNombre());
        verify(citaRepository).saveAll(List.of(suya));
    }

    @Test
    void noSeRepiteElDocumento() {
        ClienteDTORequest request = new ClienteDTORequest();
        request.setNombre("Otro");
        request.setDocumento("1098765432");
        request.setTelefono("3000000000");
        when(clienteRepository.existsByDocumentoAndIdNot("1098765432", null)).thenReturn(true);

        IllegalStateException error = assertThrows(IllegalStateException.class, () -> clienteService.crear(request));

        assertEquals("Ya existe un cliente con el documento 1098765432.", error.getMessage());
    }
}
