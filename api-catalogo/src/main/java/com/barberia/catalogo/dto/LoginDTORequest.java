package com.barberia.catalogo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

// Credenciales para POST /api/auth/login
@Getter
@Setter
@Schema(description = "Usuario y contraseña")
public class LoginDTORequest {

    @NotBlank(message = "Escriba el usuario.")
    @Schema(example = "admin")
    private String username;

    @NotBlank(message = "Escriba la contraseña.")
    @Schema(example = "123456")
    private String password;
}
