package com.barberia.catalogo.controller;

import com.barberia.catalogo.dto.MensajeDTO;
import com.barberia.catalogo.dto.ServicioDTORequest;
import com.barberia.catalogo.dto.ServicioDTOResponse;
import com.barberia.catalogo.model.Servicio;
import com.barberia.catalogo.service.ServicioService;
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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/servicios")
@RequiredArgsConstructor
@Tag(name = "Servicios", description = "Lo que ofrece la barbería, con precio y duración (colección servicios)")
public class ServicioController {

    private final ServicioService servicioService;

    // POST /api/servicios
    @PostMapping
    @Operation(summary = "Crear un servicio", description = "Solo ADMIN. El nombre no se puede repetir.",
            security = @SecurityRequirement(name = "bearer"))
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Servicio creado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos: el cuerpo dice qué campo falló",
                    content = @Content(schema = @Schema(implementation = MensajeDTO.class))),
            @ApiResponse(responseCode = "401", description = "Falta el token o venció", content = @Content),
            @ApiResponse(responseCode = "403", description = "El usuario no es ADMIN", content = @Content),
            @ApiResponse(responseCode = "409", description = "Ya existe un servicio con ese nombre",
                    content = @Content(schema = @Schema(implementation = MensajeDTO.class)))
    })
    public ResponseEntity<ServicioDTOResponse> crear(@Valid @RequestBody ServicioDTORequest request) {
        Servicio creado = servicioService.crear(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(aRespuesta(creado));
    }

    // GET /api/servicios
    // El mismo recurso se puede pedir en JSON o en XML con el header Accept.
    @GetMapping(produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    @Operation(summary = "Listar los servicios",
            description = "Público. Del más antiguo al más reciente. Responde JSON o XML según el header Accept.")
    @ApiResponse(responseCode = "200", description = "Lista de servicios, activos e inactivos")
    public ResponseEntity<List<ServicioDTOResponse>> listar() {
        return ResponseEntity.ok(servicioService.listar().stream().map(this::aRespuesta).toList());
    }

    // GET /api/servicios/{id}
    @GetMapping(value = "/{id}", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    @Operation(summary = "Consultar un servicio", description = "Público. api-citas lo usa al agendar una cita.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "El servicio"),
            @ApiResponse(responseCode = "404", description = "No existe un servicio con ese ID",
                    content = @Content(schema = @Schema(implementation = MensajeDTO.class)))
    })
    public ResponseEntity<ServicioDTOResponse> buscarPorId(
            @Parameter(description = "ObjectId del servicio", example = "6735f0c2a1b2c3d4e5f60718") @PathVariable String id) {
        return ResponseEntity.ok(aRespuesta(servicioService.buscarPorId(id)));
    }

    // PUT /api/servicios/{id}
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar un servicio",
            description = "Solo ADMIN. Reemplaza los datos; si no se envía 'activo', conserva el que tenía. "
                    + "Las citas ya agendadas conservan el precio y la duración con que se agendaron.",
            security = @SecurityRequirement(name = "bearer"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Servicio actualizado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos",
                    content = @Content(schema = @Schema(implementation = MensajeDTO.class))),
            @ApiResponse(responseCode = "401", description = "Falta el token o venció", content = @Content),
            @ApiResponse(responseCode = "403", description = "El usuario no es ADMIN", content = @Content),
            @ApiResponse(responseCode = "404", description = "No existe un servicio con ese ID",
                    content = @Content(schema = @Schema(implementation = MensajeDTO.class))),
            @ApiResponse(responseCode = "409", description = "Otro servicio ya usa ese nombre",
                    content = @Content(schema = @Schema(implementation = MensajeDTO.class)))
    })
    public ResponseEntity<ServicioDTOResponse> actualizar(
            @Parameter(description = "ObjectId del servicio", example = "6735f0c2a1b2c3d4e5f60718") @PathVariable String id,
            @Valid @RequestBody ServicioDTORequest request) {

        return ResponseEntity.ok(aRespuesta(servicioService.actualizar(id, request)));
    }

    // DELETE /api/servicios/{id}
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar un servicio",
            description = "Solo ADMIN. Antes le pregunta a api-citas si alguna cita programada lo usa; si la hay, no lo elimina.",
            security = @SecurityRequirement(name = "bearer"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Servicio eliminado de MongoDB"),
            @ApiResponse(responseCode = "401", description = "Falta el token o venció", content = @Content),
            @ApiResponse(responseCode = "403", description = "El usuario no es ADMIN", content = @Content),
            @ApiResponse(responseCode = "404", description = "No existe un servicio con ese ID",
                    content = @Content(schema = @Schema(implementation = MensajeDTO.class))),
            @ApiResponse(responseCode = "409", description = "Tiene citas programadas",
                    content = @Content(schema = @Schema(implementation = MensajeDTO.class))),
            @ApiResponse(responseCode = "503", description = "api-citas no respondió",
                    content = @Content(schema = @Schema(implementation = MensajeDTO.class)))
    })
    public ResponseEntity<MensajeDTO> eliminar(
            @Parameter(description = "ObjectId del servicio", example = "6735f0c2a1b2c3d4e5f60718") @PathVariable String id) {
        servicioService.eliminar(id);

        return ResponseEntity.ok(new MensajeDTO("Servicio eliminado correctamente."));
    }

    // Modelo -> DTO de respuesta
    private ServicioDTOResponse aRespuesta(Servicio servicio) {
        return new ServicioDTOResponse(
                servicio.getId(),
                servicio.getNombre(),
                servicio.getDescripcion(),
                servicio.getPrecio(),
                servicio.getDuracionMinutos(),
                servicio.isActivo()
        );
    }
}
