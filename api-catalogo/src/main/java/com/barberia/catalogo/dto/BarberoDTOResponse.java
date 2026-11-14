package com.barberia.catalogo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Datos que la API devuelve de un barbero.
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Un barbero de la barbería")
public class BarberoDTOResponse {

    @Schema(description = "ObjectId que asignó MongoDB", example = "6735f0c2a1b2c3d4e5f6071a")
    private String id;
    @Schema(example = "Andrés Rojas")
    private String nombre;
    @Schema(example = "1012345678")
    private String documento;
    @Schema(example = "3104567890")
    private String telefono;
    @Schema(example = "Cortes clásicos")
    private String especialidad;
    @Schema(example = "true")
    private boolean activo;
}
