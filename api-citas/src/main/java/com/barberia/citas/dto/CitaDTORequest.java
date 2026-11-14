package com.barberia.citas.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

// Para agendar una cita no se envían los objetos completos:
// basta con los IDs del cliente, el servicio y el barbero.
@Getter
@Setter
@Schema(description = "Datos para agendar o reprogramar una cita")
public class CitaDTORequest {

    @NotBlank(message = "Indique el cliente de la cita.")
    @Schema(description = "ObjectId de un cliente de api-citas", example = "6735f0c2a1b2c3d4e5f60730")
    private String clienteId;

    @NotBlank(message = "Indique el servicio de la cita.")
    @Schema(description = "ObjectId de un servicio de api-catalogo", example = "6735f0c2a1b2c3d4e5f60718")
    private String servicioId;

    @NotBlank(message = "Indique el barbero de la cita.")
    @Schema(description = "ObjectId de un barbero de api-catalogo", example = "6735f0c2a1b2c3d4e5f6071a")
    private String barberoId;

    @NotNull(message = "La fecha y la hora de la cita son obligatorias.")
    @Schema(description = "Fecha y hora local, sin zona horaria", example = "2026-11-20T10:00")
    private LocalDateTime fechaHora;

    @Size(max = 500, message = "Las observaciones no pueden pasar de 500 caracteres.")
    @Schema(example = "Prefiere máquina número 2")
    private String observaciones;
}
