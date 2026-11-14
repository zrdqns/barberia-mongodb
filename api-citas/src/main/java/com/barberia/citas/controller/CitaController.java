package com.barberia.citas.controller;

import com.barberia.citas.dto.CitaDTORequest;
import com.barberia.citas.dto.CitaDTOResponse;
import com.barberia.citas.dto.EstadoCitaDTORequest;
import com.barberia.citas.dto.MensajeDTO;
import com.barberia.citas.model.Cita;
import com.barberia.citas.model.EstadoCita;
import com.barberia.citas.service.CitaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/citas")
@RequiredArgsConstructor
@Tag(name = "Citas", description = "Agenda de la barbería (colección citas). Requiere token de ADMIN o USER.")
@SecurityRequirement(name = "bearer")
public class CitaController {

    private final CitaService citaService;

    // POST /api/citas
    // Agenda la cita. El service consulta el servicio y el barbero en api-catalogo.
    @PostMapping
    @Operation(summary = "Agendar una cita",
            description = "Consulta el servicio y el barbero en api-catalogo y guarda una copia de su nombre, precio y "
                    + "duración. Revisa el horario de atención y que ni el barbero ni el cliente tengan otra cita a esa hora.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Cita agendada en estado PROGRAMADA"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos, fecha pasada o fuera del horario",
                    content = @Content(schema = @Schema(implementation = MensajeDTO.class))),
            @ApiResponse(responseCode = "401", description = "Falta el token o venció", content = @Content),
            @ApiResponse(responseCode = "404", description = "El cliente, el servicio o el barbero no existen",
                    content = @Content(schema = @Schema(implementation = MensajeDTO.class))),
            @ApiResponse(responseCode = "409", description = "Servicio o barbero inactivo, o la hora se cruza con otra cita",
                    content = @Content(schema = @Schema(implementation = MensajeDTO.class))),
            @ApiResponse(responseCode = "503", description = "api-catalogo no respondió",
                    content = @Content(schema = @Schema(implementation = MensajeDTO.class)))
    })
    public ResponseEntity<CitaDTOResponse> crear(@Valid @RequestBody CitaDTORequest request) {
        Cita creada = citaService.crear(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(aRespuesta(creada));
    }

    // GET /api/citas
    // api-catalogo usa barberoId o servicioId con estado=PROGRAMADA antes de eliminar.
    @GetMapping
    @Operation(summary = "Listar las citas",
            description = "Filtros opcionales y combinables. Ordenadas de la más próxima a la más lejana. "
                    + "api-catalogo usa barberoId o servicioId con estado=PROGRAMADA antes de eliminar.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Citas que cumplen los filtros"),
            @ApiResponse(responseCode = "400", description = "Un filtro no tiene el formato esperado",
                    content = @Content(schema = @Schema(implementation = MensajeDTO.class))),
            @ApiResponse(responseCode = "401", description = "Falta el token o venció", content = @Content)
    })
    public ResponseEntity<List<CitaDTOResponse>> listar(
            @Parameter(description = "ObjectId del cliente") @RequestParam(required = false) String clienteId,
            @Parameter(description = "ObjectId del barbero") @RequestParam(required = false) String barberoId,
            @Parameter(description = "ObjectId del servicio") @RequestParam(required = false) String servicioId,
            @Parameter(description = "PROGRAMADA, COMPLETADA o CANCELADA") @RequestParam(required = false) EstadoCita estado,
            @Parameter(description = "Día de la cita", example = "2026-11-20")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {

        return ResponseEntity.ok(citaService.listar(clienteId, barberoId, servicioId, estado, fecha).stream()
                .map(this::aRespuesta)
                .toList());
    }

    // GET /api/citas/{id}
    @GetMapping("/{id}")
    @Operation(summary = "Consultar una cita")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "La cita"),
            @ApiResponse(responseCode = "401", description = "Falta el token o venció", content = @Content),
            @ApiResponse(responseCode = "404", description = "No existe una cita con ese ID",
                    content = @Content(schema = @Schema(implementation = MensajeDTO.class)))
    })
    public ResponseEntity<CitaDTOResponse> buscarPorId(
            @Parameter(description = "ObjectId de la cita", example = "6735f0c2a1b2c3d4e5f60720") @PathVariable String id) {
        return ResponseEntity.ok(aRespuesta(citaService.buscarPorId(id)));
    }

    // PUT /api/citas/{id}
    // Reemplaza los datos de la cita (reprogramar, cambiar servicio o barbero).
    @PutMapping("/{id}")
    @Operation(summary = "Reprogramar una cita",
            description = "Solo citas PROGRAMADAS. Si el servicio no cambia, conserva el precio y la duración con que se agendó.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cita actualizada"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos, fecha pasada o fuera del horario",
                    content = @Content(schema = @Schema(implementation = MensajeDTO.class))),
            @ApiResponse(responseCode = "401", description = "Falta el token o venció", content = @Content),
            @ApiResponse(responseCode = "404", description = "La cita, el cliente, el servicio o el barbero no existen",
                    content = @Content(schema = @Schema(implementation = MensajeDTO.class))),
            @ApiResponse(responseCode = "409", description = "La cita ya se cerró o la hora se cruza con otra cita",
                    content = @Content(schema = @Schema(implementation = MensajeDTO.class))),
            @ApiResponse(responseCode = "503", description = "api-catalogo no respondió",
                    content = @Content(schema = @Schema(implementation = MensajeDTO.class)))
    })
    public ResponseEntity<CitaDTOResponse> actualizar(
            @Parameter(description = "ObjectId de la cita", example = "6735f0c2a1b2c3d4e5f60720") @PathVariable String id,
            @Valid @RequestBody CitaDTORequest request) {

        return ResponseEntity.ok(aRespuesta(citaService.actualizar(id, request)));
    }

    // PATCH /api/citas/{id}/estado
    // Modificación parcial: solo cambia el estado (completar o cancelar).
    @PatchMapping("/{id}/estado")
    @Operation(summary = "Completar o cancelar una cita",
            description = "Cambia solo el estado. Una cita se completa desde la hora en que empieza.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estado cambiado"),
            @ApiResponse(responseCode = "400", description = "El estado no es COMPLETADA ni CANCELADA",
                    content = @Content(schema = @Schema(implementation = MensajeDTO.class))),
            @ApiResponse(responseCode = "401", description = "Falta el token o venció", content = @Content),
            @ApiResponse(responseCode = "404", description = "No existe una cita con ese ID",
                    content = @Content(schema = @Schema(implementation = MensajeDTO.class))),
            @ApiResponse(responseCode = "409", description = "La cita ya se cerró o todavía no empieza",
                    content = @Content(schema = @Schema(implementation = MensajeDTO.class)))
    })
    public ResponseEntity<CitaDTOResponse> cambiarEstado(
            @Parameter(description = "ObjectId de la cita", example = "6735f0c2a1b2c3d4e5f60720") @PathVariable String id,
            @Valid @RequestBody EstadoCitaDTORequest request) {

        return ResponseEntity.ok(aRespuesta(citaService.cambiarEstado(id, request.getEstado())));
    }

    // DELETE /api/citas/{id}
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar una cita", description = "La borra de MongoDB.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cita eliminada"),
            @ApiResponse(responseCode = "401", description = "Falta el token o venció", content = @Content),
            @ApiResponse(responseCode = "404", description = "No existe una cita con ese ID",
                    content = @Content(schema = @Schema(implementation = MensajeDTO.class)))
    })
    public ResponseEntity<MensajeDTO> eliminar(
            @Parameter(description = "ObjectId de la cita", example = "6735f0c2a1b2c3d4e5f60720") @PathVariable String id) {
        citaService.eliminar(id);

        return ResponseEntity.ok(new MensajeDTO("Cita eliminada correctamente."));
    }

    // Modelo -> DTO de respuesta
    private CitaDTOResponse aRespuesta(Cita cita) {
        return new CitaDTOResponse(
                cita.getId(),
                cita.getFechaHora(),
                cita.getEstado(),
                cita.getObservaciones(),
                cita.getClienteId(),
                cita.getClienteNombre(),
                cita.getServicioId(),
                cita.getServicioNombre(),
                cita.getPrecio(),
                cita.getDuracionMinutos(),
                cita.getBarberoId(),
                cita.getBarberoNombre()
        );
    }
}
