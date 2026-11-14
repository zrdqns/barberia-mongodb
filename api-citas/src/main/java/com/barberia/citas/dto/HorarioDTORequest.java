package com.barberia.citas.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;

// Cuerpo del PUT /api/horario/{diaSemana}. Las dos horas en null: ese día no se atiende.
@Getter
@Setter
@Schema(description = "Apertura y cierre de un día; las dos en null lo cierran")
public class HorarioDTORequest {

    @JsonFormat(pattern = "HH:mm")
    @Schema(type = "string", example = "09:00")
    private LocalTime apertura;
    @JsonFormat(pattern = "HH:mm")
    @Schema(type = "string", example = "19:00")
    private LocalTime cierre;
}
