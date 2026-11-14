package com.barberia.citas.service;

import com.barberia.citas.dto.HorarioDTORequest;
import com.barberia.citas.model.Cita;
import com.barberia.citas.model.DiaHorario;
import com.barberia.citas.model.EstadoCita;
import com.barberia.citas.model.HorarioSemanal;
import com.barberia.citas.repository.CitaRepository;
import com.barberia.citas.repository.HorarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class HorarioService {

    // Índice 1 = lunes ... 7 = domingo
    public static final String[] DIAS = {"", "lunes", "martes", "miércoles", "jueves", "viernes", "sábado", "domingo"};
    private static final String[] LOS_DIAS = {"", "lunes", "martes", "miércoles", "jueves", "viernes", "sábados", "domingos"};
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("H:mm");
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("d/MM 'a las' H:mm");

    // Un solo documento en la colección "horario", con los siete días embebidos
    private final HorarioRepository horarioRepository;
    private final CitaRepository citaRepository;

    public List<DiaHorario> listar() {
        return semana().getDias().stream()
                .sorted(Comparator.comparingInt(DiaHorario::getDiaSemana))
                .toList();
    }

    // Cambia el horario de un día. Apertura y cierre en null: ese día no se atiende.
    public synchronized DiaHorario actualizar(int diaSemana, HorarioDTORequest request) {
        HorarioSemanal semana = semana();
        DiaHorario dia = buscarDia(semana, diaSemana);
        LocalTime apertura = request.getApertura();
        LocalTime cierre = request.getCierre();

        if ((apertura == null) != (cierre == null)) {
            throw new IllegalArgumentException("Indique la apertura y el cierre, o deje los dos vacíos para no atender ese día.");
        }
        if (apertura != null && !apertura.isBefore(cierre)) {
            throw new IllegalArgumentException("La apertura debe ser antes del cierre.");
        }

        // Las citas programadas de ese día de la semana tienen que seguir cabiendo en el horario
        DiaHorario nuevo = new DiaHorario(diaSemana, apertura, cierre);
        List<Cita> porFuera = citaRepository.findByEstadoAndFechaHoraAfter(EstadoCita.PROGRAMADA, LocalDateTime.now()).stream()
                .filter(cita -> cita.getFechaHora().getDayOfWeek().getValue() == diaSemana)
                .filter(cita -> !cabe(nuevo, cita.getFechaHora(), cita.calcularFin()))
                .sorted(Comparator.comparing(Cita::getFechaHora))
                .toList();
        if (!porFuera.isEmpty()) {
            throw new IllegalStateException("Con ese horario quedarían por fuera " + porFuera.size()
                    + (porFuera.size() == 1 ? " cita programada" : " citas programadas")
                    + ", la primera el " + porFuera.get(0).getFechaHora().format(FECHA)
                    + ". Muévalas o cancélelas antes de cambiar el horario.");
        }

        // Se cambia el día dentro de la semana y se guarda el documento completo
        dia.setApertura(apertura);
        dia.setCierre(cierre);
        horarioRepository.save(semana);
        return dia;
    }

    // Una cita debe empezar y terminar dentro del horario de su día
    public void validarAtencion(LocalDateTime inicio, LocalDateTime fin) {
        int numero = inicio.getDayOfWeek().getValue();
        DiaHorario dia = buscarDia(semana(), numero);

        if (!dia.abierto()) {
            throw new IllegalArgumentException("La barbería no atiende los " + LOS_DIAS[numero] + ".");
        }
        if (!cabe(dia, inicio, fin)) {
            long minutos = Duration.between(inicio, fin).toMinutes();
            throw new IllegalArgumentException("Los " + LOS_DIAS[numero] + " la barbería atiende de "
                    + dia.getApertura().format(HORA) + " a " + dia.getCierre().format(HORA)
                    + ". Una cita de " + minutos + " minutos debe empezar entre las "
                    + dia.getApertura().format(HORA) + " y las "
                    + dia.getCierre().minusMinutes(minutos).format(HORA) + ".");
        }
    }

    private HorarioSemanal semana() {
        return horarioRepository.findById(HorarioSemanal.ID)
                .orElseThrow(() -> new IllegalStateException("El horario de atención no está configurado."));
    }

    private DiaHorario buscarDia(HorarioSemanal semana, int diaSemana) {
        return semana.getDias().stream()
                .filter(dia -> dia.getDiaSemana() == diaSemana)
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("El día de la semana va de 1 (lunes) a 7 (domingo)."));
    }

    private boolean cabe(DiaHorario dia, LocalDateTime inicio, LocalDateTime fin) {
        return dia.abierto()
                && fin.toLocalDate().equals(inicio.toLocalDate())
                && !inicio.toLocalTime().isBefore(dia.getApertura())
                && !fin.toLocalTime().isAfter(dia.getCierre());
    }
}
