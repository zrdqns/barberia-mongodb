package com.barberia.citas.controller;

import com.barberia.citas.dto.ClienteDTORequest;
import com.barberia.citas.dto.ClienteDTOResponse;
import com.barberia.citas.dto.MensajeDTO;
import com.barberia.citas.model.Cliente;
import com.barberia.citas.service.ClienteService;
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
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
@Tag(name = "Clientes", description = "Clientes de la barbería (colección clientes). Requiere token de ADMIN o USER.")
@SecurityRequirement(name = "bearer")
public class ClienteController {

    private final ClienteService clienteService;

    // POST /api/clientes
    @PostMapping
    @Operation(summary = "Crear un cliente", description = "El documento no se puede repetir.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Cliente creado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos: el cuerpo dice qué campo falló",
                    content = @Content(schema = @Schema(implementation = MensajeDTO.class))),
            @ApiResponse(responseCode = "401", description = "Falta el token o venció", content = @Content),
            @ApiResponse(responseCode = "409", description = "Ya existe un cliente con ese documento",
                    content = @Content(schema = @Schema(implementation = MensajeDTO.class)))
    })
    public ResponseEntity<ClienteDTOResponse> crear(@Valid @RequestBody ClienteDTORequest request) {
        Cliente creado = clienteService.crear(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(aRespuesta(creado));
    }

    // GET /api/clientes
    @GetMapping
    @Operation(summary = "Listar los clientes", description = "Del más antiguo al más reciente.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de clientes"),
            @ApiResponse(responseCode = "401", description = "Falta el token o venció", content = @Content)
    })
    public ResponseEntity<List<ClienteDTOResponse>> listar() {
        return ResponseEntity.ok(clienteService.listar().stream().map(this::aRespuesta).toList());
    }

    // GET /api/clientes/{id}
    @GetMapping("/{id}")
    @Operation(summary = "Consultar un cliente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "El cliente"),
            @ApiResponse(responseCode = "401", description = "Falta el token o venció", content = @Content),
            @ApiResponse(responseCode = "404", description = "No existe un cliente con ese ID",
                    content = @Content(schema = @Schema(implementation = MensajeDTO.class)))
    })
    public ResponseEntity<ClienteDTOResponse> buscarPorId(
            @Parameter(description = "ObjectId del cliente", example = "6735f0c2a1b2c3d4e5f60730") @PathVariable String id) {
        return ResponseEntity.ok(aRespuesta(clienteService.buscarPorId(id)));
    }

    // PUT /api/clientes/{id}
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar un cliente", description = "Si cambia el nombre, sus citas muestran el nuevo.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cliente actualizado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos",
                    content = @Content(schema = @Schema(implementation = MensajeDTO.class))),
            @ApiResponse(responseCode = "401", description = "Falta el token o venció", content = @Content),
            @ApiResponse(responseCode = "404", description = "No existe un cliente con ese ID",
                    content = @Content(schema = @Schema(implementation = MensajeDTO.class))),
            @ApiResponse(responseCode = "409", description = "Otro cliente ya tiene ese documento",
                    content = @Content(schema = @Schema(implementation = MensajeDTO.class)))
    })
    public ResponseEntity<ClienteDTOResponse> actualizar(
            @Parameter(description = "ObjectId del cliente", example = "6735f0c2a1b2c3d4e5f60730") @PathVariable String id,
            @Valid @RequestBody ClienteDTORequest request) {

        return ResponseEntity.ok(aRespuesta(clienteService.actualizar(id, request)));
    }

    // DELETE /api/clientes/{id}
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar un cliente",
            description = "Solo ADMIN. No se puede si tiene citas programadas; sus citas anteriores conservan su nombre.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cliente eliminado de MongoDB"),
            @ApiResponse(responseCode = "401", description = "Falta el token o venció", content = @Content),
            @ApiResponse(responseCode = "403", description = "El usuario no es ADMIN", content = @Content),
            @ApiResponse(responseCode = "404", description = "No existe un cliente con ese ID",
                    content = @Content(schema = @Schema(implementation = MensajeDTO.class))),
            @ApiResponse(responseCode = "409", description = "Tiene citas programadas",
                    content = @Content(schema = @Schema(implementation = MensajeDTO.class)))
    })
    public ResponseEntity<MensajeDTO> eliminar(
            @Parameter(description = "ObjectId del cliente", example = "6735f0c2a1b2c3d4e5f60730") @PathVariable String id) {
        clienteService.eliminar(id);

        return ResponseEntity.ok(new MensajeDTO("Cliente eliminado correctamente."));
    }

    // Modelo -> DTO de respuesta
    private ClienteDTOResponse aRespuesta(Cliente cliente) {
        return new ClienteDTOResponse(
                cliente.getId(),
                cliente.getNombre(),
                cliente.getDocumento(),
                cliente.getTelefono(),
                cliente.getCorreo()
        );
    }
}
