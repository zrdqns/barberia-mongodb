package com.barberia.citas.service;

import com.barberia.citas.dto.HorarioDTORequest;
import com.barberia.citas.model.Cita;
import com.barberia.citas.model.DiaHorario;
import com.barberia.citas.model.EstadoCita;
import com.barberia.citas.model.HorarioSemanal;
import com.barberia.citas.repository.CitaRepository;
import com.barberia.citas.repository.HorarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HorarioServiceTest {

    @Mock
    private HorarioRepository horarioRepository;

    @Mock
    private CitaRepository citaRepository;

    @InjectMocks
    private HorarioService horarioService;

    private HorarioSemanal semana;

    @BeforeEach
    void lunesASabadoDe9a19() {
        List<DiaHorario> dias = new ArrayList<>();
        for (int dia = 1; dia <= 6; dia++) {
            dias.add(new DiaHorario(dia, LocalTime.of(9, 0), LocalTime.of(19, 0)));
        }
        dias.add(new DiaHorario(7, null, null));
        semana = new HorarioSemanal(HorarioSemanal.ID, dias);
        when(horarioRepository.findById(HorarioSemanal.ID)).thenReturn(java.util.Optional.of(semana));
    }

    private static LocalDateTime proximo(DayOfWeek dia, int hora, int minuto) {
        return LocalDate.now().with(TemporalAdjusters.next(dia)).atTime(hora, minuto);
    }

    @Test
    void losDomingosNoSeAtiende() {
        LocalDateTime inicio = proximo(DayOfWeek.SUNDAY, 10, 0);

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> horarioService.validarAtencion(inicio, inicio.plusMinutes(30)));

        assertEquals("La barbería no atiende los domingos.", error.getMessage());
    }

    @Test
    void unaCitaQueTerminaDespuesDelCierreNoCabe() {
        LocalDateTime inicio = proximo(DayOfWeek.FRIDAY, 18, 45);

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> horarioService.validarAtencion(inicio, inicio.plusMinutes(30)));

        assertTrue(error.getMessage().contains("debe empezar entre las 9:00 y las 18:30"));
    }

    @Test
    void cambiarUnDiaGuardaLaSemanaCompleta() {
        HorarioDTORequest request = new HorarioDTORequest();
        request.setApertura(LocalTime.of(10, 0));
        request.setCierre(LocalTime.of(14, 0));

        DiaHorario sabado = horarioService.actualizar(6, request);

        assertEquals(LocalTime.of(10, 0), sabado.getApertura());
        verify(horarioRepository).save(semana);
    }

    @Test
    void noSeCierraUnDiaConCitasProgramadas() {
        Cita cita = new Cita();
        cita.setEstado(EstadoCita.PROGRAMADA);
        cita.setFechaHora(proximo(DayOfWeek.SATURDAY, 11, 0));
        cita.setDuracionMinutos(30);
        when(citaRepository.findByEstadoAndFechaHoraAfter(any(), any())).thenReturn(List.of(cita));

        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> horarioService.actualizar(6, new HorarioDTORequest()));

        assertTrue(error.getMessage().startsWith("Con ese horario quedarían por fuera 1 cita programada"));
        verify(horarioRepository, never()).save(any());
    }
}
