package com.barberia.citas.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

// Datos que el cliente de la API envía para crear o actualizar un cliente.
// El id no viaja aquí: lo asigna MongoDB.
@Getter
@Setter
@Schema(description = "Datos para crear o actualizar un cliente")
public class ClienteDTORequest {

    @NotBlank(message = "El nombre del cliente es obligatorio.")
    @Size(max = 100, message = "El nombre no puede pasar de 100 caracteres.")
    @Schema(example = "Carlos Ramírez")
    private String nombre;

    @NotNull(message = "El documento debe tener entre 6 y 10 dígitos.")
    @Pattern(regexp = "\\s*\\d{6,10}\\s*", message = "El documento debe tener entre 6 y 10 dígitos.")
    @Schema(example = "1098765432")
    private String documento;

    @NotNull(message = "El teléfono debe tener entre 7 y 10 dígitos.")
    @Pattern(regexp = "\\s*\\d{7,10}\\s*", message = "El teléfono debe tener entre 7 y 10 dígitos.")
    @Schema(example = "3001234567")
    private String telefono;

    // Opcional, pero si se envía debe tener forma de correo
    @Size(max = 255, message = "El correo no puede pasar de 255 caracteres.")
    @Pattern(regexp = "\\s*([^@\\s]+@[^@\\s]+\\.[^@\\s]+)?\\s*", message = "El correo no tiene un formato válido.")
    @Schema(example = "carlos.ramirez@correo.com")
    private String correo;
}
