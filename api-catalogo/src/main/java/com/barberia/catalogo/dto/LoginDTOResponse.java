package com.barberia.catalogo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;

// Lo que devuelve el login: el token y para quién es.
@Getter
@AllArgsConstructor
@Schema(description = "Token JWT para enviar en el header Authorization: Bearer <token>")
public class LoginDTOResponse {

    @Schema(example = "eyJhbGciOiJIUzI1NiJ9...")
    private String token;
    @Schema(example = "Bearer")
    private String tipo;
    @Schema(example = "admin")
    private String usuario;
    @Schema(description = "ADMIN o USER", example = "ADMIN")
    private String rol;
    @Schema(description = "Cuándo deja de servir el token")
    private Instant expira;
}
