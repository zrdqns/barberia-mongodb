package com.barberia.citas.dto;

import com.barberia.citas.model.EstadoCita;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

// Datos que la API devuelve de una cita, con lo necesario
// del cliente, el servicio y el barbero para mostrarla.
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Una cita con la copia de los datos del cliente, el servicio y el barbero")
public class CitaDTOResponse {

    @Schema(example = "6735f0c2a1b2c3d4e5f60720")
    private String id;
    @Schema(example = "2026-11-20T10:00:00")
    private LocalDateTime fechaHora;
    @Schema(example = "PROGRAMADA")
    private EstadoCita estado;
    @Schema(example = "Prefiere máquina número 2")
    private String observaciones;

    @Schema(description = "null si el cliente se eliminó; la cita conserva su nombre", example = "6735f0c2a1b2c3d4e5f60730")
    private String clienteId;
    @Schema(example = "Carlos Ramírez")
    private String clienteNombre;

    @Schema(example = "6735f0c2a1b2c3d4e5f60718")
    private String servicioId;
    @Schema(example = "Corte clásico")
    private String servicioNombre;
    @Schema(description = "Precio con que se agendó", example = "25000")
    private int precio;
    @Schema(example = "30")
    private int duracionMinutos;

    @Schema(example = "6735f0c2a1b2c3d4e5f6071a")
    private String barberoId;
    @Schema(example = "Andrés Rojas")
    private String barberoNombre;
}
