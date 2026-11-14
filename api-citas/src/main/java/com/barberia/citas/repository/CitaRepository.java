package com.barberia.citas.repository;

import com.barberia.citas.model.Cita;
import com.barberia.citas.model.EstadoCita;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDateTime;
import java.util.List;

// Spring Data MongoDB genera las consultas a partir del nombre de cada método.
// El listado con filtros opcionales está en CitaService, con MongoTemplate.
public interface CitaRepository extends MongoRepository<Cita, String> {

    // Citas en un estado que empiezan dentro de un intervalo
    List<Cita> findByEstadoAndFechaHoraBetween(EstadoCita estado, LocalDateTime desde, LocalDateTime hasta);

    // Citas en un estado que empiezan después de un momento
    List<Cita> findByEstadoAndFechaHoraAfter(EstadoCita estado, LocalDateTime desde);

    boolean existsByClienteIdAndEstado(String clienteId, EstadoCita estado);

    List<Cita> findByClienteId(String clienteId);
}
