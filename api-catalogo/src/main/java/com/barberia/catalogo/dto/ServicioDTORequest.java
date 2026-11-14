package com.barberia.catalogo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

// Datos que el cliente envía para crear o actualizar un servicio.
// El id no viaja aquí: lo asigna MongoDB. @Valid revisa estas reglas antes de llegar al service.
@Getter
@Setter
@Schema(description = "Datos para crear o actualizar un servicio")
public class ServicioDTORequest {

    @NotBlank(message = "El nombre del servicio es obligatorio.")
    @Size(max = 100, message = "El nombre no puede pasar de 100 caracteres.")
    @Schema(example = "Corte clásico")
    private String nombre;

    @Size(max = 255, message = "La descripción no puede pasar de 255 caracteres.")
    @Schema(example = "Corte a tijera o máquina con lavado")
    private String descripcion;

    @NotNull(message = "El precio es obligatorio.")
    @Positive(message = "El precio debe ser mayor que cero.")
    @Schema(description = "Pesos colombianos, sin decimales", example = "25000")
    private Integer precio;

    @NotNull(message = "La duración es obligatoria.")
    @Min(value = 5, message = "La duración debe estar entre 5 y 240 minutos.")
    @Max(value = 240, message = "La duración debe estar entre 5 y 240 minutos.")
    @Schema(example = "30")
    private Integer duracionMinutos;

    @Schema(description = "Opcional. Si no se envía, el servicio queda activo.", example = "true")
    private Boolean activo;
}
