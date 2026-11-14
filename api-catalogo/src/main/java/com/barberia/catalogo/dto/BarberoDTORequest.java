package com.barberia.catalogo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

// Datos que el cliente envía para crear o actualizar un barbero.
@Getter
@Setter
@Schema(description = "Datos para crear o actualizar un barbero")
public class BarberoDTORequest {

    @NotBlank(message = "El nombre del barbero es obligatorio.")
    @Size(max = 100, message = "El nombre no puede pasar de 100 caracteres.")
    @Schema(example = "Andrés Rojas")
    private String nombre;

    @NotNull(message = "El documento debe tener entre 6 y 10 dígitos.")
    @Pattern(regexp = "\\s*\\d{6,10}\\s*", message = "El documento debe tener entre 6 y 10 dígitos.")
    @Schema(example = "1012345678")
    private String documento;

    // Opcional, pero si se envía sigue la misma regla que el de los clientes
    @Pattern(regexp = "\\s*(\\d{7,10})?\\s*", message = "El teléfono debe tener entre 7 y 10 dígitos.")
    @Schema(example = "3104567890")
    private String telefono;

    @Size(max = 255, message = "La especialidad no puede pasar de 255 caracteres.")
    @Schema(example = "Cortes clásicos")
    private String especialidad;

    @Schema(description = "Opcional. Si no se envía, el barbero queda activo.", example = "true")
    private Boolean activo;
}
