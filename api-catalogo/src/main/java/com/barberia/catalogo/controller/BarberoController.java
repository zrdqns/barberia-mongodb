package com.barberia.catalogo.controller;

import com.barberia.catalogo.dto.BarberoDTORequest;
import com.barberia.catalogo.dto.BarberoDTOResponse;
import com.barberia.catalogo.dto.MensajeDTO;
import com.barberia.catalogo.model.Barbero;
import com.barberia.catalogo.service.BarberoService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/barberos")
@RequiredArgsConstructor
@Tag(name = "Barberos", description = "Quienes atienden en la barbería (colección barberos)")
public class BarberoController {

    private final BarberoService barberoService;

    // POST /api/barberos
    @PostMapping
    @Operation(summary = "Crear un barbero", description = "Solo ADMIN. El documento no se puede repetir.",
            security = @SecurityRequirement(name = "bearer"))
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Barbero creado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos: el cuerpo dice qué campo falló",
                    content = @Content(schema = @Schema(implementation = MensajeDTO.class))),
            @ApiResponse(responseCode = "401", description = "Falta el token o venció", content = @Content),
            @ApiResponse(responseCode = "403", description = "El usuario no es ADMIN", content = @Content),
            @ApiResponse(responseCode = "409", description = "Ya existe un barbero con ese documento",
                    content = @Content(schema = @Schema(implementation = MensajeDTO.class)))
    })
    public ResponseEntity<BarberoDTOResponse> crear(@Valid @RequestBody BarberoDTORequest request) {
        Barbero creado = barberoService.crear(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(aRespuesta(creado));
    }

    // GET /api/barberos
    @GetMapping
    @Operation(summary = "Listar los barberos", description = "Público. Del más antiguo al más reciente.")
    @ApiResponse(responseCode = "200", description = "Lista de barberos, activos e inactivos")
    public ResponseEntity<List<BarberoDTOResponse>> listar() {
        return ResponseEntity.ok(barberoService.listar().stream().map(this::aRespuesta).toList());
    }

    // GET /api/barberos/{id}
    @GetMapping("/{id}")
    @Operation(summary = "Consultar un barbero", description = "Público. api-citas lo usa al agendar una cita.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "El barbero"),
            @ApiResponse(responseCode = "404", description = "No existe un barbero con ese ID",
                    content = @Content(schema = @Schema(implementation = MensajeDTO.class)))
    })
    public ResponseEntity<BarberoDTOResponse> buscarPorId(
            @Parameter(description = "ObjectId del barbero", example = "6735f0c2a1b2c3d4e5f6071a") @PathVariable String id) {
        return ResponseEntity.ok(aRespuesta(barberoService.buscarPorId(id)));
    }

    // PUT /api/barberos/{id}
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar un barbero",
            description = "Solo ADMIN. Reemplaza los datos; si no se envía 'activo', conserva el que tenía.",
            security = @SecurityRequirement(name = "bearer"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Barbero actualizado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos",
                    content = @Content(schema = @Schema(implementation = MensajeDTO.class))),
            @ApiResponse(responseCode = "401", description = "Falta el token o venció", content = @Content),
            @ApiResponse(responseCode = "403", description = "El usuario no es ADMIN", content = @Content),
            @ApiResponse(responseCode = "404", description = "No existe un barbero con ese ID",
                    content = @Content(schema = @Schema(implementation = MensajeDTO.class))),
            @ApiResponse(responseCode = "409", description = "Otro barbero ya tiene ese documento",
                    content = @Content(schema = @Schema(implementation = MensajeDTO.class)))
    })
    public ResponseEntity<BarberoDTOResponse> actualizar(
            @Parameter(description = "ObjectId del barbero", example = "6735f0c2a1b2c3d4e5f6071a") @PathVariable String id,
            @Valid @RequestBody BarberoDTORequest request) {

        return ResponseEntity.ok(aRespuesta(barberoService.actualizar(id, request)));
    }

    // DELETE /api/barberos/{id}
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar un barbero",
            description = "Solo ADMIN. Antes le pregunta a api-citas si tiene citas programadas; si las tiene, no lo elimina.",
            security = @SecurityRequirement(name = "bearer"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Barbero eliminado de MongoDB"),
            @ApiResponse(responseCode = "401", description = "Falta el token o venció", content = @Content),
            @ApiResponse(responseCode = "403", description = "El usuario no es ADMIN", content = @Content),
            @ApiResponse(responseCode = "404", description = "No existe un barbero con ese ID",
                    content = @Content(schema = @Schema(implementation = MensajeDTO.class))),
            @ApiResponse(responseCode = "409", description = "Tiene citas programadas",
                    content = @Content(schema = @Schema(implementation = MensajeDTO.class))),
            @ApiResponse(responseCode = "503", description = "api-citas no respondió",
                    content = @Content(schema = @Schema(implementation = MensajeDTO.class)))
    })
    public ResponseEntity<MensajeDTO> eliminar(
            @Parameter(description = "ObjectId del barbero", example = "6735f0c2a1b2c3d4e5f6071a") @PathVariable String id) {
        barberoService.eliminar(id);

        return ResponseEntity.ok(new MensajeDTO("Barbero eliminado correctamente."));
    }

    // Modelo -> DTO de respuesta
    private BarberoDTOResponse aRespuesta(Barbero barbero) {
        return new BarberoDTOResponse(
                barbero.getId(),
                barbero.getNombre(),
                barbero.getDocumento(),
                barbero.getTelefono(),
                barbero.getEspecialidad(),
                barbero.isActivo()
        );
    }
}
