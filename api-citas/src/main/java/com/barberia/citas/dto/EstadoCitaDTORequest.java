package com.barberia.citas.dto;

import com.barberia.citas.model.EstadoCita;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

// Cuerpo del PATCH: solo cambia el estado de la cita.
@Getter
@Setter
@Schema(description = "Nuevo estado de la cita")
public class EstadoCitaDTORequest {

    @NotNull(message = "El estado debe ser COMPLETADA o CANCELADA.")
    @Schema(description = "COMPLETADA o CANCELADA", example = "CANCELADA")
    private EstadoCita estado;
}
