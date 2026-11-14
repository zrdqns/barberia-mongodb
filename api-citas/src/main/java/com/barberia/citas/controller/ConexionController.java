package com.barberia.citas.controller;

import com.barberia.citas.cliente.CatalogoCliente;
import com.barberia.citas.dto.MensajeDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/conexion")
@RequiredArgsConstructor
@Tag(name = "Estado", description = "Comprobación de que el microservicio está encendido y alcanza a api-catalogo")
public class ConexionController {

    private final CatalogoCliente catalogoCliente;

    // GET /api/conexion/catalogo
    // Comprueba desde este backend que api-catalogo responde.
    // Si está apagada, ManejadorErrores devuelve 503.
    @GetMapping("/catalogo")
    @Operation(summary = "Probar la comunicación con api-catalogo",
            description = "Público. api-citas llama a GET /api/status de api-catalogo con RestClient y devuelve su respuesta.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "api-catalogo respondió"),
            @ApiResponse(responseCode = "503", description = "api-catalogo no respondió",
                    content = @Content(schema = @Schema(implementation = MensajeDTO.class)))
    })
    public ResponseEntity<MensajeDTO> conexionCatalogo() {
        return ResponseEntity.ok(new MensajeDTO(catalogoCliente.verificarConexion()));
    }
}
