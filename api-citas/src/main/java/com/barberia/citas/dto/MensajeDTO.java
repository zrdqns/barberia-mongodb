package com.barberia.citas.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

// Respuesta con un mensaje: la de DELETE y la de cualquier error.
// En los errores de validación (400) trae además qué campo falló y por qué.
@Getter
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Mensaje de la API")
public class MensajeDTO {

    @Schema(example = "No se encontró una cita con el ID 6735f0c2a1b2c3d4e5f60720.")
    private String mensaje;

    @Schema(description = "Solo en errores de validación: campo y problema",
            example = "{\"telefono\": \"El teléfono debe tener entre 7 y 10 dígitos.\"}")
    private Map<String, String> errores;

    public MensajeDTO(String mensaje) {
        this(mensaje, null);
    }
}
