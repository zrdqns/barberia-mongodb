package com.barberia.citas.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;

// El horario de un día de la semana
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Horario de un día de la semana")
public class HorarioDTOResponse {

    @Schema(description = "1 = lunes ... 7 = domingo", example = "1")
    private int diaSemana;
    @Schema(example = "lunes")
    private String dia;
    @Schema(example = "true")
    private boolean abierto;
    @JsonFormat(pattern = "HH:mm")
    @Schema(type = "string", example = "09:00")
    private LocalTime apertura;
    @JsonFormat(pattern = "HH:mm")
    @Schema(type = "string", example = "19:00")
    private LocalTime cierre;
}
