package com.barberia.citas.controller;

import com.barberia.citas.dto.HorarioDTORequest;
import com.barberia.citas.dto.HorarioDTOResponse;
import com.barberia.citas.dto.MensajeDTO;
import com.barberia.citas.model.DiaHorario;
import com.barberia.citas.service.HorarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/horario")
@RequiredArgsConstructor
@Tag(name = "Horario", description = "Horario de atención: un documento con los siete días embebidos (colección horario)")
public class HorarioController {

    private final HorarioService horarioService;

    // GET /api/horario
    @GetMapping
    @Operation(summary = "Consultar el horario de atención", description = "Público. Los siete días, de lunes a domingo.")
    @ApiResponse(responseCode = "200", description = "Horario de cada día")
    public ResponseEntity<List<HorarioDTOResponse>> listar() {
        return ResponseEntity.ok(horarioService.listar().stream().map(this::aRespuesta).toList());
    }

    // PUT /api/horario/{diaSemana}
    // Cambia la apertura y el cierre de un día; las dos en null lo cierran.
    @PutMapping("/{diaSemana}")
    @Operation(summary = "Cambiar el horario de un día",
            description = "Solo ADMIN. Las dos horas en null cierran ese día. No se permite si deja por fuera citas programadas.",
            security = @SecurityRequirement(name = "bearer"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Horario del día actualizado"),
            @ApiResponse(responseCode = "400", description = "Falta una de las horas o la apertura no es antes del cierre",
                    content = @Content(schema = @Schema(implementation = MensajeDTO.class))),
            @ApiResponse(responseCode = "401", description = "Falta el token o venció", content = @Content),
            @ApiResponse(responseCode = "403", description = "El usuario no es ADMIN", content = @Content),
            @ApiResponse(responseCode = "404", description = "El día no está entre 1 y 7",
                    content = @Content(schema = @Schema(implementation = MensajeDTO.class))),
            @ApiResponse(responseCode = "409", description = "Quedarían citas programadas por fuera del horario",
                    content = @Content(schema = @Schema(implementation = MensajeDTO.class)))
    })
    public ResponseEntity<HorarioDTOResponse> actualizar(
            @Parameter(description = "1 = lunes ... 7 = domingo", example = "6") @PathVariable int diaSemana,
            @RequestBody HorarioDTORequest request) {

        return ResponseEntity.ok(aRespuesta(horarioService.actualizar(diaSemana, request)));
    }

    // Modelo -> DTO de respuesta
    private HorarioDTOResponse aRespuesta(DiaHorario dia) {
        return new HorarioDTOResponse(
                dia.getDiaSemana(),
                HorarioService.DIAS[dia.getDiaSemana()],
                dia.abierto(),
                dia.getApertura(),
                dia.getCierre()
        );
    }
}
