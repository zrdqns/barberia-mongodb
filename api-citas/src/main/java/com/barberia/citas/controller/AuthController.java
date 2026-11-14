package com.barberia.citas.controller;

import com.barberia.citas.dto.LoginDTORequest;
import com.barberia.citas.dto.LoginDTOResponse;
import com.barberia.citas.dto.MensajeDTO;
import com.barberia.citas.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticación", description = "Inicio de sesión con JWT")
public class AuthController {

    private final AuthService authService;

    // POST /api/auth/login
    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión",
            description = "Devuelve un JWT para enviar en el header Authorization: Bearer <token>. "
                    + "Usuarios de prueba: admin / 123456 (ADMIN) y recepcion / 123456 (USER).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Credenciales correctas: token emitido"),
            @ApiResponse(responseCode = "400", description = "Falta el usuario o la contraseña",
                    content = @Content(schema = @Schema(implementation = MensajeDTO.class))),
            @ApiResponse(responseCode = "401", description = "Usuario o contraseña incorrectos",
                    content = @Content(schema = @Schema(implementation = MensajeDTO.class)))
    })
    public ResponseEntity<LoginDTOResponse> login(@Valid @RequestBody LoginDTORequest request) {
        return ResponseEntity.ok(authService.iniciarSesion(request));
    }
}
