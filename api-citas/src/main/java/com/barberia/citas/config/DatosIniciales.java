package com.barberia.citas.config;

import com.barberia.citas.model.Cliente;
import com.barberia.citas.model.DiaHorario;
import com.barberia.citas.model.HorarioSemanal;
import com.barberia.citas.repository.ClienteRepository;
import com.barberia.citas.repository.HorarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

// Datos de demostración. Cada colección se llena solo si está vacía, para no repetir
// documentos ni revivir los que se hayan eliminado.
@Component
@RequiredArgsConstructor
public class DatosIniciales implements CommandLineRunner {

    private final ClienteRepository clienteRepository;
    private final HorarioRepository horarioRepository;

    @Override
    public void run(String... args) {
        if (clienteRepository.count() == 0) {
            clienteRepository.saveAll(List.of(
                    new Cliente(null, "Carlos Ramírez", "1098765432", "3001234567", "carlos.ramirez@correo.com"),
                    new Cliente(null, "Mateo Herrera", "1087654321", "3012345678", "mateo.herrera@correo.com"),
                    new Cliente(null, "Santiago López", "1076543210", "3023456789", null)));
        }

        // De lunes a sábado de 9:00 a. m. a 7:00 p. m.; el domingo no se atiende
        if (horarioRepository.count() == 0) {
            List<DiaHorario> dias = new ArrayList<>();
            for (int dia = 1; dia <= 6; dia++) {
                dias.add(new DiaHorario(dia, LocalTime.of(9, 0), LocalTime.of(19, 0)));
            }
            dias.add(new DiaHorario(7, null, null));
            horarioRepository.save(new HorarioSemanal(HorarioSemanal.ID, dias));
        }
    }
}
